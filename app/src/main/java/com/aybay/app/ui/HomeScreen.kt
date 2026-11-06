package com.aybay.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aybay.app.data.Totals
import com.aybay.app.data.Transaction
import java.time.LocalDate

val incomeCategories = listOf("মাসিক বেতন", "দৈনিক মজুরি", "সাপ্তাহিক বেতন", "অন্যান্য")
val expenseCategories = listOf(
    "বাজার", "বিল", "খাবার", "যাতায়াত", "ধর্মীয় দান", "হাতখরচ",
    "তেল/গ্যাস", "মার্কেট/শপিং", "গাড়ি ভাড়া", "রেস্টুরেন্ট",
    "বাসা ভাড়া", "দোকান ভাড়া", "অন্যান্য"
)

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

private fun percentOf(part: Long, total: Long): String {
    if (total <= 0L) return "০%"
    return toBengaliDigits((part * 100 / total).toString()) + "%"
}

@Composable
fun HomeScreen(viewModel: AyBayViewModel, onAdd: (String) -> Unit, onOpenDue: () -> Unit) {
    val transactions by viewModel.transactions.collectAsState()
    val dueItems by viewModel.dueItems.collectAsState()
    var period by remember { mutableStateOf("মাসিক") }
    val today = LocalDate.now()
    val todayKey = today.toString()
    val monthKey = todayKey.substring(0, 7)
    val yearKey = todayKey.substring(0, 4)
    val weekStart = today.minusDays(6).toString()
    val monthTotals by viewModel.totalsOfMonth(monthKey).collectAsState(initial = Totals())
    val expenseByCat by viewModel.categoryTotals(monthKey, "ব্যয়").collectAsState(initial = emptyMap())

    // সময়ের ট্যাব আসল ডাটা দিয়েই হিসাব হয়: দৈনিক=আজ, সাপ্তাহিক=শেষ ৭ দিন,
    // মাসিক=এই মাস, বার্ষিক=এই বছর।
    val shown = when (period) {
        "দৈনিক" -> totalsOf(transactions.filter { it.date == todayKey })
        "সাপ্তাহিক" -> totalsOf(transactions.filter { it.date >= weekStart })
        "বার্ষিক" -> totalsOf(transactions.filter { it.date.startsWith(yearKey) })
        else -> monthTotals
    }
    val shownTotal = shown.income + shown.expense + shown.savings

    // আজকের হিসাব (আসল লেনদেন থেকে)।
    val todayTx = transactions.filter { it.date == todayKey }
    val todayTotals = totalsOf(todayTx)

    // এই মাসের ব্যয়ের পরিসংখ্যান (আসল লেনদেন থেকে)।
    val monthExpenseTx = transactions.filter { it.date.startsWith(monthKey) && it.type == "ব্যয়" }
    val monthExpenseTotal = monthExpenseTx.sumOf { it.amount }
    val expenseDays = monthExpenseTx.map { it.date }.distinct().size
    val highestExpense = monthExpenseTx.maxOfOrNull { it.amount } ?: 0L
    val lowestExpense = monthExpenseTx.minOfOrNull { it.amount } ?: 0L
    val avgExpense = if (expenseDays > 0) monthExpenseTotal / expenseDays.toLong() else 0L

    val pendingDue = dueItems.filter { !it.isPaid }
    val dueTotal = pendingDue.sumOf { it.amount }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            // ব্যালেন্স কার্ড — গ্র্যাডিয়েন্ট হিরো কার্ড
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(GreenDark, GreenMain)))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr("বর্তমান মোট নগদ", "Current Cash"),
                                    fontSize = 14.sp,
                                    color = Color(0xFFD1FAE5)
                                )
                                Text(
                                    text = moneyText(shown.balance),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (viewModel.sync.isAvailable) tr("লাইভ সিঙ্ক চালু", "Live Sync On") else tr("অফলাইন", "Offline"),
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            DonutChart(
                                shown.income,
                                shown.expense,
                                shown.savings,
                                centerTop = moneyText(shown.balance),
                                centerBottom = tr("ব্যালেন্স", "Balance")
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LegendRow(
                            GreenTint, tr("আয়", "Income"), moneyText(shown.income),
                            percentOf(shown.income, shownTotal), Color.White
                        )
                        LegendRow(
                            RedMain, tr("ব্যয়", "Expense"), moneyText(shown.expense),
                            percentOf(shown.expense, shownTotal), Color.White
                        )
                        LegendRow(
                            BlueTint, tr("সঞ্চয়", "Savings"), moneyText(shown.savings),
                            percentOf(shown.savings, shownTotal), Color.White
                        )
                    }
                }
            }
            // সময়ের ট্যাব
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
                                    if (selected) GreenDark else Color.Transparent,
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
            // মোট কার্ড
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TotalCard(tr("মোট আয়", "Total Income"), moneyText(shown.income), GreenMain, Modifier.weight(1f), GreenTint)
                TotalCard(tr("মোট ব্যয়", "Total Expense"), moneyText(shown.expense), RedMain, Modifier.weight(1f), RedTint)
                TotalCard(tr("নীট সঞ্চয়", "Net Savings"), moneyText(shown.netSavings), BlueMain, Modifier.weight(1f), BlueTint)
            }
            // আজকের হিসাব
            SectionTitle(tr("আজকের হিসাব", "Today's Summary"))
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
                    StatBox(tr("আজকের আয়", "Today's Income"), moneyText(todayTotals.income), GreenTint, GreenMain, Modifier.weight(1f))
                    StatBox(tr("আজকের ব্যয়", "Today's Expense"), moneyText(todayTotals.expense), RedTint, RedMain, Modifier.weight(1f))
                    StatBox(tr("আজকের উদ্বৃত্ত", "Today's Balance"), moneyText(todayTotals.balance), BlueTint, BlueMain, Modifier.weight(1f))
                }
            }
            // দ্রুত যোগ করুন
            SectionTitle(tr("দ্রুত যোগ করুন", "Quick Add"))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddButton(Icons.Default.ArrowUpward, tr("আয়", "Income"), GreenMain, GreenTint, Modifier.weight(1f)) { onAdd("আয়") }
                QuickAddButton(Icons.Default.ArrowDownward, tr("ব্যয়", "Expense"), RedMain, RedTint, Modifier.weight(1f)) { onAdd("ব্যয়") }
                QuickAddButton(Icons.Default.Savings, tr("সঞ্চয়", "Savings"), BlueMain, BlueTint, Modifier.weight(1f)) { onAdd("সঞ্চয়") }
                QuickAddButton(Icons.Default.SwapHoriz, tr("ট্রান্সফার", "Transfer"), GreenDark, GreenLight, Modifier.weight(1f)) { onAdd("ট্রান্সফার") }
            }
            // মাসিক ব্যয়ের চার্ট
            SectionTitle(tr("মাসিক ব্যয়ের চার্ট", "Monthly Expense Chart"))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(tr("সর্বোচ্চ ব্যয়", "Highest Expense"), moneyText(highestExpense), RedTint, RedMain, Modifier.weight(1f))
                        StatBox(tr("সর্বনিম্ন ব্যয়", "Lowest Expense"), moneyText(lowestExpense), GreenTint, GreenMain, Modifier.weight(1f))
                        StatBox(tr("দৈনিক গড়", "Daily Avg"), moneyText(avgExpense), BlueTint, BlueMain, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (expenseByCat.isEmpty()) {
                        EmptyState("📊", tr("এখনো কোনো ব্যয় যোগ করা হয়নি", "No expense added yet"))
                    } else {
                        BarChart(expenseByCat.entries.map { it.key to it.value }, RedMain)
                    }
                }
            }
            // খাত অনুযায়ী বিশ্লেষণ
            SectionTitle(tr("ব্যয়ের খাত অনুযায়ী বিশ্লেষণ", "Expense by Category"))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (expenseByCat.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MultiDonutChart(
                                emptyList(),
                                tr("মোট ব্যয়", "Total Expense"),
                                moneyText(0L)
                            )
                            EmptyState("📊", tr("এখনো কোনো ব্যয় যোগ করা হয়নি", "No expense added yet"))
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val catItems = expenseByCat.entries.mapIndexed { index, entry ->
                                CategoryColors[index % CategoryColors.size] to entry.value
                            }
                            MultiDonutChart(
                                catItems,
                                tr("মোট ব্যয়", "Total Expense"),
                                moneyText(monthExpenseTotal)
                            )
                            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                expenseByCat.entries.forEachIndexed { index, entry ->
                                    val color = CategoryColors[index % CategoryColors.size]
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(color, RoundedCornerShape(5.dp))
                                        )
                                        Text(
                                            text = categoryLabel(entry.key),
                                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = moneyText(entry.value) + " · " + percentOf(entry.value, monthExpenseTotal),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // বাকি ও ধার — খালি থাকলেও দেখায়
            SectionTitle(tr("বাকি ও ধার", "Due & Loan"))
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenDue() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("মোট বাকি", "Total Due"),
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = moneyText(dueTotal),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dueTotal > 0L) RedMain else GreenMain
                    )
                    Text(
                        text = "›",
                        modifier = Modifier.padding(start = 10.dp),
                        fontSize = 20.sp,
                        color = GreyText
                    )
                }
            }
            // সাম্প্রতিক লেনদেন
            SectionTitle(tr("সাম্প্রতিক লেনদেন", "Recent Transactions"))
        }
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    EmptyState("🧾", tr("এখনো কোনো লেনদেন যোগ করা হয়নি", "No transactions added yet"))
                }
            }
        } else {
            items(transactions.take(20)) { item ->
                TransactionRow(item, onDelete = { viewModel.deleteTransaction(item) })
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun QuickAddButton(
    icon: ImageVector,
    label: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color)
            Text(
                text = label,
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
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
