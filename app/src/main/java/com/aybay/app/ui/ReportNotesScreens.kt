package com.aybay.app.ui

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aybay.app.data.DueItem
import com.aybay.app.data.Note
import com.aybay.app.data.Transaction
import java.time.LocalDate

// খাতের মূল অংশ ("মূল — উপখাত" থেকে মূল)।
private fun mainCategoryOf(item: Transaction): String =
    item.category.substringBefore(" — ")

// সার্চ: নাম/খাত/নোট/প্রদানকারী/পরিমাণ — বাংলা অঙ্কেও মেলে।
private fun matchesReportSearch(item: Transaction, query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.trim()
    return item.category.contains(q, ignoreCase = true) ||
        item.note.contains(q, ignoreCase = true) ||
        item.payer.contains(q, ignoreCase = true) ||
        item.amount.toString().contains(q) ||
        toBengaliDigits(item.amount.toString()).contains(q)
}

// সারিতে দেখানোর নাম: "মূল • উপখাত" (উপখাত থাকলে)।
private fun displayCategoryName(category: String): String {
    val main = category.substringBefore(" — ")
    val sub = category.substringAfter(" — ", "")
    return if (sub.isBlank()) categoryLabel(main)
    else categoryLabel(main) + " • " + categoryLabel(sub)
}

// ধরন ড্রপডাউনের মান -> দেখানোর লেখা।
private fun typeLabel(value: String): String = when (value) {
    "আয়" -> tr("শুধু আয়", "Only Income")
    "ব্যয়" -> tr("শুধু ব্যয়", "Only Expense")
    "সঞ্চয়" -> tr("শুধু সঞ্চয়", "Only Savings")
    "বাকি/ধার" -> tr("শুধু বাকি/ধার", "Only Due/Loan")
    else -> tr("সব ধরনের", "All types")
}

@Composable
private fun ReportDropdown(
    selected: String,
    options: List<String>,
    display: (String) -> String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(GreyBg, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = display(selected), modifier = Modifier.weight(1f), fontSize = 15.sp)
            Text(text = "▾", fontSize = 13.sp, color = GreyText)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (opt in options) {
                DropdownMenuItem(
                    text = { Text(text = display(opt)) },
                    onClick = { expanded = false; onSelect(opt) }
                )
            }
        }
    }
}

// তালিকা ও প্রিভিউয়ের লেনদেনের সারি: রঙিন টিন্টের আইকন-বর্গ +
// মোটা নাম + নিচে "তারিখ • সময়" + ডানে টাকা + টাকার নিচে ✏️/🗑️।
@Composable
private fun ReportTransactionRow(
    item: Transaction,
    darkIcon: Boolean,
    useCard: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isTransfer = item.type == "ট্রান্সফার"
    val positive = item.type == "আয়" ||
        (item.type == "সঞ্চয়" && item.direction != "উত্তোলন")
    val tint = when (item.type) {
        "আয়" -> GreenTint
        "ব্যয়" -> RedTint
        "সঞ্চয়" -> YellowTint
        else -> BlueTint
    }
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(46.dp)
                    .background(
                        if (darkIcon) Color(0xFF1F2937) else tint,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = categoryIcon(mainCategoryOf(item)), fontSize = 22.sp)
            }
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    text = if (isTransfer) {
                        tr("ট্রান্সফার", "Transfer") + ": " +
                            transferDisplayLabel(item.account) + " → " +
                            transferDisplayLabel(item.payer)
                    } else displayCategoryName(item.category),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = fullDateLabel(item.date) + " • " + timeLabel(item.time),
                    fontSize = 12.sp,
                    color = GreyText
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isTransfer) moneyText(item.amount)
                    else moneyTextSigned(item.amount, positive),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTransfer) JomaBlue
                    else if (positive) GreenMain else RedMain
                )
                Row(Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "✏️",
                        fontSize = 15.sp,
                        modifier = Modifier.clickable { onEdit() }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "🗑️",
                        fontSize = 15.sp,
                        modifier = Modifier.clickable { onDelete() }
                    )
                }
            }
        }
    }
    if (useCard) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) { content() }
    } else {
        content()
    }
}

