package com.aybay.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aybay.app.data.Transaction
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val JOMA_INCOME_CATS = listOf(
    "বেতন", "ব্যবসা", "পণ্য বিক্রি", "ভাড়া", "সেবা আয়", "ভাতা", "অনলাইন ইনকাম", "অন্যান্য"
)

private fun formTileLabel(cat: String): String = when (cat) {
    "বেতন" -> tr("বেতন", "Salary")
    "ব্যবসা" -> tr("ব্যবসা", "Business")
    "পণ্য বিক্রি" -> tr("পণ্য বিক্রি", "Product Sales")
    "ভাড়া" -> tr("ভাড়া", "Rent")
    "সেবা আয়" -> tr("সেবা আয়", "Service Income")
    "ভাতা" -> tr("ভাতা", "Allowance")
    "অনলাইন ইনকাম" -> tr("অনলাইন ইনকাম", "Online Income")
    else -> categoryLabel(cat)
}

private fun formTileIcon(cat: String): String = when (cat) {
    "বেতন", "মাসিক বেতন", "সাপ্তাহিক বেতন" -> "💼"
    "ব্যবসা" -> "🏢"
    "পণ্য বিক্রি" -> "📦"
    "ভাড়া" -> "🏠"
    "সেবা আয়" -> "🔔"
    "ভাতা" -> "🎗️"
    "অনলাইন ইনকাম" -> "💻"
    "অন্যান্য" -> "➕"
    "সঞ্চয়" -> "🏺"
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
    "দৈনিক মজুরি" -> "👷"
    else -> "🏷️"
}

