package com.aybay.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aybay.app.data.Transaction
import java.time.LocalDate

// লেনদেন অংশ (জমা খরচের মতো): মোট আয় / মোট ব্যয় / নীট সঞ্চয় কার্ডের
// গায়ে চাপ দিলে হোমের ভেতরে, মোট কার্ডের নিচে খোলে — আলাদা পাতা নয়।
// শিরোনামের লাইনে ডানে রিসাইকেল বিন আর ফিরে যান বোতাম থাকে — চার্ট
// কার্ডের হেডিংয়ের মতো একই লাইনে।

fun ledgerFilterByPeriod(
    list: List<Transaction>,
    period: String,
    today: LocalDate
): List<Transaction> {
    val todayKey = today.toString()
    return when (period) {
        "দৈনিক" -> list.filter { it.date == todayKey }
        "সাপ্তাহিক" -> list.filter { it.date >= today.minusDays(6).toString() }
        "বার্ষিক" -> list.filter { it.date.startsWith(todayKey.substring(0, 4)) }
        else -> list.filter { it.date.startsWith(todayKey.substring(0, 7)) }
    }
}

// সময়সীমার তারিখের সীমা: জমার মতো পরের সময়ের শুরু পর্যন্ত দেখায়
// (যেমন মাসিক: ১ নভেম্বর — ১ ডিসেম্বর)।
private fun periodRange(period: String, today: LocalDate): Pair<String, String> {
    val (start, end) = when (period) {
        "দৈনিক" -> today to today.plusDays(1)
        "সাপ্তাহিক" -> today.minusDays(6) to today.plusDays(1)
        "বার্ষিক" -> LocalDate.of(today.year, 1, 1) to LocalDate.of(today.year + 1, 1, 1)
        else -> {
            val first = LocalDate.of(today.year, today.monthValue, 1)
            first to first.plusMonths(1)
        }
    }
    return fullDateLabel(start.toString()) to fullDateLabel(end.toString())
}

fun ledgerSigned(t: Transaction): Long =
    if (t.type == "সঞ্চয়" && t.direction == "উত্তোলন") -t.amount else t.amount

