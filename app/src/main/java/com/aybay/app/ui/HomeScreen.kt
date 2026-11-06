package com.aybay.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aybay.app.data.Transaction
import java.time.LocalDate

val incomeCategories = listOf("মাসিক বেতন", "দৈনিক মজুরি", "সাপ্তাহিক বেতন", "অন্যান্য")
val expenseCategories = listOf(
    "বাজার", "বিল", "খাবার", "যাতায়াত", "ধর্মীয় দান", "হাতখরচ",
    "তেল/গ্যাস", "মার্কেট/শপিং", "গাড়ি ভাড়া", "রেস্টুরেন্ট",
    "বাসা ভাড়া", "দোকান ভাড়া", "অন্যান্য"
)

@Composable
fun HomeScreen(viewModel: AyBayViewModel, onAdd: (String) -> Unit, onOpenDue: () -> Unit) {
    val totals by viewModel.totalsAll.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val dueItems by viewModel.dueItems.collectAsState()
    var period by remember { mutableStateOf("মাসিক") }
    val monthKey = LocalDate.now().toString().substring(0, 7)
    val monthTotals by viewModel.totalsOfMonth(monthKey).collectAsState(initial = com.aybay.app.data.Totals())
    val expenseByCat by viewModel.categoryTotals(monthKey, "ব্যয়").collectAsState(initial = emptyMap())
    val shown = when (period) {
        "দৈনিক" -> totals
        "বার্ষিক" -> totals
        else -> monthTotals
    }
    val pendingDue = dueItems.filter { !it.isPaid }
    val dueTotal = pendingDue.sumOf { it.amount }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            // ব্যালেন্স কার্ড
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GreenLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = tr("বর্তমান মোট নগদ", "Current Cash"), fontSize = 14.sp, color = GreyText)
                            Text(text = moneyText(shown.balance), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GreenDark)
                            Text(
                                text = if (viewModel.sync.isAvailable) tr("লাইভ সিঙ্ক চালু", "Live Sync On") else tr("অফলাইন", "Offline"),
                                fontSize = 12.sp,
                                color = GreenDark
                            )
                        }
                        DonutChart(shown.income, shown.expense, shown.savings)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LegendRow(GreenMain, tr("আয়", "Income"), moneyText(shown.income))
                    LegendRow(RedMain, tr("ব্যয়", "Expense"), moneyText(shown.expense))
                    LegendRow(BlueMain, tr("সঞ্চয়", "Savings"), moneyText(shown.savings))
                }
            }
            // সময়ের ট্যাব
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (p in listOf("দৈনিক", "সাপ্তাহিক", "মাসিক", "বার্ষিক")) {
                    val selected = period == p
                    Text(
                        text = periodLabel(p),
                        modifier = Modifier
                            .background(
                                if (selected) GreenDark else GreyBg,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .clickable { period = p },
                        color = if (selected) Color.White else Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
            // মোট কার্ড
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TotalCard(tr("মোট আয়", "Total Income"), moneyText(shown.income), GreenMain, Modifier.weight(1f))
                TotalCard(tr("মোট ব্যয়", "Total Expense"), moneyText(shown.expense), RedMain, Modifier.weight(1f))
                TotalCard(tr("নীট সঞ্চয়", "Net Savings"), moneyText(shown.netSavings), BlueMain, Modifier.weight(1f))
            }
            // দ্রুত যোগ করুন
            SectionTitle(tr("দ্রুত যোগ করুন", "Quick Add"))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddButton(Icons.Default.ArrowUpward, tr("আয়", "Income"), GreenMain, Modifier.weight(1f)) { onAdd("আয়") }
                QuickAddButton(Icons.Default.ArrowDownward, tr("ব্যয়", "Expense"), RedMain, Modifier.weight(1f)) { onAdd("ব্যয়") }
                QuickAddButton(Icons.Default.Savings, tr("সঞ্চয়", "Savings"), BlueMain, Modifier.weight(1f)) { onAdd("সঞ্চয়") }
                QuickAddButton(Icons.Default.SwapHoriz, tr("ট্রান্সফার", "Transfer"), GreenDark, Modifier.weight(1f)) { onAdd("ট্রান্সফার") }
            }
            // মাসিক ব্যয়ের চার্ট
            SectionTitle(tr("মাসিক ব্যয়ের চার্ট", "Monthly Expense Chart"))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (expenseByCat.isEmpty()) {
                        EmptyRow(tr("এখনো কোনো ব্যয় যোগ করা হয়নি", "No expense added yet"))
                    } else {
                        BarChart(expenseByCat.entries.map { it.key to it.value }, RedMain)
                    }
                }
            }
            // খাত অনুযায়ী বিশ্লেষণ
            SectionTitle(tr("ব্যয়ের খাত অনুযায়ী বিশ্লেষণ", "Expense by Category"))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (expenseByCat.isEmpty()) {
                        EmptyRow(tr("এখনো কোনো ব্যয় যোগ করা হয়নি", "No expense added yet"))
                    } else {
                        for (entry in expenseByCat.entries) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = categoryLabel(entry.key), modifier = Modifier.weight(1f), fontSize = 14.sp)
                                Text(text = moneyText(entry.value), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            // বাকি ও ধার
            if (pendingDue.isNotEmpty()) {
                SectionTitle(tr("বাকি ও ধার", "Due & Loan"))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenDue() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("মোট বাকি", "Total Due"),
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp
                        )
                        Text(text = moneyText(dueTotal), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedMain)
                    }
                }
            }
            // সাম্প্রতিক লেনদেন
            SectionTitle(tr("সাম্প্রতিক লেনদেন", "Recent Transactions"))
        }
        if (transactions.isEmpty()) {
            item { EmptyRow(tr("এখনো কোনো লেনদেন যোগ করা হয়নি", "No transactions added yet")) }
        } else {
            items(transactions.take(20)) { item ->
                TransactionRow(item, onDelete = { viewModel.deleteTransaction(item) })
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun QuickAddButton(icon: ImageVector, label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = label)
            Text(text = label, fontSize = 13.sp)
        }
    }
}

@Composable
fun TransactionRow(item: Transaction, onDelete: () -> Unit) {
    val positive = item.type == "আয়"
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
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
