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
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    "বাজার", "বিল", "খাবার", "যাতায়াত", "ধর্মীয় দান", "হাতখরচ",
    "তেল/গ্যাস", "মার্কেট/শপিং", "গাড়ি ভাড়া", "রেস্টুরেন্ট",
    "বাসা ভাড়া", "দোকান ভাড়া", "অন্যান্য"
)

// হোম থেকে রিপোর্টে যাওয়ার সময় খোঁজার শব্দ পাঠানোর জায়গা।
object HomeNavState {
    var reportQuery: String = ""
}

private fun totalsOf(list: List<Transaction>): Totals {
    var income = 0L
    var expense = 0L
    var savings = 0L
    for (t in list) {
        when (t.type) {
            "আয়" -> income += t.amount
            "ব্যয়" -> expense += t.amount
            "সঞ্চয়" -> savings += t.amount
        }
    }
    return Totals(income, expense, savings)
}

private fun filterByPeriod(list: List<Transaction>, period: String, today: LocalDate): List<Transaction> {
    val todayKey = today.toString()
    return when (period) {
        "দৈনিক" -> list.filter { it.date == todayKey }
        "সাপ্তাহিক" -> list.filter { it.date >= today.minusDays(6).toString() }
        "বার্ষিক" -> list.filter { it.date.startsWith(todayKey.substring(0, 4)) }
        else -> list.filter { it.date.startsWith(todayKey.substring(0, 7)) }
    }
}

private fun groupByCategory(list: List<Transaction>, type: String): Map<String, Long> {
    val map = linkedMapOf<String, Long>()
    for (t in list) {
        if (t.type == type) {
            map[t.category] = (map[t.category] ?: 0L) + t.amount
        }
    }
    return map.entries.sortedByDescending { it.value }.associate { it.key to it.value }
}

private fun percentOf(part: Long, total: Long): String {
    if (total <= 0L) return "০%"
    return toBengaliDigits((part * 100 / total).toString()) + "%"
}

// লোকাল ব্যাকআপ ফাইল (Downloads/আয়-ব্যয়/backup.json) আছে কি না।
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

