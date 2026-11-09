package com.aybay.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// মোট হিসাব: আয়, ব্যয়, সঞ্চয় থেকে নগদ ব্যালেন্স বের করা হয়।
data class Totals(
    val income: Long = 0L,
    val expense: Long = 0L,
    val savings: Long = 0L
) {
    val balance: Long get() = income - expense - savings
    val netSavings: Long get() = income - expense
}

// সঞ্চয়ের নীট হিসাব: জমা যোগ, উত্তোলন বিয়োগ। পুরনো সঞ্চয়
// লেনদেনে direction ফাঁকা থাকে — সেগুলো জমা ধরা হয়।
private fun savingsSigned(t: Transaction): Long =
    if (t.direction == "উত্তোলন") -t.amount else t.amount

class Repository(private val dao: AyBayDao) {
    fun transactions(): Flow<List<Transaction>> = dao.allTransactions()
    fun transactionsOfMonth(monthPrefix: String): Flow<List<Transaction>> =
        dao.transactionsOfMonth(monthPrefix)

    // রিসাইকেল বিনের মোছা হিসাব।
    fun deletedTransactions(): Flow<List<Transaction>> = dao.deletedTransactions()

    // মুছে ফেলা (সফট-ডিলিট): রিসাইকেল বিনে যায়, চিরতরে মোছে না।
    suspend fun softDeleteTransaction(item: Transaction) =
        dao.updateTransaction(item.copy(deletedAt = System.currentTimeMillis()))

    // রিসাইকেল বিন থেকে ফেরত আনা।
    suspend fun restoreTransaction(item: Transaction) =
        dao.updateTransaction(item.copy(deletedAt = 0L))

    // রিসাইকেল বিন থেকে চিরতরে মোছা।
    suspend fun deleteTransactionPermanently(item: Transaction) =
        dao.deleteTransaction(item)

    // ৩ দিনের বেশি পুরনো মোছা হিসাব চিরতরে মুছে ফেলা (অ্যাপ চালুতে চলে)।
    suspend fun purgeExpiredDeleted() {
        val threeDaysAgo = System.currentTimeMillis() - 3L * 24L * 60L * 60L * 1000L
        dao.purgeDeleted(threeDaysAgo)
    }

    fun totalsOfMonth(monthPrefix: String): Flow<Totals> =
        dao.transactionsOfMonth(monthPrefix).map { list ->
            var income = 0L
            var expense = 0L
            var savings = 0L
            for (t in list) {
                when (t.type) {
                    "আয়" -> income += t.amount
                    "ব্যয়" -> expense += t.amount
                    "সঞ্চয়" -> savings += savingsSigned(t)
                }
            }
            Totals(income, expense, savings)
        }

    fun totalsAll(): Flow<Totals> = dao.allTransactions().map { list ->
        var income = 0L
        var expense = 0L
        var savings = 0L
        for (t in list) {
            when (t.type) {
                "আয়" -> income += t.amount
                "ব্যয়" -> expense += t.amount
                "সঞ্চয়" -> savings += savingsSigned(t)
            }
        }
        Totals(income, expense, savings)
    }

    fun categoryTotals(monthPrefix: String, type: String): Flow<Map<String, Long>> =
        dao.transactionsOfMonth(monthPrefix).map { list ->
            val map = linkedMapOf<String, Long>()
            for (t in list) {
                if (t.type == type) {
                    val signed = if (type == "সঞ্চয়") savingsSigned(t) else t.amount
                    map[t.category] = (map[t.category] ?: 0L) + signed
                }
            }
            map.entries.sortedByDescending { it.value }.associate { it.key to it.value }
        }

    suspend fun addTransaction(item: Transaction): Long = dao.insertTransaction(item)
    suspend fun updateTransaction(item: Transaction) = dao.updateTransaction(item)
    suspend fun deleteTransaction(item: Transaction) = dao.deleteTransaction(item)
    suspend fun unsyncedTransactions(): List<Transaction> = dao.unsyncedTransactions()
    suspend fun markTransactionSynced(id: Int) = dao.markTransactionSynced(id)

    fun dueItems(): Flow<List<DueItem>> = dao.allDueItems()
    suspend fun addDueItem(item: DueItem): Long = dao.insertDueItem(item)
    suspend fun updateDueItem(item: DueItem) = dao.updateDueItem(item)
    suspend fun deleteDueItem(item: DueItem) = dao.deleteDueItem(item)

    fun budgetsOfMonth(monthKey: String): Flow<List<Budget>> = dao.budgetsOfMonth(monthKey)
    fun allBudgets(): Flow<List<Budget>> = dao.allBudgets()
    suspend fun addBudget(item: Budget): Long = dao.insertBudget(item)
    suspend fun updateBudget(item: Budget) = dao.updateBudget(item)
    suspend fun deleteBudget(item: Budget) = dao.deleteBudget(item)

    fun notes(): Flow<List<Note>> = dao.allNotes()
    suspend fun addNote(item: Note): Long = dao.insertNote(item)
    suspend fun updateNote(item: Note) = dao.updateNote(item)
    suspend fun deleteNote(item: Note) = dao.deleteNote(item)

    fun accounts(): Flow<List<Account>> = dao.allAccounts()
    suspend fun addAccount(item: Account): Long = dao.insertAccount(item)
    suspend fun updateAccount(item: Account) = dao.updateAccount(item)
}
