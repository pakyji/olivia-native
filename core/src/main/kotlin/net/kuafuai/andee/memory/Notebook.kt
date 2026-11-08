package net.kuafuai.andee.memory

import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Notebook - Andee's 8-topic persistent memory system.
 * Topics: self, cares, diary, plan, people, wiki, memories, todos
 */
@Entity(tableName = "notebook_entries")
data class NotebookEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "topic") val topic: String,      // self, cares, diary, plan, people, wiki, memories, todos
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface NotebookDao {
    @Query("SELECT * FROM notebook_entries WHERE topic = :topic")
    suspend fun getByTopic(topic: String): List<NotebookEntry>

    @Query("SELECT * FROM notebook_entries WHERE topic = :topic AND key = :key")
    suspend fun getByTopicAndKey(topic: String, key: String): NotebookEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: NotebookEntry): Long

    @Delete
    suspend fun delete(entry: NotebookEntry)

    @Query("DELETE FROM notebook_entries WHERE topic = :topic AND key = :key")
    suspend fun deleteByTopicAndKey(topic: String, key: String): Int

    @Query("SELECT topic, COUNT(*) as count FROM notebook_entries GROUP BY topic")
    suspend fun getTopicCounts(): List<TopicCount>

    @Query("DELETE FROM notebook_entries WHERE topic = :topic")
    suspend fun clearTopic(topic: String): Int
}

data class TopicCount(val topic: String, val count: Int)

@Database(entities = [NotebookEntry::class], version = 1, exportSchema = false)
abstract class NotebookDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao

    companion object {
        @Volatile private var INSTANCE: NotebookDatabase? = null

        fun getInstance(context: android.content.Context): NotebookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotebookDatabase::class.java,
                    "notebook.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * High-level Notebook API
 */
class Notebook(private val db: NotebookDatabase) {

    companion object {
        val VALID_TOPICS = setOf("self", "cares", "diary", "plan", "people", "wiki", "memories", "todos")
    }

    suspend fun recall(topic: String, key: String? = null): RecallResult = withContext(Dispatchers.IO) {
        if (!VALID_TOPICS.contains(topic)) {
            return@withContext RecallResult(false, error = "Invalid topic: $topic. Valid: ${VALID_TOPICS.joinToString()}")
        }

        if (key == null) {
            val entries = db.notebookDao().getByTopic(topic)
            return@withContext RecallResult(true, allEntries = entries.associate { it.key to it.value })
        }

        val entry = db.notebookDao().getByTopicAndKey(topic, key)
        if (entry != null) {
            RecallResult(true, value = entry.value)
        } else {
            RecallResult(false, error = "Key not found: $topic.$key")
        }
    }

    suspend fun remember(topic: String, key: String, value: String): Boolean = withContext(Dispatchers.IO) {
        if (!VALID_TOPICS.contains(topic)) return false
        val entry = NotebookEntry(topic = topic, key = key, value = value)
        db.notebookDao().insert(entry) > 0
    }

    suspend fun forget(topic: String, key: String): Boolean = withContext(Dispatchers.IO) {
        if (!VALID_TOPICS.contains(topic)) return false
        db.notebookDao().deleteByTopicAndKey(topic, key) > 0
    }

    suspend fun listTopics(): Map<String, Int> = withContext(Dispatchers.IO) {
        db.notebookDao().getTopicCounts().associate { it.topic to it.count }
    }

    data class RecallResult(
        val success: Boolean,
        val value: String? = null,
        val allEntries: Map<String, String> = emptyMap(),
        val error: String? = null
    )
}