private fun categoryIcon(category: String): String = when (category) {
    "বাজার" -> "🛒"
    "বিল" -> "🧾"
    "খাবার" -> "🍲"
    "যাতায়াত" -> "🚌"
    "ধর্মীয় দান" -> "🤲"
    "হাতখরচ" -> "💵"
    "তেল/গ্যাস" -> "⛽"
    "মার্কেট/শপিং" -> "🛍️"
    "গাড়ি ভাড়া" -> "🚗"
    "রেস্টুরেন্ট" -> "🍽️"
    "বাসা ভাড়া" -> "🏠"
    "দোকান ভাড়া" -> "🏪"
    "বেতন", "মাসিক বেতন", "সাপ্তাহিক বেতন" -> "💼"
    "দৈনিক মজুরি" -> "👷"
    "সঞ্চয়" -> "🏺"
    else -> "🏷️"
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
    val shownTotal = shown.income + shown.expense + shown.savings

    // মোট কার্ডের তালা আর ড্রপডাউন (কোন কার্ড লক/খোলা আছে)।
    var lockedCards by remember { mutableStateOf(setOf<String>()) }
    var expandedCards by remember { mutableStateOf(setOf<String>()) }

    // ব্যালেন্স লুকানো/দেখানো (চোখের আইকন)।
    var balanceHidden by rememberSaveable { mutableStateOf(false) }

    // ডাটা ব্যাকআপ পিল: আসল backup.json থাকলেই done।
    var backupDone by remember { mutableStateOf(backupFileExists()) }

    // কার্ড সাজান: সেকশনের ক্রম ফোনে সেভ থাকে, সব সেকশন সবসময় দেখায়।
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

    // অটো আয় সেট ডায়ালগ।
    var showAutoDialog by remember { mutableStateOf(false) }

    // মাসিক চার্ট: দৈনিক/মাসিক টগল।
    var chartMode by remember { mutableStateOf("মাসিক") }
    val chartTx = if (chartMode == "দৈনিক") {
        transactions.filter { it.date == todayKey }
    } else {
        transactions.filter { it.date.startsWith(monthKey) }
    }
    val chartTotals = totalsOf(chartTx)
    val chartIncomeCount = chartTx.count { it.type == "আয়" }
    val chartExpenseCount = chartTx.count { it.type == "ব্যয়" }

    // ফ্লো চার্ট: মাস বদলানো যায়, দিনে ট্যাপ করলে ওই দিনের ব্যয় দেখায়।
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

    // বাকি ও ধার: আসল Due ডেটা থেকে তিনটা বাক্স।
    val pendingDue = dueItems.filter { !it.isPaid }
    val dueTotal = pendingDue.filter { it.kind == "বাকি" }.sumOf { it.amount }
    val borrowed = pendingDue.filter { it.kind == "ধার" && it.direction == "নিয়েছি" }.sumOf { it.amount }
    val lent = pendingDue.filter { it.kind == "ধার" && it.direction == "দিয়েছি" }.sumOf { it.amount }

    // নাম অনুযায়ী খাতা।
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

    // খাত সম্পাদন ডায়ালগ।
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

    // অ্যাকাউন্ট ব্যালেন্স: লেনদেন থেকে হিসাব।
    val accountBalances = accounts.map { acc ->
        acc to (acc.balance + transactions.filter { it.account == acc.name }.sumOf {
            when (it.type) {
                "আয়" -> it.amount
                "ব্যয়", "সঞ্চয়" -> -it.amount
                else -> 0L
            }
        })
    }
    val totalBalance = if (accountBalances.isEmpty()) {
        totalsAll.balance
    } else {
        accountBalances.sumOf { it.second }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().background(PageBg).padding(horizontal = 12.dp)
    ) {
        item {
            // ব্যালেন্স কার্ড — হালকা নীল
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
                                    text = tr("মোট ব্যালেন্স", "Total Balance"),
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
                        ExpenseShareDonut(shown.expense, shownTotal)
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
            // সময়ের ট্যাব — সিলেক্টেড নীল
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
            // মোট কার্ড — তালা আর ড্রপডাউন সহ
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
                        lockedCards = if ("আয়" in lockedCards) lockedCards - "আয়" else lockedCards + "আয়"
                    },
                    expanded = "আয়" in expandedCards,
                    onExpandClick = {
                        expandedCards = if ("আয়" in expandedCards) expandedCards - "আয়" else expandedCards + "আয়"
                    },
                    expandedLines = listOf(
                        tr("মাসিক সেট", "Monthly set") + ": " + moneyText(prefs.autoSalaryAmount),
                        tr("অটো সেট থেকে নির্ধারণ করুন", "Set from Auto Set")
                    ),
                    modifier = Modifier.weight(1f)
                )
                JomaTotalCard(
                    title = tr("মোট ব্যয়", "Total Expense"),
                    amount = moneyText(shown.expense),
                    icon = Icons.Default.ArrowUpward,
                    color = RedMain,
                    bgColor = RedTint,
                    locked = "ব্যয়" in lockedCards,
                    onLockClick = {
                        lockedCards = if ("ব্যয়" in lockedCards) lockedCards - "ব্যয়" else lockedCards + "ব্যয়"
                    },
                    expanded = "ব্যয়" in expandedCards,
                    onExpandClick = {
                        expandedCards = if ("ব্যয়" in expandedCards) expandedCards - "ব্যয়" else expandedCards + "ব্যয়"
                    },
                    expandedLines = listOf(
                        tr("মোট বাজেট", "Total Budget") + ": " + moneyText(totalBudgetAll),
                        tr("বাজেট সেকশনে সেট করুন", "Set in Budget section")
                    ),
                    modifier = Modifier.weight(1f)
                )
                JomaTotalCard(
                    title = tr("নীট সঞ্চয়", "Net Savings"),
                    amount = moneyText(shown.netSavings),
                    icon = Icons.Default.Savings,
                    color = OrangeMain,
                    bgColor = YellowTint,
                    locked = "সঞ্চয়" in lockedCards,
                    onLockClick = {
                        lockedCards = if ("সঞ্চয়" in lockedCards) lockedCards - "সঞ্চয়" else lockedCards + "সঞ্চয়"
                    },
                    expanded = "সঞ্চয়" in expandedCards,
                    onExpandClick = {
                        expandedCards = if ("সঞ্চয়" in expandedCards) expandedCards - "সঞ্চয়" else expandedCards + "সঞ্চয়"
                    },
                    expandedLines = listOf(
                        tr("মোট সঞ্চয়", "Total Savings") + ": " + moneyText(totalsAll.savings)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            // দ্রুত যোগ করুন + কার্ড সাজান
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
                            Icon(
                                imageVector = if (arrangeMode) Icons.Default.Check else Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = if (arrangeMode) Color.White else JomaBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (arrangeMode) {
                                    "✓ " + tr("সাজানো শেষ", "Done Arranging")
                                } else {
                                    tr("কার্ড সাজান", "Arrange Cards")
                                },
                                modifier = Modifier.padding(start = 4.dp),
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
                        QuickAddTile("🔄", tr("ট্রান্সফার", "Transfer"), PurpleTint, Modifier.weight(1f)) { onAdd("ট্রান্সফার") }
                    }
                }
            }
        }
        items(orderedVisible, key = { it }) { key ->
            val pos = orderedVisible.indexOf(key)
            when (key) {
                "chart" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(
                    JomaBlue,
                    if (chartMode == "দৈনিক") {
                        tr("আজকের আয়-ব্যয়ের চার্ট", "Today's Income-Expense Chart")
                    } else {
                        tr("মাসিক আয়-ব্যয়ের চার্ট", "Monthly Income-Expense Chart")
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
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
                            Spacer(modifier = Modifier.width(8.dp))
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
                        Spacer(modifier = Modifier.height(10.dp))
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
                                ChartTotalBox(prefix + " " + tr("উদ্বৃত্ত", "Balance"), moneyText(chartTotals.balance), YellowTint, OrangeMain)
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
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < orderedVisible.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "flow" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(GreenMain, tr("মাসিক ব্যয়ের ফ্লো চার্ট", "Monthly Expense Flow Chart"))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                        Spacer(modifier = Modifier.height(10.dp))
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
                                RedTint, RedMain, Modifier.weight(1f)
                            )
                            FlowStatBox(
                                tr("সর্বনিম্ন ব্যয়", "Lowest Expense"),
                                if (flowMin > 0L) {
                                    toBengaliDigits(flowMinDay.toString()) + " " + tr("তারিখ", "th") + " • " + moneyText(flowMin)
                                } else {
                                    moneyText(0L)
                                },
                                GreenTint, GreenMain, Modifier.weight(1f)
                            )
                            FlowStatBox(
                                tr("দৈনিক গড়", "Daily Avg"),
                                moneyText(flowAvg),
                                GreyBg, Color.Black, Modifier.weight(1f)
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
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = if (selValue > 0L) {
                                        moneyText(selValue)
                                    } else {
                                        tr("ব্যয় নেই", "No expense") + " " + moneyText(0L)
                                    },
                                    fontSize = 13.sp,
                                    color = GreyText
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowLineChart(
                                days = flowDays,
                                values = flowDaily,
                                selectedDay = selDay,
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
                            horizontalArrangement = Arrangement.End
                        ) {
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
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < orderedVisible.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "category" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                CategoryAnalysisCard(
                    type = "ব্যয়",
                    color = RedMain,
                    transactions = transactions,
                    today = today,
                    onEditCategories = { t ->
                        customCats = loadCustomCategories(context, t)
                        newCatText = ""
                        editCatType = t
                    },
                    onSeeAll = onOpenReport
                )
                CategoryAnalysisCard(
                    type = "আয়",
                    color = GreenMain,
                    transactions = transactions,
                    today = today,
                    onEditCategories = { t ->
                        customCats = loadCustomCategories(context, t)
                        newCatText = ""
                        editCatType = t
                    },
                    onSeeAll = onOpenReport
                )
                CategoryAnalysisCard(
                    type = "সঞ্চয়",
                    color = OrangeMain,
                    transactions = transactions,
                    today = today,
                    onEditCategories = { t ->
                        customCats = loadCustomCategories(context, t)
                        newCatText = ""
                        editCatType = t
                    },
                    onSeeAll = onOpenReport
                )
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < orderedVisible.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "due" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(
                    JomaBlue,
                    tr("বাকি ও ধার (Cash)", "Due & Loan (Cash)"),
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
                        DueBox(tr("মোট বাকি", "Total Due"), moneyText(dueTotal), YellowTint, "🛒", Modifier.weight(1f)) { onOpenDue() }
                        DueBox(tr("ধার নেওয়া (Cash)", "Borrowed (Cash)"), moneyText(borrowed), RedTint, "🔴", Modifier.weight(1f)) { onOpenDue() }
                        DueBox(tr("ধার দেওয়া (Cash)", "Lent (Cash)"), moneyText(lent), GreenTint, "🟢", Modifier.weight(1f)) { onOpenDue() }
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
                                if (pos < orderedVisible.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "ledger" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(JomaBlue, tr("নাম অনুযায়ী খাতা", "Ledger by Name"))
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
                                            text = tr(
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
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < orderedVisible.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "budget" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(JomaBlue, tr("মাসিক বাজেট", "Monthly Budget"))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        if (budgets.isEmpty()) {
                            Button(
                                onClick = onOpenBudget,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreyBg,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(
                                    text = "+ " + tr("বাজেট সেট করুন", "Set Budget"),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
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
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < orderedVisible.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "accounts" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                JomaSectionHeader(
                    GreenMain,
                    tr("অ্যাকাউন্ট ব্যালেন্স", "Account Balance"),
                    trailing = {
                        Button(
                            onClick = { onAdd("ট্রান্সফার") },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JomaBlue,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = tr("ট্রান্সফার", "Transfer"),
                                modifier = Modifier.padding(start = 4.dp),
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
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < orderedVisible.size - 1) {
                                    MoveButton(Icons.Default.ArrowDownward) { moveSection(key, 1) }
                                }
                            }
                        }
                    }
                }
                "recent" -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
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
                        if (arrangeMode) {
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .padding(top = 20.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (pos > 0) {
                                    MoveButton(Icons.Default.ArrowUpward) { moveSection(key, -1) }
                                }
                                if (pos < orderedVisible.size - 1) {
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
            // খাত সম্পাদন ডায়ালগ
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
        }
    }
}

@Composable
private fun JomaTotalCard(
    title: String,
    amount: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    locked: Boolean,
    onLockClick: () -> Unit,
    expanded: Boolean,
    onExpandClick: () -> Unit,
    expandedLines: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (locked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = GreyText,
                    modifier = Modifier.size(16.dp).clickable { onLockClick() }
                )
            }
            Text(
                text = if (locked) "৳ ••••" else amount,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier.size(26.dp).background(Color.White, CircleShape)
                        .clickable { onExpandClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = GreyText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(4.dp))
                for ((index, line) in expandedLines.withIndex()) {
                    Text(
                        text = line,
                        fontSize = 12.sp,
                        fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == 0) Color.Black else GreyText
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAddTile(
    emoji: String,
    label: String,
    bgColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(38.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )
            }
            Text(
                text = label,
                modifier = Modifier.padding(top = 6.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ChartTotalBox(
    label: String,
    amount: String,
    bgColor: Color,
    accent: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(bgColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = GreyText)
        Text(
            text = amount,
            modifier = Modifier.padding(top = 2.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
private fun FlowStatBox(
    title: String,
    value: String,
    bgColor: Color,
    accent: Color,
    modifier: Modifier
) {
    Column(
        modifier = modifier.background(bgColor, RoundedCornerShape(12.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = GreyText,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CategoryAnalysisCard(
    type: String,
    color: Color,
    transactions: List<Transaction>,
    today: LocalDate,
    onEditCategories: (String) -> Unit,
    onSeeAll: () -> Unit
) {
    var catPeriod by remember { mutableStateOf("মাসিক") }
    val typeName = when (type) {
        "আয়" -> tr("আয়ের", "Income")
        "ব্যয়" -> tr("ব্যয়ের", "Expense")
        else -> tr("সঞ্চয়ের", "Savings")
    }
    val totalLabel = when (type) {
        "আয়" -> tr("মোট আয়", "Total Income")
        "ব্যয়" -> tr("মোট ব্যয়", "Total Expense")
        else -> tr("মোট সঞ্চয়", "Total Savings")
    }
    val emptyText = when (type) {
        "আয়" -> tr("এই সময়সীমায় কোনো আয়ের হিসাব নেই", "No income records in this period")
        "ব্যয়" -> tr("এই সময়সীমায় কোনো ব্যয়ের হিসাব নেই", "No expense records in this period")
        else -> tr("এই সময়সীমায় কোনো সঞ্চয়ের হিসাব নেই", "No savings records in this period")
    }
    val groups = groupByCategory(filterByPeriod(transactions, catPeriod, today), type)
    val total = groups.values.sum()
    JomaSectionHeader(color, typeName + " " + tr("খাত অনুযায়ী বিশ্লেষণ", "Category Analysis"))
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
                Row(
                    modifier = Modifier.weight(1f)
                        .background(GreyBg, RoundedCornerShape(12.dp))
                        .padding(3.dp)
                ) {
                    for (p in listOf("দৈনিক", "সাপ্তাহিক", "মাসিক", "বার্ষিক")) {
                        val selected = catPeriod == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selected) color else Color.Transparent,
                                    RoundedCornerShape(9.dp)
                                )
                                .clickable { catPeriod = p }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = periodLabel(p),
                                color = if (selected) Color.White else Color.Black,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Text(
                    text = tr("সম্পাদন করুন", "Edit"),
                    modifier = Modifier.padding(start = 10.dp)
                        .clickable { onEditCategories(type) },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = JomaBlue
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RingDonut(
                    if (total > 0L) color else TrackGrey,
                    periodLabel(catPeriod),
                    totalLabel,
                    moneyText(total)
                )
                if (groups.isEmpty()) {
                    Box(modifier = Modifier.weight(1f)) {
                        EmptyState("📭", emptyText)
                    }
                } else {
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        for ((cat, amt) in groups.entries.take(5)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(30.dp)
                                        .background(GreyBg, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = categoryIcon(cat), fontSize = 15.sp)
                                }
                                Text(
                                    text = categoryLabel(cat),
                                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = moneyText(amt),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            LinearProgressIndicator(
                                progress = {
                                    if (total > 0L) amt.toFloat() / total else 0f
                                },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = color,
                                trackColor = TrackGrey
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
            if (groups.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = tr("সব দেখুন", "See all"),
                        modifier = Modifier.padding(top = 6.dp).clickable { onSeeAll() },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JomaBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun DueBox(
    label: String,
    amount: String,
    bgColor: Color,
    icon: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 14.sp)
                Text(
                    text = label,
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = amount,
                modifier = Modifier.padding(top = 6.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LedgerStatBox(
    title: String,
    value: String,
    modifier: Modifier,
    sub: String = ""
) {
    Column(
        modifier = modifier.background(GreyBg, RoundedCornerShape(12.dp)).padding(10.dp)
    ) {
        Text(text = title, fontSize = 12.sp, color = GreyText)
        Text(
            text = value,
            modifier = Modifier.padding(top = 2.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        if (sub.isNotBlank()) {
            Text(text = sub, fontSize = 12.sp, color = GreyText)
        }
    }
}

@Composable
private fun AccountRow(name: String, sub: String, balance: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(GreyBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = GreenMain,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = sub, fontSize = 12.sp, color = GreyText)
        }
        Text(
            text = moneyText(balance),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = GreenMain
        )
    }
}

@Composable
private fun RecentTransactionRow(item: Transaction) {
    val positive = item.type == "আয়"
    val tint = when (item.type) {
        "আয়" -> GreenTint
        "ব্যয়" -> RedTint
        else -> BlueTint
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(tint, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = categoryIcon(item.category), fontSize = 22.sp)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text(
                text = categoryLabel(item.category) +
                    if (item.note.isNotBlank()) " • " + item.note else "",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = fullDateLabel(item.date) + " • " + timeLabel(item.time),
                fontSize = 12.sp,
                color = GreyText
            )
        }
        Text(
            text = moneyTextSigned(item.amount, positive),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (positive) GreenMain else RedMain
        )
    }
}

@Composable
fun TransactionRow(item: Transaction, onDelete: () -> Unit) {
    val positive = item.type == "আয়"
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = categoryLabel(item.category), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = dateLabel(item.date) + " • " + item.account, fontSize = 12.sp, color = GreyText)
                if (item.note.isNotBlank()) {
                    Text(text = item.note, fontSize = 13.sp, color = GreyText)
                }
            }
            Text(
                text = moneyTextSigned(item.amount, positive),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (positive) GreenMain else RedMain
            )
            Text(
                text = "✕",
                modifier = Modifier.padding(start = 12.dp).clickable { onDelete() },
                fontSize = 16.sp,
                color = GreyText
            )
        }
    }
}

// অটো আয় সেট ডায়ালগ (মাসিক ও দৈনিক) — জমা খরচের মতো।
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
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = GreyText)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(GreenTint, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💼 " + tr("মাসিক বেতন / আয় সেট", "Monthly Salary / Income Set"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenMain
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
                                    .background(if (selected) GreenMain else Color.White, CircleShape)
                                    .border(1.dp, if (selected) GreenMain else TrackGrey, CircleShape)
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
                                checkedTrackColor = GreenMain,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = TrackGrey
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(GreyBg, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "📅 " + tr("ব্যবসায়ীদের জন্য প্রতিদিনের আয় সেট", "Daily Income Set for Businesses"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
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
                                checkedTrackColor = GreenMain,
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
                        colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                    ) {
                        Text(text = tr("সংরক্ষণ করুন", "Save"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoCategoryChip(
    label: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(if (selected) GreenMain else Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, if (selected) GreenMain else TrackGrey, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 13.sp)
        Text(
            text = label,
            modifier = Modifier.padding(start = 4.dp),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Color.Black
        )
    }
}

// কার্ড সাজান মোডে কার্ডের উপরে-নিচে সরানোর নীল গোল বোতাম।
@Composable
private fun MoveButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(40.dp).background(JomaBlue, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}
