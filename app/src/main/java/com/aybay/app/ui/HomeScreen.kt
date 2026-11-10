package com.aybay.app.ui

import android.app.DatePickerDialog
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aybay.app.data.Totals
import com.aybay.app.data.Transaction
import com.aybay.app.data.scheduleAutoIncome
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

val incomeCategories = listOf("মাসিক বেতন", "দৈনিক মজুরি", "সাপ্তাহিক বেতন", "অন্যান্য")
val expenseCategories = listOf(
    "বাজার", "বিল", "ধর্মীয় দান/সহযোগিতা", "হাতখরচ",
    "যানবাহনের তেল/গ্যাস", "মার্কেট/শপিং", "গাড়ি ভাড়া", "রেস্টুরেন্ট",
    "বাসা ভাড়া", "দোকান ভাড়া", "ফি", "মেনটেনেন্স",
    "উন্নয়নমূলক খরচ", "কিস্তি", "অন্যান্য ব্যয়"
)

object HomeNavState {
    var reportQuery: String = ""
    // বিশ্লেষণ কার্ডের "সম্পাদন করুন" থেকে রিপোর্টে যাওয়ার সময় ধরন ফিল্টার ("আয়"/"ব্যয়"/"সঞ্চয়"; খালি = সব)।
    var reportType: String = ""
    // হোমের বাকি ও ধার সেকশনের বাক্স থেকে বাকি ও ধার পেজে যাওয়ার সময় ট্যাব ("বাকি"/"ধার"; খালি = ডিফল্ট "বাকি")।
    var dueTab: String = ""
}

private fun totalsOf(list: List<Transaction>): Totals {
    var income = 0L
    var expense = 0L
    var savings = 0L
    for (t in list) {
        when (t.type) {
            "আয়" -> income += t.amount
            "ব্যয়" -> expense += t.amount
            // জমা যোগ, উত্তোলন বিয়োগ (পুরনো সঞ্চয় = জমা)।
            "সঞ্চয়" -> savings += if (t.direction == "উত্তোলন") -t.amount else t.amount
        }
    }
    return Totals(income, expense, savings)
}

private fun percentOf(part: Long, total: Long): String {
    if (total <= 0L) return "০%"
    return toBengaliDigits((part * 100 / total).toString()) + "%"
}

private fun backupFileExists(): Boolean {
    return try {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "আয়-ব্যয়"
        )
        File(dir, "backup.json").exists()
    } catch (e: Exception) {
        false
    }
}

private fun accountKindLabel(kind: String): String = when (kind) {
    "ক্যাশ" -> tr("হাতে থাকা টাকা", "Cash in hand")
    "ব্যাংক" -> tr("ব্যাংক", "Bank")
    "মোবাইল" -> tr("মোবাইল", "Mobile")
    else -> kind
}

