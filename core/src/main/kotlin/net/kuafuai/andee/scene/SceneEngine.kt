package net.kuafuai.andee.scene

import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * SceneEngine - Reusable routines (Andee-style scenes).
 * Scenes are sequences of steps: say, cmd, wait, notify
 */

@Serializable
data class Scene(
    val name: String,
    val description: String,
    val steps: List<SceneStep>
)

@Serializable
sealed class SceneStep {
    @Serializable
    data class Say(val text: String) : SceneStep()
    @Serializable
    data class Cmd(val action: String, val args: Map<String, Any> = emptyMap()) : SceneStep()
    @Serializable
    data class Wait(val ms: Long) : SceneStep()
    @Serializable
    data class Notify(val title: String, val text: String) : SceneStep()
}

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "steps_json") val stepsJson: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface SceneDao {
    @Query("SELECT * FROM scenes")
    suspend fun getAll(): List<SceneEntity>

    @Query("SELECT * FROM scenes WHERE name = :name")
    suspend fun getByName(name: String): SceneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SceneEntity): Long

    @Delete
    suspend fun delete(entity: SceneEntity)

    @Query("DELETE FROM scenes WHERE name = :name")
    suspend fun deleteByName(name: String): Int
}

@Database(entities = [SceneEntity::class], version = 1, exportSchema = false)
abstract class SceneDatabase : RoomDatabase() {
    abstract fun sceneDao(): SceneDao

    companion object {
        @Volatile private var INSTANCE: SceneDatabase? = null

        fun getInstance(context: android.content.Context): SceneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SceneDatabase::class.java,
                    "scenes.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class SceneEngine(
    private val db: SceneDatabase,
    private val brainHost: net.kuafuai.andee.brain.BrainHost
) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        val DEFAULT_SCENES = mapOf(
            "morning" to Scene(
                name = "morning",
                description = "Morning briefing: time, battery, top todos",
                steps = listOf(
                    SceneStep.Say("Good morning! Here's your briefing."),
                    SceneStep.Cmd("time"),
                    SceneStep.Cmd("battery"),
                    SceneStep.Cmd("todolist")
                )
            ),
            "focus" to Scene(
                name = "focus",
                description = "Focus mode: show current task",
                steps = listOf(
                    SceneStep.Say("Focus mode activated. Current task:"),
                    SceneStep.Cmd("todolist")
                )
            ),
            "evening" to Scene(
                name = "evening",
                description = "Evening wrap-up: review day, plan tomorrow",
                steps = listOf(
                    SceneStep.Say("Evening wrap-up. How did today go?"),
                    SceneStep.Cmd("todolist"),
                    SceneStep.Say("Want to log anything to your diary?")
                )
            ),
            "backup" to Scene(
                name = "backup",
                description = "Backup & sync: zip project, push to GitHub",
                steps = listOf(
                    SceneStep.Cmd("zip_project"),
                    SceneStep.Cmd("git_push")
                )
            )
        )
    }

    suspend fun initializeDefaults() = withContext(Dispatchers.IO) {
        for (scene in DEFAULT_SCENES.values) {
            val existing = db.sceneDao().getByName(scene.name)
            if (existing == null) {
                save(scene)
            }
        }
    }

    suspend fun list(): Map<String, Scene> = withContext(Dispatchers.IO) {
        db.sceneDao().getAll().associate { it.name to json.decodeFromString<Scene>(it.stepsJson).copy(description = it.description) }
    }

    suspend fun get(name: String): Scene? = withContext(Dispatchers.IO) {
        db.sceneDao().getByName(name)?.let { json.decodeFromString<Scene>(it.stepsJson).copy(description = it.description) }
    }

    suspend fun save(scene: Scene): Boolean = withContext(Dispatchers.IO) {
        val entity = SceneEntity(
            name = scene.name,
            description = scene.description,
            stepsJson = json.encodeToString(scene)
        )
        db.sceneDao().insert(entity) > 0
    }

    suspend fun delete(name: String): Boolean = withContext(Dispatchers.IO) {
        db.sceneDao().deleteByName(name) > 0
    }

    /**
     * Execute a scene by name. Returns the steps for the brain to process.
     */
    suspend fun execute(name: String): SceneResult = withContext(Dispatchers.IO) {
        val scene = get(name) ?: return SceneResult(false, "Scene not found: $name")

        // Execute steps sequentially
        var lastResult: String? = null
        for (step in scene.steps) {
            when (step) {
                is SceneStep.Say -> {
                    brainHost.dispatchTool("tts.speak", mapOf("text" to step.text))
                    lastResult = step.text
                }
                is SceneStep.Cmd -> {
                    val result = brainHost.dispatchTool(step.action, step.args)
                    lastResult = if (result.success) result.result else "ERROR: ${result.error}"
                }
                is SceneStep.Wait -> {
                    Thread.sleep(step.ms)
                }
                is SceneStep.Notify -> {
                    brainHost.dispatchTool("notify", mapOf("title" to step.title, "text" to step.text))
                }
            }
        }

        SceneResult(true, "Scene '$name' completed: ${scene.description}")
    }

    data class SceneResult(
        val success: Boolean,
        val message: String
    )
}