package com.aybay.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

// জমা খরচের সঞ্চয়ের ধরন (হুবহু); অন্যান্য সঞ্চয় টাইলে নতুন ধরন যোগ হয়।
private val JOMA_SAVINGS_TYPES = listOf(
    "ব্যাংক", "মোবাইল ব্যাংকিং", "ব্যাংক (ডিপিএস, ...)", "ফান্ড", "সোনা বা মূল্যবান...", "মাটির ব্যাংক"
)

private fun rateTypeLabel(rateType: String): String = when (rateType) {
    "মাসিক" -> tr("মাসিক", "Monthly")
    else -> tr("বার্ষিক", "Yearly")
}

private fun interestMethodLabel(method: String): String = when (method) {
    "চক্রবৃদ্ধি" -> tr("চক্রবৃদ্ধি মুনাফা (Compound Interest)", "Compound Interest")
    else -> tr("সরল মুনাফা (Simple Interest)", "Simple Interest")
}

private fun formTileLabel(cat: String): String = when (cat) {
    "বেতন" -> tr("বেতন", "Salary")
    "ব্যবসা" -> tr("ব্যবসা", "Business")
    "পণ্য বিক্রি" -> tr("পণ্য বিক্রি", "Product Sales")
    "ভাড়া" -> tr("ভাড়া", "Rent")
    "সেবা আয়" -> tr("সেবা আয়", "Service Income")
    "ভাতা" -> tr("ভাতা", "Allowance")
    "অনলাইন ইনকাম" -> tr("অনলাইন ইনকাম", "Online Income")
    "ব্যাংক" -> tr("ব্যাংক", "Bank")
    "মোবাইল ব্যাংকিং" -> tr("মোবাইল ব্যাংকিং", "Mobile Banking")
    "ব্যাংক (ডিপিএস, ...)" -> tr("ব্যাংক (ডিপিএস, ...)", "Bank (DPS, ...)")
    "ফান্ড" -> tr("ফান্ড", "Fund")
    "সোনা বা মূল্যবান..." -> tr("সোনা বা মূল্যবান...", "Gold or valuables...")
    "মাটির ব্যাংক" -> tr("মাটির ব্যাংক", "Clay bank")
    "অন্যান্য সঞ্চয়" -> tr("অন্যান্য সঞ্চয়", "Other savings")
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
    "ব্যাংক", "ব্যাংক (ডিপিএস, ...)" -> "🏦"
    "মোবাইল ব্যাংকিং" -> "📱"
    "ফান্ড" -> "📈"
    "সোনা বা মূল্যবান..." -> "🎖️"
    "মাটির ব্যাংক" -> "🏺"
    "অন্যান্য সঞ্চয়" -> "➕"
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
    "দৈনিক মজুরি" -> "👷"
    else -> "🏷️"
}

