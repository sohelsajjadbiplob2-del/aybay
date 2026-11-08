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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aybay.app.data.Transaction
import java.time.LocalDate

// হোমের ছোট কম্পোনেন্ট — HomeScreen.kt ছোট রাখতে এখানে সরানো হয়েছে
// (create_or_update_file-এ এক আর্গুমেন্টের 131072 বাইট সীমা পেরিয়ে যাচ্ছিল)।
// বড় ফাইল ভাগ করা হলে পুশ আবার চলে — এই নিয়ম ভবিষ্যতের সব আপডেটেও মানতে হবে।

@Composable
fun QuickAddTile(
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
fun AutoCategoryChip(
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

fun categoryIcon(category: String): String = when (category) {
    "বাজার" -> "🛒"
    "বিল" -> "🧾"
    "খাবার" -> "🍲"
    "যাতায়াত" -> "🚌"
    "ধর্মীয় দান" -> "🤲"
    "ধর্মীয় দান/সহযোগিতা" -> "🤲"
    "হাতখরচ" -> "👛"
    "তেল/গ্যাস" -> "⛽"
    "যানবাহনের তেল/গ্যাস" -> "⛽"
    "মার্কেট/শপিং" -> "🛍️"
    "গাড়ি ভাড়া" -> "🚗"
    "রেস্টুরেন্ট" -> "🍽️"
    "বাসা ভাড়া" -> "🏠"
    "দোকান ভাড়া" -> "🏪"
    "ফি" -> "🎓"
    "মেনটেনেন্স" -> "🔧"
    "উন্নয়নমূলক খরচ" -> "🏗️"
    "কিস্তি" -> "💳"
    "অন্যান্য ব্যয়" -> "➕"
    "বেতন", "মাসিক বেতন", "সাপ্তাহিক বেতন" -> "💼"
    "দৈনিক মজুরি" -> "👷"
    "সঞ্চয়" -> "🏺"
    "ট্রান্সফার" -> "🔁"
    "ব্যাংক", "ব্যাংক (ডিপিএস, ...)" -> "🏦"
    "মোবাইল ব্যাংকিং" -> "📱"
    "ফান্ড" -> "📈"
    "সোনা বা মূল্যবান..." -> "🎖️"
    "মাটির ব্যাংক" -> "🏺"
    "অন্যান্য সঞ্চয়" -> "➕"
    else -> "🏷️"
}

@Composable
fun JomaTotalCard(
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
    modifier: Modifier = Modifier,
    // কার্ডের গায়ে চাপ দিলে লেনদেন পাতা খোলে; তালা আর ▼-এর নিজের ক্লিক আলাদা থাকে।
    onBodyClick: (() -> Unit)? = null
) {
    Card(
        onClick = { onBodyClick?.invoke() },
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
fun ChartTotalBox(
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
fun FlowStatBox(
    title: String,
    value: String,
    bgColor: Color,
    accent: Color,
    modifier: Modifier,
    icon: String = "",
    titleColor: Color = GreyText
) {
    Column(
        modifier = modifier.background(bgColor, RoundedCornerShape(12.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon.isNotBlank()) {
                Text(
                    text = icon,
                    fontSize = 12.sp,
                    color = titleColor,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = title,
                modifier = if (icon.isNotBlank()) Modifier.padding(start = 3.dp) else Modifier,
                fontSize = 12.sp,
                color = titleColor,
                textAlign = TextAlign.Center
            )
        }
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
fun DueBox(
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
fun LedgerStatBox(
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
fun AccountRow(name: String, sub: String, balance: Long) {
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
fun RecentTransactionRow(item: Transaction) {
    // জমা সঞ্চয় আয়ের মতো সবুজ + চিহ্ন; উত্তোলন ব্যয়ের মতো লাল − চিহ্ন।
    // ট্রান্সফার আয়-ব্যয় নয় — চিহ্ন ছাড়া নীল অঙ্ক দেখায়।
    val isTransfer = item.type == "ট্রান্সফার"
    val positive = item.type == "আয়" ||
        (item.type == "সঞ্চয়" && item.direction != "উত্তোলন")
    val tint = when (item.type) {
        "আয়" -> GreenTint
        "ব্যয়" -> RedTint
        "ট্রান্সফার" -> PurpleTint
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
                text = if (isTransfer) {
                    tr("ট্রান্সফার", "Transfer") + ": " +
                        transferDisplayLabel(item.account) + " → " +
                        transferDisplayLabel(item.payer) +
                        if (item.note.isNotBlank()) " • " + item.note else ""
                } else {
                    categoryLabel(item.category) +
                        if (item.note.isNotBlank()) " • " + item.note else ""
                },
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
            text = if (isTransfer) moneyText(item.amount) else moneyTextSigned(item.amount, positive),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isTransfer) JomaBlue else if (positive) GreenMain else RedMain
        )
    }
}

@Composable
fun MoveButton(
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

// লেনদেনের সারি (লেজার/রিপোর্ট পাতায় ব্যবহৃত) — HomeScreen.kt থেকে সরানো,
// কারণ HomeScreen.kt পুশের argv সীমা (131072 বাইট) ছাড়িয়ে গিয়েছিল।
@Composable
fun TransactionRow(item: Transaction, onDelete: () -> Unit) {
    // জমা সঞ্চয় সবুজ + চিহ্ন; উত্তোলন লাল − চিহ্ন।
    // ট্রান্সফার আয়-ব্যয় নয় — চিহ্ন ছাড়া নীল অঙ্ক, কোথা থেকে কোথায় দেখায়।
    val isTransfer = item.type == "ট্রান্সফার"
    val positive = item.type == "আয়" ||
        (item.type == "সঞ্চয়" && item.direction != "উত্তোলন")
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
                Text(
                    text = if (isTransfer) {
                        tr("ট্রান্সফার", "Transfer") + ": " +
                            transferDisplayLabel(item.account) + " → " +
                            transferDisplayLabel(item.payer)
                    } else {
                        categoryLabel(item.category)
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dateLabel(item.date) + " • " +
                        if (isTransfer) {
                            transferDisplayLabel(item.account) + " → " +
                                transferDisplayLabel(item.payer)
                        } else {
                            item.account
                        },
                    fontSize = 12.sp,
                    color = GreyText
                )
                if (item.note.isNotBlank()) {
                    Text(text = item.note, fontSize = 13.sp, color = GreyText)
                }
            }
            Text(
                text = if (isTransfer) moneyText(item.amount) else moneyTextSigned(item.amount, positive),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTransfer) JomaBlue else if (positive) GreenMain else RedMain
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

// HomeScreen.kt থেকে সরানো: খাত অনুযায়ী বিশ্লেষণ কার্ড + তার হেলপার
// (HomeScreen.kt-এর পুশ-JSON 131072 বাইট সীমা ছাড়িয়ে যাচ্ছিল)।

fun filterByPeriod(list: List<Transaction>, period: String, today: LocalDate): List<Transaction> {
    val todayKey = today.toString()
    return when (period) {
        "দৈনিক" -> list.filter { it.date == todayKey }
        "সাপ্তাহিক" -> list.filter { it.date >= today.minusDays(6).toString() }
        "বার্ষিক" -> list.filter { it.date.startsWith(todayKey.substring(0, 4)) }
        else -> list.filter { it.date.startsWith(todayKey.substring(0, 7)) }
    }
}

fun groupByCategory(list: List<Transaction>, type: String): Map<String, Long> {
    val map = linkedMapOf<String, Long>()
    for (t in list) {
        if (t.type == type) {
            val signed = if (type == "সঞ্চয়" && t.direction == "উত্তোলন") -t.amount else t.amount
            map[t.category] = (map[t.category] ?: 0L) + signed
        }
    }
    return map.entries.sortedByDescending { it.value }.associate { it.key to it.value }
}

@Composable
fun CategoryAnalysisCard(
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
    Column(modifier = Modifier.fillMaxWidth()) {
    JomaSectionHeader(
        color,
        typeName + " " + tr("খাত অনুযায়ী বিশ্লেষণ", "Category Analysis"),
        trailing = {
            Text(
                text = tr("সম্পাদন করুন", "Edit"),
                modifier = Modifier.clickable { onEditCategories(type) },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = JomaBlue
            )
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
                modifier = Modifier.fillMaxWidth()
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
}