@Composable
fun HomeScreen(
    viewModel: AyBayViewModel,
    onAdd: (String) -> Unit,
    onOpenDue: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val dueItems by viewModel.dueItems.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val totalsAll by viewModel.totalsAll.collectAsState()
    val today = LocalDate.now()
    val todayKey = today.toString()
    val monthKey = todayKey.substring(0, 7)
    val expenseByCatMonth by viewModel.categoryTotals(monthKey, "ব্যয়").collectAsState(initial = emptyMap())
    val budgets by viewModel.budgetsOfMonth(monthKey).collectAsState(initial = emptyList())
    val allBudgets by viewModel.allBudgets().collectAsState(initial = emptyList())
    val totalBudgetAll = allBudgets.sumOf { it.limitAmount }
    var prefs by remember { mutableStateOf(loadAppPrefs(context)) }

    var period by remember { mutableStateOf("মাসিক") }
    val shown = totalsOf(filterByPeriod(transactions, period, today))

    // মোট কার্ডের তালা: AppPrefs.lockedSections-এ সেভ থাকে (অ্যাপ বন্ধ করলেও থাকে)।
    // SharedPreferences থেকে প্রতিবার নতুন করে পড়া হয়, যাতে সেটিংসের ডায়ালগে
    // বদলানো লক হোমে ফিরেই দেখা যায়।
    val lockedCards = loadAppPrefs(context).lockedSections
        .filter { it == "আয়" || it == "ব্যয়" || it == "সঞ্চয়" }.toSet()
    fun setSectionLocked(key: String, locked: Boolean) {
        val now = loadAppPrefs(context)
        val next = if (locked) now.lockedSections + key else now.lockedSections - key
        val updated = now.copy(lockedSections = next)
        saveAppPrefs(context, updated)
        prefs = updated
    }
    // লক করা কার্ড খুলতে পিন ডায়ালগ: কোন কার্ড খোলা হচ্ছে ("আয়"/"ব্যয়"/"সঞ্চয়")।
    var unlockCard by remember { mutableStateOf<String?>(null) }
    var searchUnlocked by remember { mutableStateOf(false) }
    var showSearchPin by remember { mutableStateOf(false) }
    // তালা চাপলে কনফার্ম পপআপ: কোন কার্ড লক করতে চাওয়া হচ্ছে ("আয়"/"ব্যয়"/"সঞ্চয়")।
    var lockConfirmCard by remember { mutableStateOf<String?>(null) }
    // লক করার পর পিন না থাকলে পিন ডায়ালগ নিজে থেকে খুলবে (জমার মতো)।
    var showPinDialog by remember { mutableStateOf(false) }
    // লেনদেন অংশ: কোন মোট কার্ডের অংশ খোলা আছে ("আয়"/"ব্যয়"/"সঞ্চয়"), না থাকলে null।
    var ledgerPage by remember { mutableStateOf<String?>(null) }
    // লেনদেন অংশের সময়সীমা: খোলার সময় হোমের সময়সীমা থেকে নেয়, তারপর নিজে চলে।
    var ledgerPeriod by remember { mutableStateOf("মাসিক") }
    // লেনদেন অংশের ডায়ালগ (রুটে থাকে) আর বিশ্লেষণে লুকানো খাত।
    var ledgerHiddenCats by remember { mutableStateOf(loadHiddenAnalysisCats(context)) }
    var showLedgerRecycleBin by remember { mutableStateOf(false) }
    var showLedgerAnalysisEdit by remember { mutableStateOf(false) }
    // ট্রান্সফার ডায়ালগ: দ্রুত যোগের 🔄 টাইল আর অ্যাকাউন্ট অংশের
    // ট্রান্সফার বোতাম — দুটোই এই ডায়ালগ খোলে।
    var showTransferDialog by remember { mutableStateOf(false) }
    val deletedTransactions by viewModel.deletedTransactions.collectAsState()
    fun openLedger(type: String) {
        ledgerPeriod = period
        ledgerPage = type
    }
    fun toggleLedger(type: String) {
        if (ledgerPage == type) ledgerPage = null else openLedger(type)
    }
    // হোমের স্ক্রল: "সব দেখুন"-এ চাপ দিলে লেনদেন অংশটা উপরে খোলে, তাই স্ক্রল করে সেখানে নিয়ে যেতে হয়।
    val homeListState = rememberLazyListState()
    val homeScope = rememberCoroutineScope()
    fun openLedgerAndShow(type: String) {
        openLedger(type)
        homeScope.launch { homeListState.animateScrollToItem(0) }
    }
    // ইনলাইন লেনদেন অংশের হিসাব (বিশ্লেষণের ডায়ালগেও একই হিসাব লাগে)।
    val ledgerPeriodList = if (ledgerPage == null) emptyList() else
        ledgerFilterByPeriod(transactions, ledgerPeriod, today)
            .filter { it.type == ledgerPage }
            .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id })
    val ledgerPeriodTotal = ledgerPeriodList.sumOf { ledgerSigned(it) }
    val ledgerAnalysisGroups: List<Pair<String, Long>> = run {
        val map = linkedMapOf<String, Long>()
        for (t in ledgerPeriodList) {
            map[t.category] = (map[t.category] ?: 0L) + ledgerSigned(t)
        }
        map.entries.sortedByDescending { it.value }.map { it.key to it.value }
    }
    var pendingLedgerAfterUnlock by remember { mutableStateOf<String?>(null) }
    var pendingAutoDialogAfterUnlock by remember { mutableStateOf(false) }
    var showBudgetSetup by remember { mutableStateOf(false) }
    var pendingBudgetSetupAfterUnlock by remember { mutableStateOf(false) }
    var expandedCards by remember { mutableStateOf(setOf<String>()) }

    var balanceHidden by rememberSaveable { mutableStateOf(false) }

    var backupDone by remember { mutableStateOf(backupFileExists()) }

    var arrangeMode by remember { mutableStateOf(false) }
    var sectionOrder by remember { mutableStateOf(loadHomeSectionOrder(context)) }
    val orderedVisible = sectionOrder
    fun moveSection(key: String, delta: Int) {
        val idx = sectionOrder.indexOf(key)
        val next = idx + delta
        if (idx < 0 || next < 0 || next >= sectionOrder.size) return
        val list = sectionOrder.toMutableList()
        val tmp = list[idx]
        list[idx] = list[next]
        list[next] = tmp
        sectionOrder = list
        saveHomeSectionOrder(context, list)
    }

    var showAutoDialog by remember { mutableStateOf(false) }

    var chartMode by remember { mutableStateOf("মাসিক") }
    val chartTx = if (chartMode == "দৈনিক") {
        transactions.filter { it.date == todayKey }
    } else {
        transactions.filter { it.date.startsWith(monthKey) }
    }
    val chartTotals = totalsOf(chartTx)
    val chartIncomeCount = chartTx.count { it.type == "আয়" }
    val chartExpenseCount = chartTx.count { it.type == "ব্যয়" }

    var flowMonth by remember { mutableStateOf(monthKey) }
    var selectedDay by remember { mutableStateOf(today.dayOfMonth) }
    val flowYm = YearMonth.parse(flowMonth)
    val flowDays = flowYm.lengthOfMonth()
    val flowDaily = List(flowDays) { d ->
        val key = flowMonth + "-" + (d + 1).toString().padStart(2, '0')
        transactions.filter { it.date == key && it.type == "ব্যয়" }.sumOf { it.amount }
    }
    val flowTotal = flowDaily.sum()
    val flowExpenseDays = flowDaily.count { it > 0L }
    val flowMax = flowDaily.maxOrNull() ?: 0L
    val flowMaxDay = if (flowMax > 0L) flowDaily.indexOf(flowMax) + 1 else 0
    val flowMin = flowDaily.filter { it > 0L }.minOrNull() ?: 0L
    val flowMinDay = if (flowMin > 0L) flowDaily.indexOf(flowMin) + 1 else 0
    val daysElapsed = if (flowMonth == monthKey) today.dayOfMonth else flowDays
    val flowAvg = if (daysElapsed > 0) flowTotal / daysElapsed.toLong() else 0L
    val selDay = selectedDay.coerceIn(1, flowDays)
    val selValue = flowDaily[selDay - 1]
    val selDate = flowYm.atDay(selDay).toString()

    val pendingDue = dueItems.filter { !it.isPaid }
    val dueTotal = pendingDue.filter { it.kind == "বাকি" }.sumOf { it.amount }
    val borrowed = pendingDue.filter { it.kind == "ধার" && it.direction == "নিয়েছি" }.sumOf { it.amount }
    val lent = pendingDue.filter { it.kind == "ধার" && it.direction == "দিয়েছি" }.sumOf { it.amount }

    var ledgerType by remember { mutableStateOf("আয়") }
    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }
    var ledgerQuery by remember { mutableStateOf("") }
    val ledgerList = transactions.filter {
        it.type == ledgerType &&
            (fromDate == null || it.date >= fromDate.toString()) &&
            (toDate == null || it.date <= toDate.toString())
    }
    val ledgerGroups = ledgerList.groupBy { it.payer.ifBlank { it.category } }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .entries.sortedByDescending { it.value }
    val ledgerShown = if (ledgerQuery.isBlank()) {
        ledgerGroups
    } else {
        ledgerGroups.filter { it.key.contains(ledgerQuery, ignoreCase = true) }
    }
    val ledgerTotal = ledgerShown.sumOf { it.value }
    val ledgerMax = ledgerShown.maxOfOrNull { it.value } ?: 0L
    val ledgerMaxName = ledgerShown.maxByOrNull { it.value }?.key ?: ""

    var editCatType by remember { mutableStateOf<String?>(null) }
    var customCats by remember { mutableStateOf<Set<String>>(emptySet()) }
    var newCatText by remember { mutableStateOf("") }

    fun pickDate(initial: LocalDate, onPick: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, y, m, d -> onPick(LocalDate.of(y, m + 1, d)) },
            initial.year, initial.monthValue - 1, initial.dayOfMonth
        ).show()
    }

    // ট্রান্সফার: এই অ্যাকাউন্ট থেকে গেলে কমে, এই অ্যাকাউন্টে এলে বাড়ে
    // (payer-এ গন্তব্যের নাম থাকে)।
    val accountBalances = accounts.map { acc ->
        acc to (acc.balance + transactions.filter {
            it.account == acc.name ||
                (it.type == "ট্রান্সফার" && it.payer == acc.name)
        }.sumOf {
            when (it.type) {
                "আয়" -> it.amount
                "ব্যয়" -> -it.amount
                // সঞ্চয়ে জমা দিলে নগদ কমে, তুললে নগদ বাড়ে।
                "সঞ্চয়" -> if (it.direction == "উত্তোলন") it.amount else -it.amount
                "ট্রান্সফার" -> if (it.account == acc.name) -it.amount else it.amount
                else -> 0L
            }
        } + dueLoanBalanceEffect(dueItems, acc.name))
    }
    val totalBalance = if (accountBalances.isEmpty()) {
        totalsAll.balance
    } else {
        accountBalances.sumOf { it.second }
    }

    LazyColumn(
        state = homeListState,
        modifier = Modifier.fillMaxWidth().background(PageBg).padding(horizontal = 12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightBlueCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = JomaBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = tr("বর্তমান হাতে নগদ", "Current Cash in Hand"),
                                    modifier = Modifier.padding(start = 6.dp),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { balanceHidden = !balanceHidden },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (balanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = tr("ব্যালেন্স লুকান/দেখান", "Hide/show balance"),
                                        tint = JomaBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (balanceHidden) "৳ ••••••" else moneyText(totalsAll.balance),
                                modifier = Modifier.padding(top = 6.dp),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        JomaBalanceDonut(shown.savings, totalsAll.balance, shown.expense)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .background(
                                if (backupDone) GreenMain else GreenTint,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                val file = exportBackupJson(context, transactions)
                                if (file != null) backupDone = true
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (backupDone) {
                                tr("📊 ডাটা ব্যাকআপ করা হয়েছে", "📊 Data backed up")
                            } else {
                                tr("📊 ডাটা ব্যাকআপ করুন", "📊 Back up data")
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (backupDone) Color.White else GreenMain
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (p in listOf("দৈনিক", "সাপ্তাহিক", "মাসিক", "বার্ষিক")) {
                        val selected = period == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selected) JomaBlue else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { period = p }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = periodLabel(p),
                                color = if (selected) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JomaTotalCard(
                    title = tr("মোট আয়", "Total Income"),
                    amount = moneyText(shown.income),
                    icon = Icons.Default.ArrowDownward,
                    color = GreenMain,
                    bgColor = GreenTint,
                    locked = "আয়" in lockedCards,
                    onLockClick = {
                        if ("আয়" in lockedCards) unlockCard = "আয়" else lockConfirmCard = "আয়"
                    },
                    expanded = "আয়" in expandedCards,
                    onExpandClick = {
                        expandedCards = if ("আয়" in expandedCards) expandedCards - "আয়" else expandedCards + "আয়"
                    },
                    expandedLines = listOf(
                        tr("মাসিক সেট", "Monthly set") + ": " + moneyText(prefs.autoSalaryAmount),
                        tr("অটো সেট থেকে নির্ধারণ করুন", "Set from Auto Set")
                    ),
                    modifier = Modifier.weight(1f),
                    onBodyClick = {
                        if ("আয়" in lockedCards) {
                            pendingLedgerAfterUnlock = "আয়"
                            unlockCard = "আয়"
                        } else {
                            toggleLedger("আয়")
                        }
                    },
                    onExpandedClick = {
                        if ("আয়" in lockedCards) {
                            pendingAutoDialogAfterUnlock = true
                            unlockCard = "আয়"
                        } else {
                            showAutoDialog = true
                        }
                    }
                )
                JomaTotalCard(
                    title = tr("মোট ব্যয়", "Total Expense"),
                    amount = moneyText(shown.expense),
                    icon = Icons.Default.ArrowUpward,
                    color = RedMain,
                    bgColor = RedTint,
                    locked = "ব্যয়" in lockedCards,
                    onLockClick = {
                        if ("ব্যয়" in lockedCards) unlockCard = "ব্যয়" else lockConfirmCard = "ব্যয়"
                    },
                    expanded = "ব্যয়" in expandedCards,
                    onExpandClick = {
                        expandedCards = if ("ব্যয়" in expandedCards) expandedCards - "ব্যয়" else expandedCards + "ব্যয়"
                    },
                    expandedLines = listOf(
                        when (period) {
                            "দৈনিক" -> tr("দৈনিক বাজেট", "Daily Budget") + ": " + moneyText(loadMonthlyBudgetDaily(context, monthKey))
                            "সাপ্তাহিক" -> tr("সাপ্তাহিক বাজেট", "Weekly Budget") + ": " + moneyText(loadMonthlyBudgetDaily(context, monthKey) * 7)
                            "বার্ষিক" -> tr("বার্ষিক বাজেট", "Yearly Budget") + ": " + moneyText(totalBudgetAll * 12)
                            else -> tr("মোট বাজেট", "Total Budget") + ": " + moneyText(totalBudgetAll)
                        },
                        tr("বাজেট সেকশনে সেট করুন", "Set in Budget section")
                    ),
                    modifier = Modifier.weight(1f),
                    onBodyClick = {
                        if ("ব্যয়" in lockedCards) {
                            pendingLedgerAfterUnlock = "ব্যয়"
                            unlockCard = "ব্যয়"
                        } else {
                            toggleLedger("ব্যয়")
                        }
                    },
                    onExpandedClick = {
                        if ("ব্যয়" in lockedCards) {
                            pendingBudgetSetupAfterUnlock = true
                            unlockCard = "ব্যয়"
                        } else {
                            showBudgetSetup = true
                        }
                    }
                )
                JomaTotalCard(
                    title = tr("নীট সঞ্চয়", "Net Savings"),
                    amount = moneyText(shown.netSavings),
                    icon = Icons.Default.Savings,
                    color = OrangeMain,
                    bgColor = YellowTint,
                    locked = "সঞ্চয়" in lockedCards,
                    onLockClick = {
                        if ("সঞ্চয়" in lockedCards) unlockCard = "সঞ্চয়" else lockConfirmCard = "সঞ্চয়"
                    },
                    expanded = "সঞ্চয়" in expandedCards,
                    onExpandClick = {
                        expandedCards = if ("সঞ্চয়" in expandedCards) expandedCards - "সঞ্চয়" else expandedCards + "সঞ্চয়"
                    },
                    expandedLines = listOf(
                        tr("মোট সঞ্চয়", "Total Savings") + ": " + moneyText(totalsAll.savings)
                    ),
                    modifier = Modifier.weight(1f),
                    onBodyClick = {
                        if ("সঞ্চয়" in lockedCards) {
                            pendingLedgerAfterUnlock = "সঞ্চয়"
                            unlockCard = "সঞ্চয়"
                        } else {
                            toggleLedger("সঞ্চয়")
                        }
                    }
                )
            }
            if (ledgerPage != null) {
                LedgerSection(
                    viewModel = viewModel,
                    type = ledgerPage!!,
                    ledgerPeriod = ledgerPeriod,
                    onLedgerPeriodChange = { ledgerPeriod = it },
                    periodList = ledgerPeriodList,
                    periodTotal = ledgerPeriodTotal,
                    onBack = { ledgerPage = null },
                    onAdd = onAdd,
                    onShowRecycleBin = { showLedgerRecycleBin = true },
                    onOpenReport = onOpenReport
                )
            }
            if (ledgerPage == null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(OrangeMain, CircleShape))
                        Text(
                            text = "⚡ " + tr("দ্রুত যোগ করুন", "Quick Add"),
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .background(
                                    if (arrangeMode) JomaBlue else LightBlueCard,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { arrangeMode = !arrangeMode }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (arrangeMode) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (arrangeMode) {
                                    "✓ " + tr("সাজানো শেষ", "Done Arranging")
                                } else {
                                    "↓↑ " + tr("কার্ড সাজান", "Arrange Cards")
                                },
                                modifier = Modifier.padding(start = if (arrangeMode) 4.dp else 0.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (arrangeMode) Color.White else JomaBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAddTile("💵", tr("আয়", "Income"), GreenTint, Modifier.weight(1f)) { onAdd("আয়") }
                        QuickAddTile("💸", tr("ব্যয়", "Expense"), RedTint, Modifier.weight(1f)) { onAdd("ব্যয়") }
                        QuickAddTile("💰", tr("সঞ্চয়", "Savings"), YellowTint, Modifier.weight(1f)) { onAdd("সঞ্চয়") }
                        QuickAddTile("🔄", tr("ট্রান্সফার", "Transfer"), PurpleTint, Modifier.weight(1f)) { showTransferDialog = true }
                    }
                }
            }
            }
        }
        val homeSections = if (ledgerPage != null) listOf("category") else orderedVisible
        items(homeSections, key = { it }) { key ->
            val pos = homeSections.indexOf(key)
            when (key) {
                "chart" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(
                    JomaBlue,
                    if (chartMode == "দৈনিক") {
                        tr("📊 আজকের আয়-ব্যয়ের চার্ট", "📊 Today's Income-Expense Chart")
                    } else {
                        tr("📊 মাসিক আয়-ব্যয়ের চার্ট", "📊 Monthly Income-Expense Chart")
                    },
                    trailing = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(LightBlueCard, RoundedCornerShape(16.dp))
                                    .clickable { showAutoDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tr("অটো সেট", "Auto Set"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = JomaBlue
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .background(GreyBg, RoundedCornerShape(16.dp))
                                    .clickable { chartMode = if (chartMode == "দৈনিক") "মাসিক" else "দৈনিক" }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📅",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (chartMode == "দৈনিক") fullDateLabel(todayKey) else monthLabel(monthKey),
                                    modifier = Modifier.padding(start = 4.dp),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GreyBg, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            for (m in listOf("দৈনিক", "মাসিক")) {
                                val selected = chartMode == m
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selected) JomaBlue else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { chartMode = m }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tr(
                                            m + " চার্ট",
                                            if (m == "দৈনিক") "Daily Chart" else "Monthly Chart"
                                        ),
                                        color = if (selected) Color.White else Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                IncomeExpenseBars(chartTotals.income, chartTotals.expense)
                            }
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val prefix = if (chartMode == "দৈনিক") {
                                    tr("আজকের", "Today's")
                                } else {
                                    tr("মাসিক", "Monthly")
                                }
                                ChartTotalBox(prefix + " " + tr("আয়", "Income"), moneyText(chartTotals.income), GreenTint, GreenMain)
                                ChartTotalBox(prefix + " " + tr("ব্যয়", "Expense"), moneyText(chartTotals.expense), RedTint, RedMain)
                                ChartTotalBox(prefix + " " + tr("উদ্বৃত্ত", "Balance"), moneyTextSigned(if (chartTotals.balance >= 0) chartTotals.balance else -chartTotals.balance, chartTotals.balance >= 0), YellowTint, OrangeMain)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        SplitProgressBar(chartTotals.income, chartTotals.expense)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = tr("আয়ের লেনদেন", "Income transactions") + ": " + toBengaliDigits(chartIncomeCount.toString()),
                                fontSize = 12.sp,
                                color = GreyText
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = tr("ব্যয়ের লেনদেন", "Expense transactions") + ": " + toBengaliDigits(chartExpenseCount.toString()),
                                fontSize = 12.sp,
                                color = GreyText
                            )
                        }
                    }
                }
                    }
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < homeSections.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "flow" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(
                    GreenMain,
                    tr("📈 মাসিক ব্যয়ের ফ্লো চার্ট", "📈 Monthly Expense Flow Chart"),
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    flowMonth = YearMonth.parse(flowMonth).minusMonths(1).toString()
                                    selectedDay = 1
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                            }
                            Box(
                                modifier = Modifier
                                    .background(GreyBg, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = monthLabel(flowMonth) + " (" + toBengaliDigits("1") + "–" + toBengaliDigits(flowDays.toString()) + ")",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    val next = YearMonth.parse(flowMonth).plusMonths(1).toString()
                                    if (next <= monthKey) {
                                        flowMonth = next
                                        selectedDay = 1
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FlowStatBox(
                                tr("সর্বোচ্চ ব্যয়", "Highest Expense"),
                                if (flowMax > 0L) {
                                    toBengaliDigits(flowMaxDay.toString()) + " " + tr("তারিখ", "th") + " • " + moneyText(flowMax)
                                } else {
                                    moneyText(0L)
                                },
                                RedTint, RedMain, Modifier.weight(1f),
                                icon = "🔺", titleColor = RedMain
                            )
                            FlowStatBox(
                                tr("সর্বনিম্ন ব্যয়", "Lowest Expense"),
                                if (flowMin > 0L) {
                                    toBengaliDigits(flowMinDay.toString()) + " " + tr("তারিখ", "th") + " • " + moneyText(flowMin)
                                } else {
                                    moneyText(0L)
                                },
                                GreenTint, GreenMain, Modifier.weight(1f),
                                icon = "🟢", titleColor = GreenMain
                            )
                            FlowStatBox(
                                tr("দৈনিক গড়", "Daily Avg"),
                                moneyText(flowAvg),
                                GreyBg, Color.Black, Modifier.weight(1f),
                                icon = "—", titleColor = GreyText
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(GreenTint, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = GreenMain,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = fullDateLabel(selDate),
                                        modifier = Modifier.padding(start = 4.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GreenMain
                                    )
                                }
                                // ওই দিনের ব্যয়ের লেনদেনের গোনা (জমার মতো: "(১ টি ব্যয়)")।
                                val selCount = transactions.count { it.type == "ব্যয়" && it.date == selDate }
                                if (selCount > 0) {
                                    Text(
                                        text = if (selCount == 1) {
                                            tr("(১ টি ব্যয়)", "(1 expense)")
                                        } else {
                                            "(" + toBengaliDigits(selCount.toString()) + " " +
                                                tr("টি ব্যয়)", "expenses)")
                                        },
                                        modifier = Modifier.padding(start = 6.dp),
                                        fontSize = 13.sp,
                                        color = GreyText
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                // বাছাই করা দিন মাসের সর্বোচ্চ ব্যয়ের দিন হলে লাল "বেশি ▲"।
                                val isMaxDay = selValue > 0L && selValue == flowMax
                                Text(
                                    text = if (isMaxDay) {
                                        tr("বেশি", "Highest") + " ▲ " + moneyText(selValue)
                                    } else if (selValue > 0L) {
                                        moneyText(selValue)
                                    } else {
                                        tr("ব্যয় নেই", "No expense") + " " + moneyText(0L)
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = if (isMaxDay) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isMaxDay) RedMain else GreyText
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowLineChart(
                                days = flowDays,
                                values = flowDaily,
                                selectedDay = selDay,
                                avg = flowAvg,
                                onSelectDay = { selectedDay = it }
                            )
                            Row(modifier = Modifier.fillMaxWidth()) {
                                val labels = listOf(1, 5, 10, 15, 20, 25, 30)
                                for ((i, d) in labels.withIndex()) {
                                    Text(
                                        text = if (d <= flowDays) toBengaliDigits(d.toString()) else "",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 12.sp,
                                        color = GreyText,
                                        textAlign = when (i) {
                                            0 -> TextAlign.Start
                                            labels.size - 1 -> TextAlign.End
                                            else -> TextAlign.Center
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 " + tr(
                                    "চার্টে ট্যাপ বা স্লাইড করে যেকোনো দিনের ব্যয় দেখুন",
                                    "Tap or slide on the chart to see any day's expense"
                                ),
                                modifier = Modifier.weight(1f),
                                fontSize = 12.sp,
                                color = GreyText
                            )
                            Text(
                                text = tr("মোট ব্যয়ের দিন", "Expense days") + ": " +
                                    toBengaliDigits(flowExpenseDays.toString()) + "/" +
                                    toBengaliDigits(flowDays.toString()),
                                fontSize = 12.sp,
                                color = GreyText
                            )
                        }
                    }
                }
                    }
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < homeSections.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "category" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                if (ledgerPage == null || ledgerPage == "ব্যয়") {
                CategoryAnalysisCard(
                    type = "ব্যয়",
                    color = JomaBlue,
                    transactions = transactions,
                    today = today,
                    onEditCategories = { t ->
                        HomeNavState.reportType = t
                        onOpenReport()
                    },
                    onSeeAll = { openLedgerAndShow("ব্যয়") }
                )
                }
                if (ledgerPage == null || ledgerPage == "আয়") {
                CategoryAnalysisCard(
                    type = "আয়",
                    color = GreenMain,
                    transactions = transactions,
                    today = today,
                    onEditCategories = { t ->
                        HomeNavState.reportType = t
                        onOpenReport()
                    },
                    onSeeAll = { openLedgerAndShow("আয়") }
                )
                }
                if (ledgerPage == null || ledgerPage == "সঞ্চয়") {
                CategoryAnalysisCard(
                    type = "সঞ্চয়",
                    color = PurpleMain,
                    transactions = transactions,
                    today = today,
                    onEditCategories = { t ->
                        HomeNavState.reportType = t
                        onOpenReport()
                    },
                    onSeeAll = { openLedgerAndShow("সঞ্চয়") }
                )
                }
                    }
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < homeSections.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "due" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(
                    JomaBlue,
                    tr("🤝 বাকি ও ধার (Cash)", "🤝 Due & Loan (Cash)"),
                    trailing = {
                        Text(
                            text = tr("বিস্তারিত দেখুন ›", "See details ›"),
                            modifier = Modifier.clickable { onOpenDue() },
                            fontSize = 13.sp,
                            color = GreyText
                        )
                    }
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DueBox(tr("মোট বাকি", "Total Due"), moneyText(dueTotal), YellowTint, "🛒", Modifier.weight(1f)) { HomeNavState.dueTab = "বাকি"; onOpenDue() }
                        DueBox(tr("ধার নেওয়া (Cash)", "Borrowed (Cash)"), moneyText(borrowed), RedTint, "🔴", Modifier.weight(1f)) { HomeNavState.dueTab = "ধার"; onOpenDue() }
                        DueBox(tr("ধার দেওয়া (Cash)", "Lent (Cash)"), moneyText(lent), GreenTint, "🟢", Modifier.weight(1f)) { HomeNavState.dueTab = "ধার"; onOpenDue() }
                    }
                }
                    }
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < homeSections.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "ledger" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(JomaBlue, tr("📘 নাম অনুযায়ী খাতা", "📘 Ledger by Name"))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = GreyText,
                                modifier = Modifier.size(18.dp)
                            )
                            Row(
                                modifier = Modifier.weight(1f).padding(start = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(GreyBg, RoundedCornerShape(12.dp))
                                        .clickable { pickDate(fromDate ?: today) { fromDate = it } }
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = fromDate?.let { fullDateLabel(it.toString()) }
                                            ?: tr("শুরু থেকে", "From start"),
                                        fontSize = 13.sp,
                                        color = GreyText
                                    )
                                }
                                Text(text = "—", fontSize = 14.sp, color = GreyText)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(GreyBg, RoundedCornerShape(12.dp))
                                        .clickable { pickDate(toDate ?: today) { toDate = it } }
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = toDate?.let { fullDateLabel(it.toString()) }
                                            ?: tr("বর্তমান পর্যন্ত", "Until now"),
                                        fontSize = 13.sp,
                                        color = GreyText
                                    )
                                }
                            }
                            IconButton(
                                onClick = { fromDate = null; toDate = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = GreyText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (t in listOf("আয়", "ব্যয়", "সঞ্চয়")) {
                                val selected = ledgerType == t
                                val tColor = when (t) {
                                    "আয়" -> GreenMain
                                    "ব্যয়" -> RedMain
                                    else -> YellowMain
                                }
                                val tBg = when (t) {
                                    "আয়" -> GreenTint
                                    "ব্যয়" -> RedTint
                                    else -> YellowTint
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selected) tColor else tBg,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { ledgerType = t }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = (when (t) {
                                                "আয়" -> "💰 "
                                                "ব্যয়" -> "💸 "
                                                else -> "🏦 "
                                            }) + tr(
                                                t,
                                                if (t == "আয়") "Income" else if (t == "ব্যয়") "Expense" else "Savings"
                                            ),
                                            modifier = Modifier.padding(
                                                start = if (selected) 4.dp else 0.dp
                                            ),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else Color.Black
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val totalName = when (ledgerType) {
                                "আয়" -> tr("মোট আয়", "Total Income")
                                "ব্যয়" -> tr("মোট ব্যয়", "Total Expense")
                                else -> tr("মোট সঞ্চয়", "Total Savings")
                            }
                            LedgerStatBox(
                                totalName, moneyText(ledgerTotal), Modifier.weight(1f)
                            )
                            LedgerStatBox(
                                tr("লেনদেন", "Transactions"),
                                toBengaliDigits(ledgerList.size.toString()),
                                Modifier.weight(1f),
                                sub = toBengaliDigits(ledgerShown.size.toString()) + " " +
                                    tr("টি নাম/খাত", "names")
                            )
                            LedgerStatBox(
                                tr("সর্বোচ্চ", "Highest"),
                                moneyText(ledgerMax),
                                Modifier.weight(1f),
                                sub = ledgerMaxName
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = ledgerQuery,
                                onValueChange = { ledgerQuery = it },
                                placeholder = {
                                    Text(text = tr("নাম বা খাত লিখে খুঁজুন...", "Search by name..."))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = GreyText
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            if ("সার্চ" in loadAppPrefs(context).lockedSections && !searchUnlocked) {
                                Box(
                                    modifier = Modifier.matchParentSize()
                                        .clickable { showSearchPin = true }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (ledgerShown.isEmpty()) {
                            EmptyState(
                                "📭",
                                tr("এই সময়সীমায় কোনো হিসাব নেই", "No records in this period")
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = tr("নাম / খাত", "Name"),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreyText
                                )
                                Text(
                                    text = tr("লেনদেন", "Count"),
                                    modifier = Modifier.width(64.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreyText,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = tr("মোট", "Total"),
                                    modifier = Modifier.width(88.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreyText,
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            for ((name, amt) in ledgerShown) {
                                val count = ledgerList.count {
                                    (it.payer.ifBlank { it.category }) == name
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        HomeNavState.reportQuery = name
                                        onOpenReport()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "🏷️", fontSize = 14.sp)
                                            Text(
                                                text = name,
                                                modifier = Modifier.padding(start = 6.dp),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .padding(start = 6.dp)
                                                    .background(GreyBg, RoundedCornerShape(10.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tr("খাত", "Ledger"),
                                                    fontSize = 11.sp,
                                                    color = GreyText
                                                )
                                            }
                                        }
                                        Text(
                                            text = toBengaliDigits(count.toString()),
                                            modifier = Modifier.width(64.dp),
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = moneyText(amt),
                                            modifier = Modifier.width(88.dp),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End,
                                            color = if (ledgerType == "ব্যয়") RedMain else GreenMain
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = {
                                            if (ledgerMax > 0L) amt.toFloat() / ledgerMax else 0f
                                        },
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = if (ledgerType == "ব্যয়") RedMain else GreenMain,
                                        trackColor = TrackGrey
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GreyBg, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = tr("মোট", "Total"),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = toBengaliDigits(ledgerList.size.toString()),
                                    modifier = Modifier.width(64.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = moneyText(ledgerTotal),
                                    modifier = Modifier.width(88.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
                    }
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < homeSections.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "budget" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(JomaBlue, tr("🎯 মাসিক বাজেট", "🎯 Monthly Budget"))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        if (budgets.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = tr("মাসের খরচের সীমা ঠিক করুন, সীমা পার হলে সতর্কতা পাবেন", "Set a limit for your monthly spending and get an alert when you exceed it"),
                                    fontSize = 14.sp,
                                    color = GreyText,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )
                                Button(
                                    onClick = { showBudgetSetup = true },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BudgetPillBtnBg,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        text = "+ " + tr("বাজেট সেট করুন", "Set Budget"),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            val totalLimit = budgets.sumOf { it.limitAmount }
                            val totalSpent = budgets.sumOf { expenseByCatMonth[it.category] ?: 0L }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = tr("খরচ", "Spent") + " " + moneyText(totalSpent) +
                                        " / " + moneyText(totalLimit),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = percentOf(totalSpent, totalLimit),
                                    fontSize = 13.sp,
                                    color = GreyText
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = {
                                    if (totalLimit > 0L) {
                                        (totalSpent.toFloat() / totalLimit).coerceIn(0f, 1f)
                                    } else {
                                        0f
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = if (totalSpent > totalLimit) RedMain else GreenMain,
                                trackColor = TrackGrey
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            for (b in budgets.take(3)) {
                                val spent = expenseByCatMonth[b.category] ?: 0L
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = categoryLabel(b.category),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = moneyText(spent) + " / " + moneyText(b.limitAmount),
                                        fontSize = 13.sp,
                                        color = GreyText
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = tr("বিস্তারিত দেখুন ›", "See details ›"),
                                    modifier = Modifier.clickable { onOpenBudget() },
                                    fontSize = 13.sp,
                                    color = JomaBlue
                                )
                            }
                        }
                    }
                }
                    }
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < homeSections.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "accounts" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(
                    GreenMain,
                    tr("💳 অ্যাকাউন্ট ব্যালেন্স", "💳 Account Balance"),
                    trailing = {
                        Button(
                            onClick = { showTransferDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JomaBlue,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🔁 " + tr("ট্রান্সফার", "Transfer"),
                                fontSize = 13.sp
                            )
                        }
                    }
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        if (accountBalances.isEmpty()) {
                            AccountRow(
                                tr("নগদ (হাতে)", "Cash in hand"),
                                tr("হাতে থাকা টাকা", "Cash in hand"),
                                totalsAll.balance
                            )
                        } else {
                            for ((index, pair) in accountBalances.withIndex()) {
                                val (acc, bal) = pair
                                AccountRow(acc.name, accountKindLabel(acc.kind), bal)
                                if (index < accountBalances.size - 1) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(1.dp)
                                            .background(TrackGrey)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(1.dp)
                                .background(TrackGrey)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = tr("আয় বা ব্যয় যোগ করার সময় সঞ্চয়ের কোনো ধরন বেছে নিলে এখানে সেই ধরনের আলাদা ব্যালেন্স দেখা যাবে।", "If you choose a savings type when adding income or expense, the separate balance for that type will appear here."),
                            fontSize = 14.sp,
                            color = GreyText
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GreyBg, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = tr("মোট (নগদ + সব অ্যাকাউন্ট)", "Total (cash + all accounts)"),
                                modifier = Modifier.weight(1f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = moneyText(totalBalance),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                    }
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < homeSections.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "recent" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(JomaBlue, tr("সাম্প্রতিক লেনদেন", "Recent Transactions"))
                if (transactions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        EmptyState(
                            "🧾",
                            tr("এখনো কোনো লেনদেন যোগ করা হয়নি", "No transactions added yet")
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                            val recent = transactions.take(10)
                            for ((index, item) in recent.withIndex()) {
                                RecentTransactionRow(item)
                                if (index < recent.size - 1) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(1.dp)
                                            .background(TrackGrey)
                                    )
                                }
                            }
                        }
                    }
                }
                    }
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < homeSections.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            if (editCatType != null) {
                val type = editCatType!!
                AlertDialog(
                    onDismissRequest = { editCatType = null },
                    title = { Text(text = tr("খাত সম্পাদন করুন", "Edit Categories")) },
                    text = {
                        Column {
                            for (cat in customCats) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cat,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 15.sp
                                    )
                                    IconButton(
                                        onClick = {
                                            val next = customCats - cat
                                            saveCustomCategories(context, type, next)
                                            customCats = next
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = RedMain,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = newCatText,
                                onValueChange = { newCatText = it },
                                label = { Text(text = tr("নতুন খাতের নাম", "New category name")) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val name = newCatText.trim()
                                    if (name.isNotBlank() && name !in allCategories(context, type)) {
                                        val next = customCats + name
                                        saveCustomCategories(context, type, next)
                                        customCats = next
                                    }
                                    newCatText = ""
                                },
                                enabled = newCatText.trim().isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                            ) {
                                Text(text = tr("খাত যোগ করুন", "Add Category"), fontSize = 15.sp)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { editCatType = null }) {
                            Text(text = tr("ঠিক আছে", "OK"))
                        }
                    }
                )
            }
        }
    }
    // লেনদেন অংশের ডায়ালগগুলো রুটে থাকে, LazyColumn-এর ভেতরে নয়।
    if (showLedgerRecycleBin) {
        RecycleBinDialog(
            deleted = deletedTransactions,
            onRestore = { viewModel.restoreTransaction(it) },
            onDeletePermanently = { viewModel.deleteTransactionPermanently(it) },
            onDismiss = { showLedgerRecycleBin = false }
        )
    }
    if (showLedgerAnalysisEdit && ledgerPage != null) {
        val editType = ledgerPage!!
        AnalysisEditDialog(
            type = editType,
            groups = ledgerAnalysisGroups,
            hiddenCats = ledgerHiddenCats,
            onToggle = { cat, show ->
                val key = "$editType|$cat"
                val next = if (show) ledgerHiddenCats - key else ledgerHiddenCats + key
                ledgerHiddenCats = next
                saveHiddenAnalysisCats(context, next)
            },
            onDismiss = { showLedgerAnalysisEdit = false }
        )
    }
    // ডায়ালগগুলো LazyColumn-এর বাইরে: হোমের উপরে থাকলেও কম্পোজ হয়।
    if (showAutoDialog) {
        AutoIncomeDialog(
            prefs = prefs,
            onDismiss = { showAutoDialog = false },
            onSave = { next ->
                saveAppPrefs(context, next)
                prefs = next
                scheduleAutoIncome(context)
                showAutoDialog = false
            }
        )
    }
    // বাজেট সেট ডায়ালগ (মোট ব্যয় কার্ডের নিচের লাইনে চাপ দিলে খোলে)।
    if (showBudgetSetup) {
        BudgetSetupDialog(
            context = context,
            monthKey = monthKey,
            budgets = allBudgets.filter { it.monthKey == monthKey },
            monthTotal = loadMonthlyBudgetTotal(context, monthKey),
            monthDaily = loadMonthlyBudgetDaily(context, monthKey),
            viewModel = viewModel,
            onDismiss = { showBudgetSetup = false },
            onSaved = { _, _ -> showBudgetSetup = false }
        )
    }
    // ট্রান্সফার ডায়ালগ (রুটে থাকে, LazyColumn-এর ভেতরে নয়)।
    if (showTransferDialog) {
        TransferDialog(
            viewModel = viewModel,
            onDismiss = { showTransferDialog = false },
            onSaved = { showTransferDialog = false }
        )
    }
    if (lockConfirmCard != null) {
        val confirmKey = lockConfirmCard!!
        val cardTitleBn = when (confirmKey) {
            "আয়" -> "মোট আয়"
            "ব্যয়" -> "মোট ব্যয়"
            else -> "নীট সঞ্চয়"
        }
        val cardTitleEn = when (confirmKey) {
            "আয়" -> "Total Income"
            "ব্যয়" -> "Total Expense"
            else -> "Net Savings"
        }
        Dialog(onDismissRequest = { lockConfirmCard = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.border(2.dp, OrangeMain, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(72.dp)
                            .background(YellowTint, CircleShape)
                            .border(2.dp, OrangeMain, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔒", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = tr("লক করবেন কি?", "Lock this?"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JomaBlue,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr("আপনি কি '" + cardTitleBn + "' লক করতে চান?", "Do you want to lock '" + cardTitleEn + "'?"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("লক করলে এই অংশের টাকার হিসাব গোপন থাকবে এবং দেখতে পিন বা বায়োমেট্রিক লাগবে।", "Once locked, this amount will stay hidden and viewing it will need a PIN or biometric."),
                        fontSize = 13.sp,
                        color = GreyText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { lockConfirmCard = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreyBg,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = tr("না, থাকুক", "No, keep it"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                setSectionLocked(confirmKey, true)
                                lockConfirmCard = null
                                if (prefs.pin.isBlank()) showPinDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                        ) {
                            Text(text = tr("হ্যাঁ, লক করুন", "Yes, lock it"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
    // লক করা কার্ড খুলতে পিন ডায়ালগ (তালা চাপলে আর সরাসরি আনলক হয় না)।
    if (unlockCard != null) {
        val unlockKey = unlockCard!!
        PinUnlockDialog(
            prefs = loadAppPrefs(context),
            onPrefsChange = { next -> saveAppPrefs(context, next); prefs = next },
            onUnlocked = {
                setSectionLocked(unlockKey, false)
                unlockCard = null
                // কার্ডের গায়ে চাপ দিয়ে পিন খুললে লেনদেন অংশ খুলবে।
                if (pendingLedgerAfterUnlock == unlockKey) {
                    openLedger(unlockKey)
                }
                pendingLedgerAfterUnlock = null
                // নিচের লাইনে চাপ দিয়ে পিন খুললে অটো সেট ডায়ালগ খুলবে।
                if (pendingAutoDialogAfterUnlock) {
                    showAutoDialog = true
                }
                pendingAutoDialogAfterUnlock = false
                // মোট ব্যয়ের নিচের লাইনে চাপ দিয়ে পিন খুললে বাজেট সেট ডায়ালগ খুলবে।
                if (pendingBudgetSetupAfterUnlock) {
                    showBudgetSetup = true
                }
                pendingBudgetSetupAfterUnlock = false
            },
            onDismiss = { unlockCard = null; pendingLedgerAfterUnlock = null; pendingAutoDialogAfterUnlock = false; pendingBudgetSetupAfterUnlock = false }
        )
    }
    // লক করার পর পিন না থাকলে "পিন ও গোপনীয়তা" ডায়ালগ নিজে থেকে খুলবে (জমার মতো)।
    if (showPinDialog) {
        PinPrivacyDialog(
            prefs = prefs,
            onPrefsChange = { next -> saveAppPrefs(context, next); prefs = next },
            onDismiss = { showPinDialog = false }
        )
    }
    if (showSearchPin) {
        PinUnlockDialog(
            prefs = loadAppPrefs(context),
            onPrefsChange = { next -> saveAppPrefs(context, next); prefs = next },
            onUnlocked = {
                searchUnlocked = true
                showSearchPin = false
            },
            onDismiss = { showSearchPin = false }
        )
    }
}

@Composable
private fun AutoIncomeDialog(
    prefs: AppPrefs,
    onDismiss: () -> Unit,
    onSave: (AppPrefs) -> Unit
) {
    val context = LocalContext.current
    val incomeChips = listOf(
        Triple("বেতন", "Salary", "💼"),
        Triple("ব্যবসা", "Business", "🏢"),
        Triple("পণ্য বিক্রি", "Product Sales", "📦"),
        Triple("ভাড়া", "Rent", "🏠"),
        Triple("সেবা আয়", "Service Income", "🔔"),
        Triple("ভাতা", "Allowance", "🎗️"),
        Triple("অনলাইন ইনকাম", "Online Income", "💻")
    )
    val dateChips = listOf(1, 5, 7, 10, 15, 20, 25, 30)

    var monthlyAmount by remember { mutableStateOf(if (prefs.autoSalaryAmount > 0L) prefs.autoSalaryAmount.toString() else "0") }
    var monthlyDay by remember { mutableStateOf(prefs.autoSalaryDay) }
    var monthlyDayText by remember { mutableStateOf(prefs.autoSalaryDay.toString()) }
    var monthlyCategory by remember { mutableStateOf(prefs.autoSalaryCategory) }
    var monthlyCustomOpen by remember { mutableStateOf(false) }
    var monthlyCustom by remember { mutableStateOf("") }
    var monthlyEnabled by remember { mutableStateOf(prefs.autoSalaryEnabled) }

    var dailyAmount by remember { mutableStateOf(if (prefs.autoDailyAmount > 0L) prefs.autoDailyAmount.toString() else "0") }
    var dailyCategory by remember { mutableStateOf(prefs.autoDailyCategory) }
    var dailyCustomOpen by remember { mutableStateOf(false) }
    var dailyCustom by remember { mutableStateOf("") }
    var dailyEnabled by remember { mutableStateOf(prefs.autoDailyEnabled) }

    fun saveDialog() {
        val mAmt = monthlyAmount.filter { it.isDigit() }.toLongOrNull() ?: 0L
        val mDay = monthlyDayText.filter { it.isDigit() }.toIntOrNull()?.coerceIn(1, 31) ?: 1
        val mCat = if (monthlyCustomOpen && monthlyCustom.isNotBlank()) monthlyCustom.trim() else monthlyCategory
        val dAmt = dailyAmount.filter { it.isDigit() }.toLongOrNull() ?: 0L
        val dCat = if (dailyCustomOpen && dailyCustom.isNotBlank()) dailyCustom.trim() else dailyCategory
        val existing = loadCustomCategories(context, "আয়")
        var next = existing
        if (mCat !in allCategories(context, "আয়")) next = next + mCat
        if (dCat !in allCategories(context, "আয়")) next = next + dCat
        if (next != existing) saveCustomCategories(context, "আয়", next)
        onSave(
            prefs.copy(
                autoSalaryEnabled = monthlyEnabled,
                autoSalaryAmount = mAmt,
                autoSalaryDay = mDay,
                autoSalaryCategory = mCat,
                autoDailyEnabled = dailyEnabled,
                autoDailyAmount = dAmt,
                autoDailyCategory = dCat
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚡ " + tr("অটো আয় সেট (মাসিক ও দৈনিক)", "Auto Income Set (Monthly & Daily)"),
                        modifier = Modifier.weight(1f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).background(GreyBg, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = GreyText)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(AutoDialogGreenBg, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💼 " + tr("মাসিক বেতন / আয় সেট", "Monthly Salary / Income Set"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LedgerGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tr("নির্দিষ্ট তারিখে মাসিক বেতন বা আয় অটো যোগ হবে এবং ড্যাশবোর্ডে মোট আয়ের নিচে দেখা যাবে", "On the selected date, the monthly salary or income will be added automatically and shown below the total income on the dashboard"),
                        fontSize = 13.sp,
                        color = GreyText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("কত টাকা (৳)", "Amount (৳)"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = monthlyAmount,
                                onValueChange = { monthlyAmount = it.filter { ch -> ch.isDigit() }.take(9) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("কত তারিখে (১–৩১)", "On which date (1–31)"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = monthlyDayText,
                                onValueChange = {
                                    monthlyDayText = it.filter { ch -> ch.isDigit() }.take(2)
                                    monthlyDayText.toIntOrNull()?.let { d -> monthlyDay = d.coerceIn(1, 31) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = tr("তারিখ", "Date") + ":", fontSize = 13.sp, color = GreyText)
                        for (d in dateChips) {
                            val selected = monthlyDay == d
                            Box(
                                modifier = Modifier.size(38.dp)
                                    .background(if (selected) LedgerGreen else Color.White, CircleShape)
                                    .border(1.dp, if (selected) LedgerGreen else TrackGrey, CircleShape)
                                    .clickable {
                                        monthlyDay = d
                                        monthlyDayText = d.toString()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = toBengaliDigits(d.toString()),
                                    fontSize = 14.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else Color.Black
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for ((bn, en, icon) in incomeChips) {
                            val selected = !monthlyCustomOpen && monthlyCategory == bn
                            AutoCategoryChip(tr(bn, en), icon, selected) {
                                monthlyCategory = bn
                                monthlyCustomOpen = false
                            }
                        }
                        AutoCategoryChip(
                            "+ " + tr("অন্যান্য", "Others"),
                            "＋",
                            monthlyCustomOpen
                        ) { monthlyCustomOpen = !monthlyCustomOpen }
                    }
                    if (monthlyCustomOpen) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = monthlyCustom,
                            onValueChange = { monthlyCustom = it },
                            label = { Text(text = tr("নতুন খাতের নাম", "New category name")) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("উক্ত তারিখে অটো যোগ করুন", "Auto add on that date"),
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = monthlyEnabled,
                            onCheckedChange = { monthlyEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = LedgerGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = TrackGrey
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(AutoDialogBlueBg, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "📅 " + tr("ব্যবসায়ীদের জন্য প্রতিদিনের আয় সেট", "Daily Income Set for Businesses"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tr("প্রতিদিনের নির্ধারিত আয় সেট করুন — ড্যাশবোর্ডের মোট আয় হাইড বাটনে ক্লিক করলে দেখা যাবে", "Set a fixed daily income — tap the eye button on the dashboard total income to see it"),
                        fontSize = 13.sp,
                        color = GreyText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr("প্রতিদিনের আয় কত টাকা (৳)", "Daily income amount (৳)"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = dailyAmount,
                        onValueChange = { dailyAmount = it.filter { ch -> ch.isDigit() }.take(9) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for ((bn, en, icon) in incomeChips) {
                            val selected = !dailyCustomOpen && dailyCategory == bn
                            AutoCategoryChip(tr(bn, en), icon, selected) {
                                dailyCategory = bn
                                dailyCustomOpen = false
                            }
                        }
                        AutoCategoryChip(
                            "+ " + tr("অন্যান্য", "Others"),
                            "＋",
                            dailyCustomOpen
                        ) { dailyCustomOpen = !dailyCustomOpen }
                    }
                    if (dailyCustomOpen) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = dailyCustom,
                            onValueChange = { dailyCustom = it },
                            label = { Text(text = tr("নতুন খাতের নাম", "New category name")) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("প্রতিদিন অটো যোগ করুন", "Auto add every day"),
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = dailyEnabled,
                            onCheckedChange = { dailyEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = LedgerGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = TrackGrey
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreyBg,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = tr("বাতিল", "Cancel"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { saveDialog() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LedgerGreen)
                    ) {
                        Text(text = tr("সংরক্ষণ করুন", "Save"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

