package com.aybay.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aybay.app.data.AppDatabase
import com.aybay.app.data.Budget
import com.aybay.app.data.DueItem
import com.aybay.app.data.FirebaseSync
import com.aybay.app.data.Note
import com.aybay.app.data.Account
import com.aybay.app.data.Repository
import com.aybay.app.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AyBayViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val repo = Repository(db.dao())
    val sync = FirebaseSync(app)

    val transactions: StateFlow<List<Transaction>> = repo.transactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalsAll: StateFlow<com.aybay.app.data.Totals> = repo.totalsAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.aybay.app.data.Totals())
    val dueItems: StateFlow<List<DueItem>> = repo.dueItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notes: StateFlow<List<Note>> = repo.notes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val accounts: StateFlow<List<Account>> = repo.accounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val deletedTransactions: StateFlow<List<Transaction>> = repo.deletedTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ব্যানার (জমার মতো অস্থায়ী ওভারলে): সতর্ক ~২s, সফলতা ~৪.৫s।
    // Tick বদলালে MainActivity-র টাইমার নতুন করে শুরু হয় (আবার চাপ দিলে রিস্টার্ট)।
    var warningVisible by mutableStateOf(false)
        private set
    var warningType by mutableStateOf("আয়")
        private set
    var warningTick by mutableStateOf(0)
        private set
    var savedBanner by mutableStateOf<SavedInfo?>(null)
        private set
    var savedTick by mutableStateOf(0)
        private set

    fun showWarningBanner(type: String) {
        savedBanner = null
        warningType = type
        warningVisible = true
        warningTick++
    }

    fun dismissWarning() {
        warningVisible = false
    }

    fun showSavedBanner(info: SavedInfo) {
        warningVisible = false
        savedBanner = info
        savedTick++
    }

    fun dismissSavedBanner() {
        savedBanner = null
    }

    init {
        // অ্যাপ চালুতে ৩ দিনের বেশি পুরনো মোছা হিসাব চিরতরে মুছে যায়।
        viewModelScope.launch { repo.purgeExpiredDeleted() }
    }

    fun totalsOfMonth(monthPrefix: String) = repo.totalsOfMonth(monthPrefix)
    fun categoryTotals(monthPrefix: String, type: String) =
        repo.categoryTotals(monthPrefix, type)
    fun transactionsOfMonth(monthPrefix: String) = repo.transactionsOfMonth(monthPrefix)
    fun budgetsOfMonth(monthKey: String) = repo.budgetsOfMonth(monthKey)
    fun allBudgets() = repo.allBudgets()

    fun addTransaction(item: Transaction) {
        viewModelScope.launch {
            val id = repo.addTransaction(item)
            if (sync.isAvailable && sync.isSignedIn) {
                val ok = sync.pushTransaction(item.copy(id = id.toInt()))
                if (ok) repo.markTransactionSynced(id.toInt())
            }
        }
    }

    // মুছে ফেলা = রিসাইকেল বিনে যাওয়া (চিরতরে মোছে না)।
    fun deleteTransaction(item: Transaction) {
        viewModelScope.launch { repo.softDeleteTransaction(item) }
    }

    fun restoreTransaction(item: Transaction) {
        viewModelScope.launch { repo.restoreTransaction(item) }
    }

    fun deleteTransactionPermanently(item: Transaction) {
        viewModelScope.launch { repo.deleteTransactionPermanently(item) }
    }

    fun addDueItem(item: DueItem) {
        viewModelScope.launch {
            val id = repo.addDueItem(item)
            if (sync.isAvailable && sync.isSignedIn) {
                sync.pushDueItem(item.copy(id = id.toInt()))
            }
        }
    }

    fun updateDueItem(item: DueItem) {
        viewModelScope.launch { repo.updateDueItem(item) }
    }

    fun deleteDueItem(item: DueItem) {
        viewModelScope.launch { repo.deleteDueItem(item) }
    }

    fun addBudget(item: Budget) {
        viewModelScope.launch { repo.addBudget(item) }
    }

    fun updateBudget(item: Budget) {
        viewModelScope.launch { repo.updateBudget(item) }
    }

    fun addNote(item: Note) {
        viewModelScope.launch {
            val id = repo.addNote(item)
            if (sync.isAvailable && sync.isSignedIn) {
                sync.pushNote(item.copy(id = id.toInt()))
            }
        }
    }

    fun updateNote(item: Note) {
        viewModelScope.launch { repo.updateNote(item) }
    }

    fun deleteNote(item: Note) {
        viewModelScope.launch { repo.deleteNote(item) }
    }

    fun addAccount(item: Account) {
        viewModelScope.launch { repo.addAccount(item) }
    }

    private var defaultEnsured = false

    fun ensureDefaultAccount() {
        if (defaultEnsured) return
        defaultEnsured = true
        viewModelScope.launch {
            val current = accounts.value
            if (current.isEmpty()) {
                repo.addAccount(Account(name = "হাতে নগদ", kind = "ক্যাশ", balance = 0L))
            }
        }
    }
}