// রিপোর্ট ট্যাবে বাকি/ধারের নামের সারি (ট্যাপে বিস্তারিত খোলে)।
@Composable
private fun DebtReportGroupRow(
    name: String,
    count: Int,
    amount: Long,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🏷️", fontSize = 18.sp)
        Text(
            text = name,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
        Text(
            text = toBengaliDigits(count.toString()),
            fontSize = 15.sp,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = moneyText(amount),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ReportScreen(viewModel: AyBayViewModel, onEdit: (Transaction) -> Unit) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val dueItems by viewModel.dueItems.collectAsState()
    val deleted by viewModel.deletedTransactions.collectAsState()
    var tab by remember { mutableStateOf("list") }
    // হোমের "নাম অনুযায়ী খাতা" থেকে নামে ট্যাপ করে এলে ওই নাম দিয়ে খোঁজা শুরু হয়।
    var query by remember { mutableStateOf(HomeNavState.reportQuery) }
    HomeNavState.reportQuery = ""
    // বিশ্লেষণ কার্ডের "সম্পাদন করুন" থেকে এলে ওই ধরন দিয়ে ফিল্টার শুরু হয় (জমার মতো)।
    var listType by remember { mutableStateOf(if (HomeNavState.reportType.isNotBlank()) HomeNavState.reportType else "সব") }
    HomeNavState.reportType = ""
    var listCat by remember { mutableStateOf("সব খাত") }
    var showBin by remember { mutableStateOf(false) }
    var repType by remember { mutableStateOf("সব") }
    var repCat by remember { mutableStateOf("সব খাত") }
    var repStart by remember { mutableStateOf<LocalDate?>(null) }
    var repEnd by remember { mutableStateOf<LocalDate?>(null) }
    var repQuery by remember { mutableStateOf("") }
    var reportMessage by remember { mutableStateOf("") }
    var debtDetailName by remember { mutableStateOf<String?>(null) }

    // খাত ড্রপডাউনের তালিকা: আয়/ব্যয়/সঞ্চয়ের সব খাত (কাস্টম খাতসহ)।
    val catOptions = remember {
        (JOMA_INCOME_CATS + expenseCategories + JOMA_SAVINGS_TYPES +
            allCategories(context, "আয়") + allCategories(context, "ব্যয়") +
            allCategories(context, "সঞ্চয়"))
            .filter { it != "সঞ্চয়" }.distinct()
    }
    val catDisplay: (String) -> String = {
        if (it == "সব খাত") tr("সব খাত", "All categories") else categoryLabel(it)
    }

    // তালিকা ট্যাবের ফিল্টার করা লেনদেন (সব তারিখ)।
    val listShown = transactions.filter { item ->
        (listType == "সব" || item.type == listType) &&
            (listCat == "সব খাত" || mainCategoryOf(item) == listCat) &&
            matchesReportSearch(item, query)
    }.sortedByDescending { it.date + it.time }

    // রিপোর্ট ট্যাবের ফিল্টার করা লেনদেন।
    val repFiltered = transactions.filter { item ->
        (repType == "সব" || item.type == repType) &&
            (repCat == "সব খাত" || mainCategoryOf(item) == repCat) &&
            (repStart == null || item.date >= repStart.toString()) &&
            (repEnd == null || item.date <= repEnd.toString()) &&
            matchesReportSearch(item, repQuery)
    }.sortedByDescending { it.date + it.time }
    val repIncome = repFiltered.filter { it.type == "আয়" }.sumOf { it.amount }
    val repExpense = repFiltered.filter { it.type == "ব্যয়" }.sumOf { it.amount }
    // নীট সঞ্চয় = জমা − উত্তোলন।
    val repNetSavings = repFiltered.filter { it.type == "সঞ্চয়" }
        .sumOf { if (it.direction == "উত্তোলন") -it.amount else it.amount }
    // হাতে নগদ = ফিল্টার করা আয় − ব্যয়।
    val repCash = repIncome - repExpense

    // রিপোর্ট ট্যাবে বাকি/ধার: নামে খুঁজে বাকি থাকা নামগুলো।
    val isDebtMode = repType == "বাকি/ধার"
    val debtFiltered = dueItems.filter { item ->
        (repStart == null || item.date >= repStart.toString()) &&
            (repEnd == null || item.date <= repEnd.toString()) &&
            (repQuery.isBlank() ||
                item.name.contains(repQuery, ignoreCase = true) ||
                item.description.contains(repQuery, ignoreCase = true) ||
                item.note.contains(repQuery, ignoreCase = true))
    }
    val debtOwe = debtFiltered.filter { it.direction == "নিয়েছি" }.sumOf { dueRemaining(it) }
    val debtGet = debtFiltered.filter { it.direction == "দিয়েছি" }.sumOf { dueRemaining(it) }
    val debtGroupsOwe = debtFiltered.filter { it.direction == "নিয়েছি" }
        .groupBy { it.name }
        .mapValues { (_, list) -> list.sumOf { dueRemaining(it) } }
        .filter { it.value > 0L }.entries.sortedByDescending { it.value }
    val debtGroupsGet = debtFiltered.filter { it.direction == "দিয়েছি" }
        .groupBy { it.name }
        .mapValues { (_, list) -> list.sumOf { dueRemaining(it) } }
        .filter { it.value > 0L }.entries.sortedByDescending { it.value }

    fun pickReportDate(initial: LocalDate?, onPick: (LocalDate) -> Unit) {
        val base = initial ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, y, m, d -> onPick(LocalDate.of(y, m + 1, d)) },
            base.year, base.monthValue - 1, base.dayOfMonth
        ).show()
    }

    fun printReport() {
        val title = "আয়-ব্যয়: Expense Tracker"
        if (isDebtMode) {
            val summary = listOf(
                tr("বাকি/ধার নেওয়া", "Due/Loan taken") + ": " + moneyText(debtOwe),
                tr("বাকি/ধার দেওয়া", "Due/Loan given") + ": " + moneyText(debtGet)
            )
            val rows = debtGroupsOwe.map { (n, a) ->
                n + " — " + tr("নেওয়া", "Taken") + ": " + a.toString()
            } + debtGroupsGet.map { (n, a) ->
                n + " — " + tr("দেওয়া", "Given") + ": " + a.toString()
            }
            val file = exportReportPdf(context, title, summary, rows, "report-debt-" + todayString() + ".pdf")
            reportMessage = if (file != null) {
                printPdfFile(context, file)
                tr("প্রিন্ট প্রিভিউ খুলছে", "Opening print preview")
            } else tr("PDF তৈরি হয়নি", "PDF failed")
        } else {
            val summary = listOf(
                tr("মোট আয়", "Total income") + ": " + moneyText(repIncome),
                tr("মোট ব্যয়", "Total expense") + ": " + moneyText(repExpense),
                tr("নীট সঞ্চয়", "Net savings") + ": " + moneyText(repNetSavings),
                tr("বর্তমান হাতে নগদ (ক্যাশ)", "Current cash in hand") + ": " + moneyText(repCash)
            )
            val rows = repFiltered.map { item ->
                val sign = if (item.type == "আয়") "+" else if (item.type == "ব্যয়") "−" else ""
                item.date + "  " + displayCategoryName(item.category) + "  " + sign + item.amount.toString()
            }
            val file = exportReportPdf(context, title, summary, rows, "report-" + todayString() + ".pdf")
            reportMessage = if (file != null) {
                printPdfFile(context, file)
                tr("প্রিন্ট প্রিভিউ খুলছে", "Opening print preview")
            } else tr("PDF তৈরি হয়নি", "PDF failed")
        }
    }

    fun shareReport() {
        val text = buildString {
            appendLine("📊 আয়-ব্যয়: Expense Tracker")
            if (isDebtMode) {
                appendLine(tr("বাকি/ধার নেওয়া", "Due/Loan taken") + ": " + moneyText(debtOwe))
                appendLine(tr("বাকি/ধার দেওয়া", "Due/Loan given") + ": " + moneyText(debtGet))
                for ((n, a) in debtGroupsOwe) appendLine(n + ": " + moneyText(a) + " (" + tr("নেওয়া", "Taken") + ")")
                for ((n, a) in debtGroupsGet) appendLine(n + ": " + moneyText(a) + " (" + tr("দেওয়া", "Given") + ")")
            } else {
                appendLine(tr("মোট আয়", "Total income") + ": " + moneyText(repIncome))
                appendLine(tr("মোট ব্যয়", "Total expense") + ": " + moneyText(repExpense))
                appendLine(tr("নীট সঞ্চয়", "Net savings") + ": " + moneyText(repNetSavings))
                appendLine(tr("বর্তমান হাতে নগদ (ক্যাশ)", "Current cash in hand") + ": " + moneyText(repCash))
                for (item in repFiltered) {
                    val sign = if (item.type == "আয়") "+" else if (item.type == "ব্যয়") "−" else ""
                    appendLine(item.date + " • " + displayCategoryName(item.category) + " • " + sign + moneyText(item.amount))
                }
            }
        }
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(share, tr("শেয়ার করুন", "Share")))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            // উপরের ২ ট্যাব।
            item {
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp)
                        .background(Color.White, RoundedCornerShape(16.dp)).padding(6.dp)
                ) {
                    for ((key, label) in listOf(
                        "list" to tr("📜 লেনদেনের তালিকা", "📜 Transaction List"),
                        "report" to tr("📊 রিপোর্ট ও পিডিএফ", "📊 Report & PDF")
                    )) {
                        val selected = tab == key
                        Box(
                            Modifier.weight(1f)
                                .background(
                                    if (selected) JomaBlue else Color.White,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { tab = key }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color.White else GreyText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            if (tab == "list") {
                item {
                    JomaSectionHeader(
                        dotColor = JomaBlue,
                        title = tr("লেনদেনের তালিকা", "Transaction List")
                    ) {
                        Box(
                            Modifier.background(Color.White, RoundedCornerShape(18.dp))
                                .border(1.dp, TrackGrey, RoundedCornerShape(18.dp))
                                .clickable { showBin = true }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🗑️ " + tr("রিসাইকেল বিন", "Recycle Bin"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        label = { Text("🔍 " + tr("সার্চ করুন — নাম, নোট, খাত, পরিমাণ...", "Search — name, note, category, amount..."), fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportDropdown(
                            selected = listType,
                            options = listOf("সব", "আয়", "ব্যয়", "সঞ্চয়"),
                            display = { typeLabel(it) },
                            onSelect = { listType = it },
                            modifier = Modifier.weight(1f)
                        )
                        ReportDropdown(
                            selected = listCat,
                            options = listOf("সব খাত") + catOptions,
                            display = catDisplay,
                            onSelect = { listCat = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = toBengaliDigits(listShown.size.toString()) + " " +
                            tr("টি লেনদেন পাওয়া গেছে", "transactions found"),
                        fontSize = 13.sp,
                        color = GreyText,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                if (listShown.isEmpty()) {
                    item { EmptyRow(tr("কোনো লেনদেন পাওয়া যায়নি", "No transactions found")) }
                } else {
                    items(listShown) { item ->
                        ReportTransactionRow(
                            item = item,
                            darkIcon = false,
                            useCard = true,
                            onEdit = { onEdit(item) },
                            onDelete = { viewModel.deleteTransaction(item) }
                        )
                    }
                }
            } else {
                item {
                    JomaSectionHeader(
                        dotColor = JomaBlue,
                        title = tr("রিপোর্ট তৈরি করুন", "Create Report")
                    )
                    Text(
                        text = tr("ধরন", "Type"),
                        fontSize = 13.sp,
                        color = GreyText,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                    ReportDropdown(
                        selected = repType,
                        options = listOf("সব", "আয়", "ব্যয়", "সঞ্চয়", "বাকি/ধার"),
                        display = { typeLabel(it) },
                        onSelect = { repType = it }
                    )
                    if (!isDebtMode) {
                        Text(
                            text = tr("খাত", "Category"),
                            fontSize = 13.sp,
                            color = GreyText,
                            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                        )
                        ReportDropdown(
                            selected = repCat,
                            options = listOf("সব খাত") + catOptions,
                            display = catDisplay,
                            onSelect = { repCat = it }
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = tr("শুরুর তারিখ", "Start date"),
                                fontSize = 13.sp,
                                color = GreyText,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Box(
                                Modifier.fillMaxWidth().background(GreyBg, RoundedCornerShape(12.dp))
                                    .clickable { pickReportDate(repStart) { repStart = it } }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = repStart?.let { fullDateLabel(it.toString()) }
                                        ?: "mm/dd/yyyy",
                                    fontSize = 15.sp,
                                    color = if (repStart == null) GreyText else Color.Black
                                )
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = tr("শেষ তারিখ", "End date"),
                                fontSize = 13.sp,
                                color = GreyText,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Box(
                                Modifier.fillMaxWidth().background(GreyBg, RoundedCornerShape(12.dp))
                                    .clickable { pickReportDate(repEnd) { repEnd = it } }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = repEnd?.let { fullDateLabel(it.toString()) }
                                        ?: "mm/dd/yyyy",
                                    fontSize = 15.sp,
                                    color = if (repEnd == null) GreyText else Color.Black
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = repQuery,
                        onValueChange = { repQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        label = { Text("🔍 " + tr("নাম, নোট, খাত, পরিমাণ...", "Name, note, category, amount..."), fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (isDebtMode) {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TotalCard(
                                title = tr("বাকি/ধার নেওয়া", "Due/Loan taken"),
                                amount = moneyText(debtOwe),
                                color = RedMain,
                                modifier = Modifier.weight(1f),
                                bgColor = RedTint
                            )
                            TotalCard(
                                title = tr("বাকি/ধার দেওয়া", "Due/Loan given"),
                                amount = moneyText(debtGet),
                                color = GreenMain,
                                modifier = Modifier.weight(1f),
                                bgColor = GreenTint
                            )
                        }
                        Text(
                            text = toBengaliDigits((debtGroupsOwe.size + debtGroupsGet.size).toString()) + " " +
                                tr("টি নাম পাওয়া গেছে", "names found"),
                            fontSize = 13.sp,
                            color = GreyText,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    } else {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TotalCard(
                                title = tr("মোট আয়", "Total income"),
                                amount = moneyText(repIncome),
                                color = GreenMain,
                                modifier = Modifier.weight(1f),
                                bgColor = GreenTint
                            )
                            TotalCard(
                                title = tr("মোট ব্যয়", "Total expense"),
                                amount = moneyText(repExpense),
                                color = RedMain,
                                modifier = Modifier.weight(1f),
                                bgColor = RedTint
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TotalCard(
                                title = tr("নীট সঞ্চয়", "Net savings"),
                                amount = moneyText(repNetSavings),
                                color = YellowMain,
                                modifier = Modifier.weight(1f),
                                bgColor = YellowTint
                            )
                            TotalCard(
                                title = tr("বর্তমান হাতে নগদ (ক্যাশ)", "Current cash in hand"),
                                amount = moneyText(repCash),
                                color = JomaBlue,
                                modifier = Modifier.weight(1f),
                                bgColor = BlueTint
                            )
                        }
                        Text(
                            text = toBengaliDigits(repFiltered.size.toString()) + " " +
                                tr("টি লেনদেন পাওয়া গেছে", "transactions found"),
                            fontSize = 13.sp,
                            color = GreyText,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { printReport() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                        ) {
                            Text(
                                text = "🖨️ " + tr("প্রিন্ট / পিডিএফ", "Print / PDF"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { shareReport() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534))
                        ) {
                            Text(
                                text = "📤 " + tr("শেয়ার (WhatsApp/ইমেইল)", "Share (WhatsApp/Email)"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (reportMessage.isNotBlank()) {
                        Text(
                            text = reportMessage,
                            fontSize = 13.sp,
                            color = GreyText,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                if (isDebtMode) {
                    if (debtGroupsOwe.isNotEmpty()) {
                        item {
                            SectionTitle(
                                tr("🔴 বাকি/ধার নেওয়া", "🔴 Due/Loan taken")
                            )
                        }
                        items(debtGroupsOwe) { (name, amount) ->
                            val count = debtFiltered.count { it.name == name && it.direction == "নিয়েছি" }
                            DebtReportGroupRow(
                                name = name,
                                count = count,
                                amount = amount,
                                color = RedMain,
                                onClick = { debtDetailName = name }
                            )
                        }
                    }
                    if (debtGroupsGet.isNotEmpty()) {
                        item {
                            SectionTitle(
                                tr("🟢 বাকি/ধার দেওয়া", "🟢 Due/Loan given")
                            )
                        }
                        items(debtGroupsGet) { (name, amount) ->
                            val count = debtFiltered.count { it.name == name && it.direction == "দিয়েছি" }
                            DebtReportGroupRow(
                                name = name,
                                count = count,
                                amount = amount,
                                color = GreenMain,
                                onClick = { debtDetailName = name }
                            )
                        }
                    }
                    if (debtGroupsOwe.isEmpty() && debtGroupsGet.isEmpty()) {
                        item { EmptyRow(tr("এই সময়ে কোনো বাকি/ধার নেই", "No due/loan in this period")) }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = BlueTint),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                JomaSectionHeader(
                                    dotColor = JomaBlue,
                                    title = tr("প্রিভিউ", "Preview")
                                )
                                if (repFiltered.isEmpty()) {
                                    EmptyRow(tr("কোনো লেনদেন পাওয়া যায়নি", "No transactions found"))
                                } else {
                                    for (item in repFiltered) {
                                        ReportTransactionRow(
                                            item = item,
                                            darkIcon = true,
                                            useCard = false,
                                            onEdit = { onEdit(item) },
                                            onDelete = { viewModel.deleteTransaction(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
        if (showBin) RecycleBinDialog(
            deleted = deleted,
            onRestore = { viewModel.restoreTransaction(it) },
            onDeletePermanently = { viewModel.deleteTransactionPermanently(it) },
            onDismiss = { showBin = false }
        )
        debtDetailName?.let { name ->
            DueDetailDialog(
                name = name,
                items = debtFiltered.filter { it.name == name },
                viewModel = viewModel,
                onDismiss = { debtDetailName = null }
            )
        }
    }
}


@Composable
fun NotesScreen(viewModel: AyBayViewModel) {
    val notes by viewModel.notes.collectAsState()
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var urgent by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Text(
                text = tr("দ্রুত নোট", "Quick Notes"),
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showForm = !showForm },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) {
                Text(text = tr("নতুন নোট লিখুন", "Write New Note"), fontSize = 15.sp)
            }
            if (showForm) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(text = tr("শিরোনাম", "Title")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text(text = tr("নোটের বিবরণ", "Note details")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("অতি জরুরি নোট", "Urgent note") + ": " + if (urgent) tr("চালু", "On") else tr("বন্ধ", "Off"),
                        modifier = Modifier.clickable { urgent = !urgent },
                        fontSize = 14.sp,
                        color = GreenDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.addNote(Note(title = title, body = body, urgent = urgent))
                            title = ""
                            body = ""
                            urgent = false
                            showForm = false
                        },
                        enabled = title.isNotBlank() || body.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                    ) {
                        Text(text = tr("সংরক্ষণ করুন", "Save"), fontSize = 15.sp)
                    }
                }
            }
            SectionTitle(tr("নোটের তালিকা", "Notes List"))
        }
        if (notes.isEmpty()) {
            item { EmptyRow(tr("কোনো নোট নেই", "No notes yet")) }
        } else {
            items(notes) { note ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = note.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = note.body, fontSize = 14.sp, color = GreyText)
                        if (note.urgent) {
                            Text(text = tr("অতি জরুরি", "Urgent"), fontSize = 12.sp, color = RedMain)
                        }
                    }
                    Text(
                        text = "✕",
                        modifier = Modifier.clickable { viewModel.deleteNote(note) },
                        fontSize = 16.sp,
                        color = GreyText
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
