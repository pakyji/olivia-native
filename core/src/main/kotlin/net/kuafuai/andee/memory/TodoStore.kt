package net.kuafuai.andee.memory

import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TodoStore - Persistent todo/promise system with due times.
 */
@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "due_at") val dueAt: Long?,
    @ColumnInfo(name = "done") val done: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null
)

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items WHERE done = 0 ORDER BY due_at ASC NULLS LAST, created_at ASC")
    suspend fun getActive(): List<TodoItem>

    @Query("SELECT * FROM todo_items ORDER BY created_at DESC")
    suspend fun getAll(): List<TodoItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: TodoItem): Long

    @Update
    suspend fun update(item: TodoItem): Int

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM todo_items WHERE done = 1 AND completed_at < :before")
    suspend fun deleteCompletedBefore(before: Long): Int
}

@Database(entities = [TodoItem::class], version = 1, exportSchema = false)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile private var INSTANCE: TodoDatabase? = null

        fun getInstance(context: android.content.Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todos.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class TodoStore(private val db: TodoDatabase) {

    suspend fun add(text: String, dueAt: Long? = null): TodoItem? = withContext(Dispatchers.IO) {
        val item = TodoItem(text = text, dueAt = dueAt)
        val id = db.todoDao().insert(item)
        if (id > 0) item.copy(id = id) else null
    }

    suspend fun listActive(): List<TodoItem> = withContext(Dispatchers.IO) {
        db.todoDao().getActive()
    }

    suspend fun listAll(): List<TodoItem> = withContext(Dispatchers.IO) {
        db.todoDao().getAll()
    }

    suspend fun markDone(id: Long): Boolean = withContext(Dispatchers.IO) {
        val item = db.todoDao().getAll().firstOrNull { it.id == id } // Simplified
            ?: return@withContext false
        val updated = item.copy(done = true, completedAt = System.currentTimeMillis())
        db.todoDao().update(updated) > 0
    }

    suspend fun markDoneByIndex(index: Int): Boolean = withContext(Dispatchers.IO) {
        val active = db.todoDao().getActive()
        if (index >= 0 && index < active.size) {
            val item = active[index]
            val updated = item.copy(done = true, completedAt = System.currentTimeMillis())
            db.todoDao().update(updated) > 0
        } else {
            false
        }
    }

    suspend fun cleanupOldCompleted(): Int = withContext(Dispatchers.IO) {
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        db.todoDao().deleteCompletedBefore(weekAgo)
    }

    suspend fun getOverdue(): List<TodoItem> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        db.todoDao().getActive().filter { it.dueAt != null && it.dueAt!! < now }
    }
}