// লেনদেন অংশ: হোমের ভেতরে, মোট কার্ডের নিচে খোলে — আলাদা পাতা নয়।
// হোমের LazyColumn-ই স্ক্রল করে, তাই এই অংশে নিজের কোনো স্ক্রল নেই;
// হিসাব হোম থেকে আসে, ডায়ালগগুলো হোমের রুটে থাকে।
@Composable
fun LedgerSection(
    viewModel: AyBayViewModel,
    type: String,
    ledgerPeriod: String,
    onLedgerPeriodChange: (String) -> Unit,
    periodList: List<Transaction>,
    periodTotal: Long,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
    onShowRecycleBin: () -> Unit,
    onOpenReport: () -> Unit
) {
    val today = LocalDate.now()

    val pageColor = when (type) {
        "আয়" -> LedgerGreen
        "ব্যয়" -> LedgerRed
        else -> LedgerBrown
    }
    val titleText = when (type) {
        "আয়" -> "🟢 " + tr("মোট আয়ের লেনদেন", "Income Transactions")
        "ব্যয়" -> "🔴 " + tr("মোট ব্যয়ের লেনদেন", "Expense Transactions")
        else -> "🏦 " + tr("নীট মোট সঞ্চয়ের লেনদেন", "Net Savings Transactions")
    }
    val headerText = when (type) {
        "আয়" -> "⬇️ " + tr("মোট আয়", "Total Income")
        "ব্যয়" -> "⬆️ " + tr("মোট ব্যয়", "Total Expense")
        else -> "🏺 " + tr("নীট মোট সঞ্চয়", "Net Total Savings")
    }
    val totalName = when (type) {
        "আয়" -> tr("মোট আয়", "Total Income")
        "ব্যয়" -> tr("মোট ব্যয়", "Total Expense")
        else -> tr("নীট সঞ্চয়", "Net Savings")
    }
    val addLabel = when (type) {
        "আয়" -> tr("নতুন আয় যোগ করুন", "Add new income")
        "ব্যয়" -> tr("নতুন ব্যয় যোগ করুন", "Add new expense")
        else -> tr("নতুন সঞ্চয় যোগ করুন", "Add new savings")
    }

    val (rangeStart, rangeEnd) = periodRange(ledgerPeriod, today)

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(12.dp)
    ) {
        // শিরোনাম: রঙিন ডট + নাম বামে, রিসাইকেল বিন + ফিরে যান ডানে, একই লাইনে।
        JomaSectionHeader(
            dotColor = pageColor,
            title = titleText,
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .border(1.dp, TrackGrey, RoundedCornerShape(18.dp))
                            .clickable { onShowRecycleBin() }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🗑️ " + tr("রিসাইকেল বিন", "Recycle Bin"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(JomaBlue, RoundedCornerShape(18.dp))
                            .clickable { onBack() }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "< " + tr("ফিরে যান", "Back"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        )
        // রঙিন হেডার
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(pageColor, RoundedCornerShape(14.dp))
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = headerText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        // সময়সীমার ট্যাব
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(GreyBg, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            for (p in listOf("দৈনিক", "সাপ্তাহিক", "মাসিক", "বার্ষিক")) {
                val selected = ledgerPeriod == p
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selected) pageColor else Color.Transparent,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { onLedgerPeriodChange(p) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = periodLabel(p),
                        color = if (selected) Color.White else Color.Black,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        // তারিখের সীমা
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(GreyBg, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📅 " + tr("নির্বাচিত সময়সীমা", "Selected period") + ": " +
                    periodLabel(ledgerPeriod),
                modifier = Modifier.weight(1f),
                fontSize = 13.sp
            )
            Text(
                text = if (ledgerPeriod == "দৈনিক") tr("আজ", "Today") + " (" + fullDateLabel(today.toString()) + ")" else rangeStart + " — " + rangeEnd,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        // মোট + লেনদেনের সংখ্যা
        Row(
            modifier = Modifier.fillMaxWidth()
                .border(1.5.dp, pageColor, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = periodLabel(ledgerPeriod) + " " + totalName,
                    fontSize = 14.sp,
                    color = GreyText,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = moneyText(periodTotal),
                    modifier = Modifier.padding(top = 2.dp),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = pageColor
                )
            }
            Column(
                modifier = Modifier
                    .background(GreyBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tr("লেনদেনের সংখ্যা", "Transactions"),
                    fontSize = 13.sp,
                    color = GreyText
                )
                Text(
                    text = if (LangState.isEnglish) periodList.size.toString()
                        else toBengaliDigits(periodList.size.toString()) + " টি",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        // নতুন যোগ করুন
        Button(
            onClick = { onAdd(type) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = pageColor)
        ) {
            Text(
                text = "+ " + addLabel,
                modifier = Modifier.padding(vertical = 4.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    }
}

// রিসাইকেল বিন ডায়ালগ (জমা খরচের মতো): মোছা হিসাব ৩ দিন থাকে,
// রিস্টোর করা যায়, ৩ দিন পর চিরতরে মুছে যায়।
@Composable
fun RecycleBinDialog(
    deleted: List<Transaction>,
    onRestore: (Transaction) -> Unit,
    onDeletePermanently: (Transaction) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
                    .heightIn(max = 520.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🗑️ " + tr("রিসাইকেল বিন (৩ দিন)", "Recycle Bin (3 days)"),
                        modifier = Modifier.weight(1f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = GreyText
                        )
                    }
                }
                Text(
                    text = tr(
                        "মুছে ফেলা হিসাব ৩ দিন পর স্বয়ংক্রিয়ভাবে চিরতরে মুছে যাবে",
                        "Deleted records are permanently removed automatically after 3 days"
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 13.sp,
                    color = GreyText
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (deleted.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(GreyBg, RoundedCornerShape(16.dp))
                            .padding(vertical = 28.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🗑️", fontSize = 52.sp)
                        Text(
                            text = tr("রিসাইকেল বিন খালি আছে", "Recycle bin is empty"),
                            modifier = Modifier.padding(top = 10.dp),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(
                                "তালিকা থেকে কোনো আয়, ব্যয়, সঞ্চয়, ট্রান্সফার বা বাকি-ধার মুছে ফেললে তা এখানে ৩ দিনের জন্য জমা থাকবে এবং রিস্টোর করা যাবে।",
                                "If you delete any income, expense, savings, transfer or due/loan from the list, it will stay here for 3 days and can be restored."
                            ),
                            modifier = Modifier.padding(top = 8.dp),
                            fontSize = 13.sp,
                            color = GreyText,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        for (item in deleted) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp)
                                        .background(GreyBg, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = categoryIcon(item.category), fontSize = 20.sp)
                                }
                                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                                    Text(
                                        text = categoryLabel(item.category),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = dateLabel(item.date) + " • " + item.account,
                                        fontSize = 12.sp,
                                        color = GreyText
                                    )
                                }
                                Text(
                                    text = moneyText(item.amount),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tr("রিস্টোর", "Restore"),
                                    modifier = Modifier.padding(start = 10.dp)
                                        .clickable { onRestore(item) },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JomaBlue
                                )
                                IconButton(
                                    onClick = { onDeletePermanently(item) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = RedMain,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
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
}

// বিশ্লেষণে কোন খাত দেখাবে — সম্পাদন করুন।
@Composable
fun AnalysisEditDialog(
    type: String,
    groups: List<Pair<String, Long>>,
    hiddenCats: Set<String>,
    onToggle: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = typeName + " " + tr("খাত অনুযায়ী বিশ্লেষণ", "Category Analysis"),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (groups.isEmpty()) {
                    Text(
                        text = tr("এই সময়সীমায় কোনো হিসাব নেই", "No records in this period"),
                        fontSize = 14.sp,
                        color = GreyText
                    )
                } else {
                    for ((cat, amt) in groups) {
                        val shown = "$type|$cat" !in hiddenCats
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = categoryIcon(cat), fontSize = 18.sp)
                            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                                Text(
                                    text = categoryLabel(cat),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = moneyText(amt),
                                    fontSize = 12.sp,
                                    color = GreyText
                                )
                            }
                            Switch(
                                checked = shown,
                                onCheckedChange = { onToggle(cat, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GreenMain,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = TrackGrey
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = tr("ঠিক আছে", "OK"))
            }
        }
    )
}
