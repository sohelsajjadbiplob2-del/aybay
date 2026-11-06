package com.aybay.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ---------- লেনদেন ----------
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // আয় / ব্যয় / সঞ্চয় / ট্রান্সফার
    val amount: Long,
    val category: String,
    val account: String, // হাতে নগদ / ব্যাংক / মোবাইল
    val date: String, // yyyy-MM-dd
    val time: String, // HH:mm
    val note: String = "",
    val payer: String = "",
    val synced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "due_items")
data class DueItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val kind: String, // বাকি / ধার
    val direction: String, // নিয়েছি / দিয়েছি
    val name: String,
    val amount: Long,
    val date: String,
    val dueDate: String = "",
    val isPaid: Boolean = false,
    val synced: Boolean = false
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val monthKey: String, // yyyy-MM
    val limitAmount: Long
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val urgent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val kind: String, // ক্যাশ / ব্যাংক / মোবাইল
    val balance: Long = 0L
)

@Dao
interface AyBayDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun allTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC, id DESC")
    fun transactionsOfMonth(monthPrefix: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE synced = 0")
    suspend fun unsyncedTransactions(): List<Transaction>

    @Insert
    suspend fun insertTransaction(item: Transaction): Long

    @Update
    suspend fun updateTransaction(item: Transaction)

    @Delete
    suspend fun deleteTransaction(item: Transaction)

    @Query("UPDATE transactions SET synced = 1 WHERE id = :id")
    suspend fun markTransactionSynced(id: Int)

    @Query("SELECT * FROM due_items ORDER BY isPaid ASC, date DESC")
    fun allDueItems(): Flow<List<DueItem>>

    @Insert
    suspend fun insertDueItem(item: DueItem): Long

    @Update
    suspend fun updateDueItem(item: DueItem)

    @Delete
    suspend fun deleteDueItem(item: DueItem)

    @Query("SELECT * FROM budgets WHERE monthKey = :monthKey")
    fun budgetsOfMonth(monthKey: String): Flow<List<Budget>>

    @Insert
    suspend fun insertBudget(item: Budget): Long

    @Update
    suspend fun updateBudget(item: Budget)

    @Query("SELECT * FROM notes ORDER BY urgent DESC, createdAt DESC")
    fun allNotes(): Flow<List<Note>>

    @Insert
    suspend fun insertNote(item: Note): Long

    @Update
    suspend fun updateNote(item: Note)

    @Delete
    suspend fun deleteNote(item: Note)

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun allAccounts(): Flow<List<Account>>

    @Insert
    suspend fun insertAccount(item: Account): Long

    @Update
    suspend fun updateAccount(item: Account)
}

@Database(
    entities = [Transaction::class, DueItem::class, Budget::class, Note::class, Account::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AyBayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aybay.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