@Composable
fun AddFormScreen(viewModel: AyBayViewModel, initialType: String, initialNote: String = "", onSaved: () -> Unit) {
    val context = LocalContext.current
    var type by remember { mutableStateOf(if (initialType == "ট্রান্সফার") "ব্যয়" else initialType) }
    var category by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("হাতে নগদ") }
    var accountExpanded by remember { mutableStateOf(false) }
    var accountDialog by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf(initialNote) }
    var showKeypad by remember { mutableStateOf(false) }
    var detailsExpanded by remember { mutableStateOf(false) }
    var dateKey by remember { mutableStateOf(todayString()) }
    var time24 by remember { mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var showPayer by remember { mutableStateOf(false) }
    var payerName by remember { mutableStateOf("") }
    var payerAddress by remember { mutableStateOf("") }
    var payerMobile by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val mainColor = when (type) {
        "আয়" -> GreenMain
        "ব্যয়" -> RedMain
        else -> YellowMain
    }
    val onMainColor = if (type == "সঞ্চয়") Color.Black else Color.White
    val amount = amountText.toLongOrNull() ?: 0L
    val step = when {
        category.isBlank() -> 1
        amount <= 0L -> 2
        else -> 3
    }
    val cashBalance = (accounts.find { it.name == "হাতে নগদ" }?.balance ?: 0L) +
        transactions.filter { it.account == "হাতে নগদ" }.sumOf {
            when (it.type) {
                "আয়" -> it.amount
                else -> -it.amount
            }
        }
    val accountKind = accounts.find { it.name == account }?.kind
    val accountIcon = when {
        account == "হাতে নগদ" -> "💵"
        accountKind == "ব্যাংক" -> "🏦"
        accountKind == "মোবাইল" -> "📱"
        else -> "🗂️"
    }
    val accountLabel = if (account == "হাতে নগদ") tr("হাতে নগদ", "Cash in hand") else account

    val formCategories: List<String> = when (type) {
        "আয়" -> JOMA_INCOME_CATS + loadCustomCategories(context, "আয়")
            .filter { it !in JOMA_INCOME_CATS }
        "ব্যয়" -> expenseCategories + loadCustomCategories(context, "ব্যয়")
            .filter { it !in expenseCategories }
        else -> listOf("সঞ্চয়", "অন্যান্য") + loadCustomCategories(context, "সঞ্চয়")
            .filter { it !in listOf("সঞ্চয়", "অন্যান্য") }
    }

    fun pickDate(onPick: (LocalDate) -> Unit) {
        val initial = LocalDate.parse(dateKey)
        DatePickerDialog(
            context,
            { _, y, m, d -> onPick(LocalDate.of(y, m + 1, d)) },
            initial.year, initial.monthValue - 1, initial.dayOfMonth
        ).show()
    }

    fun pickTime() {
        val parts = time24.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(
            context,
            { _, hour, minute ->
                time24 = String.format("%02d:%02d", hour, minute)
            },
            h, m, false
        ).show()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ধরনের পিল
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val typePills = listOf(
                    Triple("আয়", GreenMain, "+ ✓"),
                    Triple("ব্যয়", RedMain, "−"),
                    Triple("সঞ্চয়", YellowMain, "🏺")
                )
                for ((t, color, icon) in typePills) {
                    val selected = type == t
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selected) color else when (t) {
                                    "আয়" -> GreenTint
                                    "ব্যয়" -> RedTint
                                    else -> YellowTint
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                type = t
                                category = ""
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = icon + " " + tr(
                                t,
                                if (t == "আয়") "Income" else if (t == "ব্যয়") "Expense" else "Savings"
                            ),
                            color = if (selected) {
                                if (t == "সঞ্চয়") Color.Black else Color.White
                            } else Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            // ধাপের সারি
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val steps = listOf(
                    tr("খাত বাছুন", "Pick a category"),
                    tr("পরিমাণ লিখুন", "Enter amount"),
                    tr("সংরক্ষণ", "Save")
                )
                for ((index, label) in steps.withIndex()) {
                    val current = step == index + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (current) mainColor else GreyBg,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = toBengaliDigits((index + 1).toString()) + " " + label,
                            color = if (current) onMainColor else GreyText,
                            fontSize = 12.sp,
                            fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // খাত
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "●",
                    color = mainColor,
                    fontSize = 13.sp
                )
                Text(
                    text = when (type) {
                        "আয়" -> tr("আয়ের খাত নির্বাচন করুন", "Choose an income category")
                        "ব্যয়" -> tr("ব্যয়ের খাত নির্বাচন করুন", "Choose an expense category")
                        else -> tr("সঞ্চয়ের খাত নির্বাচন করুন", "Choose a savings category")
                    },
                    modifier = Modifier.padding(start = 6.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            val tileBgs = listOf(BlueTint, PinkTint, GreenTint, PurpleTint)
            for (row in formCategories.chunked(4)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for ((index, cat) in row.withIndex()) {
                        val selected = category == cat
                        val tileIndex = formCategories.indexOf(cat)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(88.dp)
                                .background(tileBgs[tileIndex % 4], RoundedCornerShape(14.dp))
                                .then(
                                    if (selected) Modifier.border(
                                        2.dp, mainColor, RoundedCornerShape(14.dp)
                                    ) else Modifier
                                )
                                .clickable { category = cat }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = formTileIcon(cat), fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formTileLabel(cat),
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                        if (index == row.size - 1 && row.size < 4) {
                            repeat(4 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            // টাকা কোথায়
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreyBg, RoundedCornerShape(12.dp))
                    .clickable { accountExpanded = !accountExpanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("টাকা কোথায় জমা হচ্ছে", "Where is the money going"),
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .background(BlueTint, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = accountIcon + " " + accountLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(32.dp).background(JomaBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (accountExpanded) "▲" else "▼",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
            if (accountExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val cashSelected = account == "হাতে নগদ"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (cashSelected) JomaBlue else Color.White,
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                if (cashSelected) JomaBlue else TrackGrey,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                account = "হাতে নগদ"
                                accountExpanded = false
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "💵 " + tr("হাতে নগদ", "Cash in hand"),
                                color = if (cashSelected) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = moneyText(cashBalance),
                                color = if (cashSelected) Color.White else GreyText,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .border(1.dp, TrackGrey, RoundedCornerShape(14.dp))
                            .clickable { accountDialog = "pick" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🗂️ " + tr("অ্যাকাউন্ট", "Account"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr("ব্যাংক / মোবাইল ব্যাংকিং", "Bank / Mobile Banking"),
                                fontSize = 12.sp,
                                color = GreyText
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // পরিমাণ
            Text(
                text = tr("পরিমাণ *", "Amount *"),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(52.dp).background(JomaBlue, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "৳",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v -> amountText = v.filter { it.isDigit() }.take(9) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🧮",
                    modifier = Modifier.clickable { showKeypad = !showKeypad },
                    fontSize = 26.sp
                )
            }
            if (showKeypad) {
                Spacer(modifier = Modifier.height(10.dp))
                NumberKeypad(onKey = { key ->
                    amountText = when (key) {
                        "⌫" -> if (amountText.isNotEmpty()) amountText.dropLast(1) else ""
                        else -> (amountText + key).take(9)
                    }
                })
            }
            Spacer(modifier = Modifier.height(16.dp))
            // তারিখ ও সময়, নোট, প্রদানকারী তথ্য
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreyBg, RoundedCornerShape(12.dp))
                    .clickable { detailsExpanded = !detailsExpanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("তারিখ ও সময়, নোট, প্রদানকারী তথ্য", "Date & time, note, payer info"),
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier.size(32.dp).background(JomaBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (detailsExpanded) "▲" else "▼",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
            if (detailsExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreyBg, RoundedCornerShape(12.dp))
                        .clickable {
                            pickDate { picked ->
                                dateKey = picked.toString()
                                pickTime()
                            }
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🕐 " + tr("তারিখ ও সময়", "Date & time") + ": " +
                            (if (dateKey == todayString()) tr("আজ", "Today") else fullDateLabel(dateKey)) +
                            ", " + timeLabel(time24),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("তারিখ", "Date"),
                            fontSize = 13.sp,
                            color = GreyText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, TrackGrey, RoundedCornerShape(12.dp))
                                .clickable { pickDate { picked -> dateKey = picked.toString() } }
                                .padding(12.dp)
                        ) {
                            Text(text = fullDateLabel(dateKey), fontSize = 14.sp)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("সময়", "Time"),
                            fontSize = 13.sp,
                            color = GreyText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, TrackGrey, RoundedCornerShape(12.dp))
                                .clickable { pickTime() }
                                .padding(12.dp)
                        ) {
                            Text(text = timeLabel(time24), fontSize = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = tr("নোট (ঐচ্ছিক)", "Note (optional)"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text(text = tr("বিস্তারিত লিখুন...", "Write details..."), color = GreyText) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreyBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👤 " + tr("প্রদানকারীর তথ্য যোগ করুন", "Add payer info"),
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = showPayer,
                        onCheckedChange = { showPayer = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GreenMain,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = TrackGrey
                        )
                    )
                }
                if (showPayer) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = tr("নাম", "Name"), fontSize = 13.sp, color = GreyText)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = payerName,
                        onValueChange = { payerName = it },
                        placeholder = { Text(text = tr("নাম লিখুন", "Write name"), color = GreyText) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = tr("ঠিকানা", "Address"), fontSize = 13.sp, color = GreyText)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = payerAddress,
                        onValueChange = { payerAddress = it },
                        placeholder = { Text(text = tr("ঠিকানা লিখুন", "Write address"), color = GreyText) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = tr("মোবাইল নম্বর", "Mobile number"), fontSize = 13.sp, color = GreyText)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = payerMobile,
                        onValueChange = { payerMobile = it.filter { ch -> ch.isDigit() }.take(11) },
                        placeholder = { Text(text = "০১XXXXXXXXX", color = GreyText) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val canSave = amount > 0L && category.isNotBlank()
            Button(
                onClick = {
                    val finalType = if (type == "ট্রান্সফার") "ব্যয়" else type
                    val payerParts = listOfNotNull(
                        payerAddress.trim().takeIf { it.isNotBlank() }
                            ?.let { tr("ঠিকানা", "Address") + ": " + it },
                        payerMobile.trim().takeIf { it.isNotBlank() }
                            ?.let { tr("মোবাইল", "Mobile") + ": " + it }
                    )
                    val finalNote = if (payerParts.isEmpty()) note
                    else (if (note.isBlank()) "" else note + "\n") + payerParts.joinToString(", ")
                    viewModel.addTransaction(
                        Transaction(
                            type = finalType,
                            amount = amount,
                            category = category,
                            account = account,
                            date = dateKey,
                            time = time24,
                            note = finalNote,
                            payer = payerName.trim()
                        )
                    )
                    saved = true
                    onSaved()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = mainColor,
                    contentColor = onMainColor
                )
            ) {
                Text(
                    text = "💾 " + tr(
                        type + " সংরক্ষণ করুন",
                        "Save " + if (type == "আয়") "Income" else if (type == "ব্যয়") "Expense" else "Savings"
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (saved) {
                Text(
                    text = tr("সংরক্ষণ হয়েছে", "Saved"),
                    fontSize = 14.sp,
                    color = GreenDark,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        when (accountDialog) {
            "pick" -> AccountPickDialog(
                onDismiss = { accountDialog = null },
                onPickBank = { accountDialog = "bank" },
                onPickMobile = { accountDialog = "mobile" }
            )
            "bank" -> BankAccountDialog(
                viewModel = viewModel,
                onDismiss = { accountDialog = null },
                onBack = { accountDialog = "pick" },
                onGoMobile = { accountDialog = "mobile" },
                onAccountChosen = { name ->
                    account = name
                    accountExpanded = false
                }
            )
            "mobile" -> MobileAccountDialog(
                viewModel = viewModel,
                onDismiss = { accountDialog = null },
                onBack = { accountDialog = "pick" },
                onGoBank = { accountDialog = "bank" },
                onAccountChosen = { name ->
                    account = name
                    accountExpanded = false
                }
            )
        }
    }
}

@Composable
fun NumberKeypad(onKey: (String) -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("00", "0", "⌫")
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (key in row) {
                    Text(
                        text = toBengaliDigits(key),
                        modifier = Modifier
                            .weight(1f)
                            .background(GreyBg, RoundedCornerShape(10.dp))
                            .padding(vertical = 12.dp)
                            .clickable { onKey(key) },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