@Composable
fun AddFormScreen(viewModel: AyBayViewModel, initialType: String, initialNote: String = "", onSaved: () -> Unit) {
    val context = LocalContext.current
    var type by remember { mutableStateOf(initialType) }
    var category by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("হাতে নগদ") }
    var accountExpanded by remember { mutableStateOf(false) }
    var accountDialog by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf(initialNote) }
    var subcat by remember { mutableStateOf("") }
    var showCalc by remember { mutableStateOf(false) }
    var showNewCatDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var renameFor by remember { mutableStateOf<String?>(null) }
    var pendingCatName by remember { mutableStateOf("") }
    var showIconDialog by remember { mutableStateOf(false) }
    var iconChangeFor by remember { mutableStateOf<String?>(null) }
    var iconInitial by remember { mutableStateOf("✨") }
    var showNewSubcatDialog by remember { mutableStateOf(false) }
    var newSubcatName by remember { mutableStateOf("") }
    var catMenuFor by remember { mutableStateOf<String?>(null) }
    var catsTick by remember { mutableStateOf(0) }
    var detailsExpanded by remember { mutableStateOf(false) }
    var dateKey by remember { mutableStateOf(todayString()) }
    var time24 by remember { mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var showPayer by remember { mutableStateOf(false) }
    var payerName by remember { mutableStateOf("") }
    var payerAddress by remember { mutableStateOf("") }
    var payerMobile by remember { mutableStateOf("") }
    // সঞ্চয় ফর্ম: জমা/উত্তোলন, মুনাফা, ব্যাংক/প্রতিষ্ঠানের তথ্য, পিন ডায়ালগ।
    var prefs by remember { mutableStateOf(loadAppPrefs(context)) }
    var savingsDirection by remember { mutableStateOf("জমা") }
    var showPinDialog by remember { mutableStateOf(false) }
    var profitRate by remember { mutableStateOf("") }
    var rateType by remember { mutableStateOf("বার্ষিক") }
    var interestMethod by remember { mutableStateOf("সরল") }
    var showBankInfo by remember { mutableStateOf(true) }
    var bankName by remember { mutableStateOf("") }
    var bankAddress by remember { mutableStateOf("") }
    var bankMobile by remember { mutableStateOf("") }
    var rateTypeMenu by remember { mutableStateOf(false) }
    var methodMenu by remember { mutableStateOf(false) }
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
        transactions.filter {
            it.account == "হাতে নগদ" ||
                (it.type == "ট্রান্সফার" && it.payer == "হাতে নগদ")
        }.sumOf {
            when (it.type) {
                "আয়" -> it.amount
                // সঞ্চয়ে জমা দিলে নগদ কমে, তুললে নগদ বাড়ে।
                "সঞ্চয়" -> if (it.direction == "উত্তোলন") it.amount else -it.amount
                // ট্রান্সফার: নগদ থেকে গেলে কমে, নগদে এলে বাড়ে।
                "ট্রান্সফার" -> if (it.account == "হাতে নগদ") -it.amount else it.amount
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

    // সফলতার ব্যানারের হিসাবের জন্য সংরক্ষণের আগের ব্যালেন্স (হোমের accountBalances-এর মতো)।
    fun accountBalanceBefore(name: String): Long {
        val acc = accounts.find { it.name == name }
        return (acc?.balance ?: 0L) + transactions.filter {
            it.account == name || (it.type == "ট্রান্সফার" && it.payer == name)
        }.sumOf {
            when (it.type) {
                "আয়" -> it.amount
                "ব্যয়" -> -it.amount
                "সঞ্চয়" -> if (it.direction == "উত্তোলন") it.amount else -it.amount
                "ট্রান্সফার" -> if (it.account == name) -it.amount else it.amount
                else -> 0L
            }
        }
    }

    // সঞ্চয়ের ধরনের আগের জমা (টাইলে দেখানো হিসাবের মতো)।
    fun savingsTypeBalanceBefore(name: String): Long {
        return transactions.filter {
            (it.type == "সঞ্চয়" && it.category == name) ||
                (it.type == "ট্রান্সফার" && (it.account == name || it.payer == name))
        }.sumOf {
            when {
                it.type == "ট্রান্সফার" -> if (it.account == name) -it.amount else it.amount
                it.direction == "উত্তোলন" -> -it.amount
                else -> it.amount
            }
        }
    }

    // খাতের টাইল: ডিফল্ট খাত + নিজের খাত (অন্যান্য টাইলের আগে) + অন্যান্য (➕)।
    val customCatMap = remember(type, catsTick) { loadCustomCategoryMap(context, type) }
    val formCatItems: List<CatItem> = run {
        val baseCats = when (type) {
            "আয়" -> JOMA_INCOME_CATS
            "ব্যয়" -> expenseCategories
            else -> JOMA_SAVINGS_TYPES
        }
        val base = baseCats.filter { it != "অন্যান্য" && it != "অন্যান্য ব্যয়" }
        val customs = customCatMap.entries
            .filter { (name, _) -> name !in baseCats }
            .map { (name, icon) -> CatItem(name, icon, true) }
        base.map { CatItem(it, formTileIcon(it), false) } + customs +
            listOf(CatItem("অন্যান্য", "➕", false))
    }
    val subcatOptions = remember(type, category, catsTick) {
        if (category.isBlank()) emptyList() else allSubcats(context, type, category)
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
                    val pillIcon = if (t == "সঞ্চয়" && selected) "🏺✓" else icon
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
                                subcat = ""
                                if (t == "সঞ্চয়") {
                                    // সঞ্চয় ফর্মে অ্যাকাউন্ট বাছাই নেই — সবসময় হাতে নগদ থেকে।
                                    account = "হাতে নগদ"
                                    accountExpanded = false
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pillIcon + " " + tr(
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
                        else -> tr("সঞ্চয়ের ধরন নির্বাচন করুন", "Choose a savings type")
                    },
                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (type == "সঞ্চয়") {
                    Box(
                        modifier = Modifier
                            .background(LightChartBg, RoundedCornerShape(20.dp))
                            .border(1.dp, BlueTint, RoundedCornerShape(20.dp))
                            .clickable { showPinDialog = true }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔐 " + tr("পিন ও গোপনীয়তা", "PIN & Privacy"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JomaBlue
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // জমা / উত্তোলন (শুধু সঞ্চয়ে): জমা বাড়ায়, উত্তোলন কমায়।
            if (type == "সঞ্চয়") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val depositSelected = savingsDirection == "জমা"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (depositSelected) JomaBlue else Color.White,
                                RoundedCornerShape(24.dp)
                            )
                            .border(
                                2.dp,
                                JomaBlue,
                                RoundedCornerShape(24.dp)
                            )
                            .clickable { savingsDirection = "জমা" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (if (depositSelected) "✓ " else "") + "⬆️ " +
                                tr("জমা করুন", "Deposit"),
                            color = if (depositSelected) Color.White else JomaBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (!depositSelected) GreenMain else Color.White,
                                RoundedCornerShape(24.dp)
                            )
                            .border(
                                2.dp,
                                GreenMain,
                                RoundedCornerShape(24.dp)
                            )
                            .clickable { savingsDirection = "উত্তোলন" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (if (!depositSelected) "✓ " else "") + "⬇️ " +
                                tr("উত্তোলন করুন", "Withdraw"),
                            color = if (!depositSelected) Color.White else GreenMain,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            val tileBgs = listOf(BlueTint, PinkTint, GreenTint, PurpleTint)
            for ((rowIndex, row) in formCatItems.chunked(4).withIndex()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for ((index, item) in row.withIndex()) {
                        val selected = category == item.name
                        val tileIndex = rowIndex * 4 + index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(if (type == "সঞ্চয়") 104.dp else 88.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(tileBgs[tileIndex % 4], RoundedCornerShape(14.dp))
                                    .then(
                                        if (selected) Modifier.border(
                                            2.dp, mainColor, RoundedCornerShape(14.dp)
                                        ) else Modifier
                                    )
                                    .clickable {
                                        if (item.name == "অন্যান্য") {
                                            renameFor = null
                                            newCatName = ""
                                            showNewCatDialog = true
                                        } else {
                                            category = item.name
                                            subcat = ""
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = item.icon, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when {
                                        item.name == "অন্যান্য" && type == "ব্যয়" ->
                                            tr("অন্যান্য ব্যয়", "Other Expenses")
                                        item.name == "অন্যান্য" && type == "সঞ্চয়" ->
                                            tr("অন্যান্য সঞ্চয়", "Other savings")
                                        else -> formTileLabel(item.name)
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                                // সঞ্চয়ে প্রতিটি ধরনের বর্তমান জমা (জমা − উত্তোলন) দেখায়।
                                // ট্রান্সফার: এই ধরনে এলে বাড়ে, এই ধরন থেকে গেলে কমে।
                                if (type == "সঞ্চয়") {
                                    Text(
                                        text = if (item.name == "অন্যান্য") {
                                            tr("নতুন যোগ", "Add new")
                                        } else {
                                            moneyText(
                                                transactions
                                                    .filter {
                                                        (it.type == "সঞ্চয়" && it.category == item.name) ||
                                                            (it.type == "ট্রান্সফার" &&
                                                                (it.account == item.name || it.payer == item.name))
                                                    }
                                                    .sumOf {
                                                        when {
                                                            it.type == "ট্রান্সফার" ->
                                                                if (it.account == item.name) -it.amount else it.amount
                                                            it.direction == "উত্তোলন" -> -it.amount
                                                            else -> it.amount
                                                        }
                                                    }
                                            )
                                        },
                                        fontSize = 11.sp,
                                        color = GreyText
                                    )
                                }
                            }
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset((-4).dp, (-4).dp)
                                        .size(22.dp)
                                        .background(GreenMain, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (item.isCustom) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(4.dp, (-4).dp)
                                        .size(24.dp)
                                        .background(JomaBlue, CircleShape)
                                        .clickable { catMenuFor = item.name },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⋮",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
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
            // উপখাত (সঞ্চয়ে উপখাত নেই)
            if (category.isNotBlank() && type != "সঞ্চয়") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tr("উপখাত", "Sub-category"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (sub in subcatOptions) {
                        val subSelected = subcat == sub
                        Box(
                            modifier = Modifier
                                .background(
                                    if (subSelected) JomaBlue else GreyBg,
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    if (subSelected) JomaBlue else TrackGrey,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    if (sub == "অন্যান্য") {
                                        newSubcatName = ""
                                        showNewSubcatDialog = true
                                    } else {
                                        subcat = sub
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (if (subSelected) "✓ " else "") + categoryLabel(sub),
                                color = if (subSelected) Color.White else Color.Black,
                                fontSize = 13.sp,
                                fontWeight = if (subSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            // টাকা কোথায় (সঞ্চয়ে ধরনই ঠিকানা — এই অংশ লুকানো থাকে)
            if (type != "সঞ্চয়") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreyBg, RoundedCornerShape(12.dp))
                    .clickable { accountExpanded = !accountExpanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (type == "ব্যয়") tr("টাকা কোথা থেকে বের হচ্ছে", "Where is the money coming from")
                        else tr("টাকা কোথায় জমা হচ্ছে", "Where is the money going"),
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
                // খাত না বাছা থাকলে ঘরটায় চাপ দিলে সতর্ক-ব্যানার আসে (ঘর disabled থাকে)।
                // clickable শুধু খাত খালি থাকলেই যোগ করা হয় — খাত বাছা থাকলে clickable
                // একদম থাকে না, নইলে বাবার clickable ঘরের চাপ আটকে দেয় (১.২০-এর বাগ)।
                Box(
                    modifier = Modifier.weight(1f)
                        .then(if (category.isBlank()) Modifier.clickable { viewModel.showWarningBanner(type) } else Modifier),
                    contentAlignment = Alignment.CenterStart
                ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v -> amountText = v.filter { it.isDigit() }.take(9) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = category.isNotBlank(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = {
                        Text(
                            text = if (category.isBlank()) {
                                if (type == "সঞ্চয়") tr(
                                    "আগে উপরে সঞ্চয়ের ধরন বাছুন",
                                    "Pick a savings type above first"
                                ) else tr("আগে উপরে খাত বাছুন", "Pick a category above first")
                            } else "0.00",
                            color = GreyText
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🧮",
                    modifier = Modifier.clickable(enabled = category.isNotBlank()) { showCalc = true },
                    fontSize = 26.sp
                )
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
                    text = if (type == "সঞ্চয়") tr(
                        "তারিখ ও সময়, নোট, ব্যাংক/প্রতিষ্ঠানের তথ্য",
                        "Date & time, note, bank/institution info"
                    ) else if (type == "ব্যয়") tr("তারিখ ও সময়, নোট, গ্রহণকারী তথ্য", "Date & time, note, receiver info")
                        else tr("তারিখ ও সময়, নোট, প্রদানকারী তথ্য", "Date & time, note, payer info"),
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
                // সঞ্চয়ের মুনাফা আর ব্যাংক/প্রতিষ্ঠানের তথ্য (জমার মতো)।
                if (type == "সঞ্চয়") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("মুনাফার হার % (ঐচ্ছিক)", "Profit rate % (optional)"),
                                fontSize = 13.sp,
                                color = GreyText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = profitRate,
                                onValueChange = { profitRate = it.filter { ch -> ch.isDigit() || ch == '.' }.take(5) },
                                placeholder = { Text(text = tr("যেমন: ৬", "e.g. 6"), color = GreyText) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("হার প্রকার", "Rate type"),
                                fontSize = 13.sp,
                                color = GreyText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .border(1.dp, TrackGrey, RoundedCornerShape(12.dp))
                                        .clickable { rateTypeMenu = true }
                                        .padding(12.dp)
                                ) {
                                    Text(text = rateTypeLabel(rateType), fontSize = 14.sp)
                                }
                                DropdownMenu(
                                    expanded = rateTypeMenu,
                                    onDismissRequest = { rateTypeMenu = false }
                                ) {
                                    for (rt in listOf("বার্ষিক", "মাসিক")) {
                                        DropdownMenuItem(
                                            text = { Text(text = rateTypeLabel(rt)) },
                                            onClick = { rateType = rt; rateTypeMenu = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr("মুনাফা হিসাবের পদ্ধতি", "Interest calculation method"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, TrackGrey, RoundedCornerShape(12.dp))
                                .clickable { methodMenu = true }
                                .padding(12.dp)
                        ) {
                            Text(text = interestMethodLabel(interestMethod), fontSize = 14.sp)
                        }
                        DropdownMenu(
                            expanded = methodMenu,
                            onDismissRequest = { methodMenu = false }
                        ) {
                            for (m in listOf("সরল", "চক্রবৃদ্ধি")) {
                                DropdownMenuItem(
                                    text = { Text(text = interestMethodLabel(m)) },
                                    onClick = { interestMethod = m; methodMenu = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GreyBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤 " + tr("ব্যাংক/প্রতিষ্ঠানের তথ্য যোগ করুন", "Add bank/institution info"),
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = showBankInfo,
                            onCheckedChange = { showBankInfo = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GreenMain,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = TrackGrey
                            )
                        )
                    }
                    if (showBankInfo) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = tr("নাম", "Name"), fontSize = 13.sp, color = GreyText)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            placeholder = { Text(text = tr("নাম লিখুন", "Write name"), color = GreyText) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = tr("ঠিকানা", "Address"), fontSize = 13.sp, color = GreyText)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = bankAddress,
                            onValueChange = { bankAddress = it },
                            placeholder = { Text(text = tr("ঠিকানা লিখুন", "Write address"), color = GreyText) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = tr("মোবাইল নম্বর", "Mobile number"), fontSize = 13.sp, color = GreyText)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = bankMobile,
                            onValueChange = { bankMobile = it.filter { ch -> ch.isDigit() }.take(11) },
                            placeholder = { Text(text = "০১XXXXXXXXX", color = GreyText) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (type != "সঞ্চয়") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreyBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👤 " + if (type == "ব্যয়") tr("যাকে দেওয়া হয়েছে তার তথ্য যোগ করুন", "Add receiver info")
                            else tr("প্রদানকারীর তথ্য যোগ করুন", "Add payer info"),
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
                }
                if (showPayer && type != "সঞ্চয়") {
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
                    val finalType = type
                    val finalCategory =
                        if (subcat.isNotBlank() && subcat != "অন্যান্য") "${category} — ${subcat}"
                        else category
                    val payerParts = listOfNotNull(
                        payerAddress.trim().takeIf { it.isNotBlank() }
                            ?.let { tr("ঠিকানা", "Address") + ": " + it },
                        payerMobile.trim().takeIf { it.isNotBlank() }
                            ?.let { tr("মোবাইল", "Mobile") + ": " + it }
                    )
                    // সঞ্চয়ের মুনাফা আর ব্যাংক/প্রতিষ্ঠানের তথ্য নোটে জোড়া হয়।
                    val savingsParts = mutableListOf<String>()
                    if (type == "সঞ্চয়") {
                        if (profitRate.isNotBlank()) {
                            savingsParts.add(
                                tr("মুনাফার হার", "Profit rate") + ": " + profitRate + "% " +
                                    rateTypeLabel(rateType) + " (" +
                                    interestMethodLabel(interestMethod) + ")"
                            )
                        }
                        if (showBankInfo) {
                            val bankParts = listOfNotNull(
                                bankName.trim().takeIf { it.isNotBlank() }
                                    ?.let { tr("নাম", "Name") + ": " + it },
                                bankAddress.trim().takeIf { it.isNotBlank() }
                                    ?.let { tr("ঠিকানা", "Address") + ": " + it },
                                bankMobile.trim().takeIf { it.isNotBlank() }
                                    ?.let { tr("মোবাইল", "Mobile") + ": " + it }
                            )
                            if (bankParts.isNotEmpty()) {
                                savingsParts.add(
                                    tr("ব্যাংক/প্রতিষ্ঠান", "Bank/Institution") + ": " +
                                        bankParts.joinToString(", ")
                                )
                            }
                        }
                    }
                    val extraParts = if (type == "সঞ্চয়") savingsParts else payerParts
                    val finalNote = if (extraParts.isEmpty()) note
                    else (if (note.isBlank()) "" else note + "\n") + extraParts.joinToString(", ")
                    // ব্যানারে খাতের নাম: আয়ে শুধু মূল খাত; ব্যয়ে উপখাত থাকলে "মূল (উপখাত)"; সঞ্চয়ে ধরনের নাম।
                    val displayCat = when (finalType) {
                        "আয়" -> category
                        "ব্যয়" -> if (subcat.isNotBlank() && subcat != "অন্যান্য") "$category ($subcat)" else category
                        else -> category
                    }
                    val isWithdraw = finalType == "সঞ্চয়" && savingsDirection == "উত্তোলন"
                    val totalBefore = when (finalType) {
                        "আয়" -> transactions.filter { it.type == "আয়" }.sumOf { it.amount }
                        "ব্যয়" -> transactions.filter { it.type == "ব্যয়" }.sumOf { it.amount }
                        else -> transactions.filter { it.type == "সঞ্চয়" }
                            .sumOf { if (it.direction == "উত্তোলন") -it.amount else it.amount }
                    }
                    val totalAfter = if (isWithdraw) totalBefore - amount else totalBefore + amount
                    val accLabel = if (finalType == "সঞ্চয়") {
                        category
                    } else if (account == "হাতে নগদ") {
                        if (finalType == "আয়") tr("বর্তমান হাতে নগদ", "Current cash in hand")
                        else tr("হাতে নগদ", "Cash in hand")
                    } else {
                        accountLabel
                    }
                    val accBefore = if (finalType == "সঞ্চয়") {
                        savingsTypeBalanceBefore(category)
                    } else {
                        accountBalanceBefore(account)
                    }
                    val accAfter = when {
                        finalType == "ব্যয়" || isWithdraw -> accBefore - amount
                        else -> accBefore + amount
                    }
                    viewModel.addTransaction(
                        Transaction(
                            type = finalType,
                            amount = amount,
                            category = finalCategory,
                            account = account,
                            date = dateKey,
                            time = time24,
                            note = finalNote,
                            payer = if (type == "সঞ্চয়") "" else payerName.trim(),
                            direction = if (type == "সঞ্চয়") savingsDirection else ""
                        )
                    )
                    viewModel.showSavedBanner(SavedInfo(
                        type = finalType,
                        displayCat = displayCat,
                        amount = amount,
                        totalBefore = totalBefore,
                        totalAfter = totalAfter,
                        accountLabel = accLabel,
                        accountBefore = accBefore,
                        accountAfter = accAfter,
                        isWithdraw = isWithdraw
                    ))
                    // জমার মতো ফরম খোলা থাকে, ব্যানার দেখায় — হোমে ফেরে না।
                    // পরের লেনদেনের জন্য খাত/উপখাত/পরিমাণ খালি করে দিই।
                    category = ""
                    subcat = ""
                    amountText = ""
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "সঞ্চয়") BrownMain else mainColor,
                    contentColor = if (type == "সঞ্চয়") Color.White else onMainColor
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
        if (showNewCatDialog) {
            CategoryNameDialog(
                title = if (renameFor != null) tr("খাতের নাম বদলান", "Rename category")
                else tr("নতুন খাতের নাম লিখুন:", "Enter the new category name:"),
                initial = newCatName,
                onDismiss = {
                    showNewCatDialog = false
                    renameFor = null
                },
                onConfirm = { name ->
                    showNewCatDialog = false
                    val old = renameFor
                    renameFor = null
                    if (old != null) {
                        renameCustomCategory(context, type, old, name)
                        if (category == old) {
                            category = name
                        }
                        catsTick++
                    } else if (name.isNotBlank()) {
                        val baseCats = when (type) {
                            "আয়" -> JOMA_INCOME_CATS
                            "ব্যয়" -> expenseCategories
                            else -> JOMA_SAVINGS_TYPES
                        }
                        if (name in baseCats || name in customCatMap) {
                            category = name
                            subcat = ""
                        } else {
                            pendingCatName = name
                            iconInitial = "✨"
                            iconChangeFor = null
                            showIconDialog = true
                        }
                    }
                }
            )
        }
        if (showIconDialog) {
            IconPickerDialog(
                initialIcon = iconInitial,
                onDismiss = {
                    showIconDialog = false
                    iconChangeFor = null
                },
                onConfirm = { icon ->
                    showIconDialog = false
                    val target = iconChangeFor
                    iconChangeFor = null
                    if (target != null) {
                        updateCustomCategoryIcon(context, type, target, icon)
                    } else if (pendingCatName.isNotBlank()) {
                        saveCustomCategory(context, type, pendingCatName, icon)
                        category = pendingCatName
                        subcat = ""
                        pendingCatName = ""
                    }
                    catsTick++
                }
            )
        }
        if (showNewSubcatDialog) {
            CategoryNameDialog(
                title = tr("নতুন উপখাতের নাম লিখুন:", "Enter the new sub-category name:"),
                initial = newSubcatName,
                onDismiss = { showNewSubcatDialog = false },
                onConfirm = { name ->
                    showNewSubcatDialog = false
                    if (name.isNotBlank()) {
                        addCustomSubcat(context, type, category, name)
                        subcat = name
                        catsTick++
                    }
                }
            )
        }
        catMenuFor?.let { menuCat ->
            CategoryMenuDialog(
                catName = menuCat,
                onDismiss = { catMenuFor = null },
                onRename = {
                    catMenuFor = null
                    renameFor = menuCat
                    newCatName = menuCat
                    showNewCatDialog = true
                },
                onChangeIcon = {
                    catMenuFor = null
                    iconChangeFor = menuCat
                    iconInitial = customCatMap[menuCat] ?: "✨"
                    showIconDialog = true
                },
                onDelete = {
                    catMenuFor = null
                    deleteCustomCategory(context, type, menuCat)
                    if (category == menuCat) {
                        category = ""
                        subcat = ""
                    }
                    catsTick++
                }
            )
        }
        if (showCalc) {
            CalculatorSheet(
                initialAmount = amountText,
                onDismiss = { showCalc = false },
                onApply = { result ->
                    amountText = result
                    showCalc = false
                }
            )
        }
        // সঞ্চয় ফর্মের "পিন ও গোপনীয়তা" বোতামের ডায়ালগ (রুটে থাকে)।
        if (showPinDialog) {
            PinPrivacyDialog(
                prefs = prefs,
                onPrefsChange = { next -> saveAppPrefs(context, next); prefs = next },
                onDismiss = { showPinDialog = false }
            )
        }
    }
}
