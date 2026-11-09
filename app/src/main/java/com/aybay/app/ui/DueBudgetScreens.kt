package com.aybay.app.ui

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aybay.app.data.Account
import com.aybay.app.data.Budget
import com.aybay.app.data.DueItem
import com.aybay.app.data.Transaction
import java.time.LocalDate

// yyyy-MM-dd লেখা থেকে তারিখ; ভুল লেখা হলে null।
private fun String.toLocalDateOrNull(): LocalDate? = try {
    LocalDate.parse(this)
} catch (e: Exception) {
    null
}

// ধারের বাকি টাকা (মোট − শোধ) — শোধ হয়ে গেলে ০।
fun dueRemaining(item: DueItem): Long =
    if (item.isPaid) 0L else (item.amount - item.paidAmount).coerceAtLeast(0L)

// ধারের অ্যাকাউন্ট-প্রভাব: ধার নিলে বাছা অ্যাকাউন্টে যোগ হয়, ধার
// দিলে কমে। বাকির কোনো প্রভাব নেই (টাকা এখনো হাতে আসেনি/যায়নি)।
// ট্রান্সফারের ব্যালেন্স-আপডেটের মতোই ৩ জায়গায় যোগ করা হয়:
// হোমের অ্যাকাউন্ট ব্যালেন্স, ফরমের হাতে নগদ/অ্যাকাউন্ট/সঞ্চয়ের হিসাব।
fun dueLoanBalanceEffect(items: List<DueItem>, accountName: String): Long =
    items.filter { it.kind == "ধার" && it.account == accountName }
        .sumOf { if (it.direction == "নিয়েছি") dueRemaining(it) else -dueRemaining(it) }

// অ্যাকাউন্টের বর্তমান ব্যালেন্স (হোমের accountBalances-এর মতো)।
fun accountBalanceOf(
    accounts: List<Account>,
    transactions: List<Transaction>,
    dueItems: List<DueItem>,
    name: String
): Long {
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
    } + dueLoanBalanceEffect(dueItems, name)
}

// সঞ্চয়ের ধরনের বর্তমান জমা (ফরমের টাইলের হিসাবের মতো)।
fun savingsTypeBalanceOf(
    transactions: List<Transaction>,
    dueItems: List<DueItem>,
    name: String
): Long {
    return transactions.filter {
        (it.type == "সঞ্চয়" && it.category == name) ||
            (it.type == "ট্রান্সফার" && (it.account == name || it.payer == name))
    }.sumOf {
        when {
            it.type == "ট্রান্সফার" -> if (it.account == name) -it.amount else it.amount
            it.direction == "উত্তোলন" -> -it.amount
            else -> it.amount
        }
    } + dueLoanBalanceEffect(dueItems, name)
}

// তথ্য-বাক্স (বাকি/ধার ডায়ালগের রঙিন বাক্স)।
@Composable
private fun DueInfoBox(text: String, bgColor: Color) {
    Box(
        Modifier.fillMaxWidth().background(bgColor, RoundedCornerShape(12.dp)).padding(12.dp)
    ) {
        Text(text = text, fontSize = 13.sp)
    }
}

// তারিখের ঘর: লেবেল + ট্যাপে ক্যালেন্ডার পিকার।
@Composable
private fun DueDateField(
    label: String,
    dateKey: String,
    onPick: (String) -> Unit
) {
    val context = LocalContext.current
    Text(
        text = label,
        fontSize = 13.sp,
        color = GreyText,
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
    )
    Box(
        Modifier.fillMaxWidth().background(GreyBg, RoundedCornerShape(12.dp))
            .clickable {
                val base = dateKey.toLocalDateOrNull() ?: LocalDate.now()
                DatePickerDialog(
                    context,
                    { _, y, m, d -> onPick(LocalDate.of(y, m + 1, d).toString()) },
                    base.year, base.monthValue - 1, base.dayOfMonth
                ).show()
            }
            .padding(12.dp)
    ) {
        Text(
            text = if (dateKey.isBlank()) "—" else fullDateLabel(dateKey),
            fontSize = 15.sp,
            color = if (dateKey.isBlank()) GreyText else Color.Black
        )
    }
}

// ধারের অ্যাকাউন্ট ড্রপডাউন: ট্রান্সফারের ৬টি খাত + নিজের ক্যাশ
// অ্যাকাউন্ট, প্রতিটার পাশে বর্তমান ব্যালেন্স।
@Composable
private fun DueAccountDropdown(
    selected: String,
    accounts: List<Account>,
    transactions: List<Transaction>,
    dueItems: List<DueItem>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val customKeys = accounts
        .filter { it.kind == "ক্যাশ" && it.name != "হাতে নগদ" }
        .map { it.name to "🗂️" }
    val options = TRANSFER_FIXED_KEYS + customKeys
    fun balanceFor(key: String): Long =
        if (key == "হাতে নগদ" || customKeys.any { it.first == key }) {
            accountBalanceOf(accounts, transactions, dueItems, key)
        } else {
            savingsTypeBalanceOf(transactions, dueItems, key)
        }
    fun display(key: String): String {
        val emoji = options.find { it.first == key }?.second ?: "💵"
        return emoji + " " + transferDisplayLabel(key) + " • " + moneyText(balanceFor(key))
    }
    Box(Modifier.fillMaxWidth()) {
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
            for ((key, _) in options) {
                DropdownMenuItem(
                    text = { Text(text = display(key)) },
                    onClick = { expanded = false; onSelect(key) }
                )
            }
        }
    }
}

// পরিশোধ/আদায় ডায়ালগ: আংশিক শোধ যায়, বাকির বেশি দেওয়া যায় না।
@Composable
private fun DuePayDialog(
    item: DueItem,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, date: String) -> Unit
) {
    val remaining = dueRemaining(item)
    var amountText by remember { mutableStateOf(remaining.toString()) }
    var dateKey by remember { mutableStateOf(todayString()) }
    var error by remember { mutableStateOf<String?>(null) }
    val isCollect = item.direction == "দিয়েছি"
    val hint = when {
        item.kind == "বাকি" && !isCollect ->
            tr("এটা ব্যয় হিসেবে যোগ হবে এবং হাতে নগদ কমবে।", "This will count as expense and cash in hand will decrease.")
        item.kind == "বাকি" && isCollect ->
            tr("এটা আয় হিসেবে যোগ হবে এবং হাতে নগদ বাড়বে।", "This will count as income and cash in hand will increase.")
        !isCollect ->
            tr("শুধু হাতে নগদ কমবে, ব্যয় ধরা হবে না।", "Only cash in hand will decrease; it won't count as expense.")
        else ->
            tr("শুধু হাতে নগদ বাড়বে, আয় ধরা হবে না।", "Only cash in hand will increase; it won't count as income.")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isCollect) tr("আদায় করুন", "Collect")
                else tr("পরিশোধ করুন", "Pay")
            )
        },
        text = {
            Column {
                Text(
                    text = tr("বাকি", "Remaining") + ": " + moneyText(remaining),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(text = hint, fontSize = 13.sp, color = GreyText)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v -> amountText = v.filter { it.isDigit() } },
                    label = { Text(text = tr("পরিমাণ (৳) *", "Amount (৳) *")) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                DueDateField(
                    label = tr("তারিখ", "Date"),
                    dateKey = dateKey,
                    onPick = { dateKey = it }
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        fontSize = 13.sp,
                        color = RedMain,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toLongOrNull() ?: 0L
                if (amount <= 0L) {
                    error = tr("পরিমাণ লিখুন", "Enter an amount")
                } else if (amount > remaining) {
                    error = tr("বাকির বেশি দেওয়া যাবে না", "Cannot pay more than the remaining amount")
                } else {
                    onConfirm(amount, dateKey)
                }
            }) { Text(text = tr("সংরক্ষণ করুন", "Save")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = tr("বাতিল", "Cancel")) }
        }
    )
}

// এক নামের বিস্তারিত: প্রতিটা হিসাবের তারিখ, বিবরণ, টাকা, বাকি,
// পরিশোধ/আদায় বোতাম, আংশিক শোধের হিসাব, 🗑 মুছুন।
@Composable
fun DueDetailDialog(
    name: String,
    items: List<DueItem>,
    viewModel: AyBayViewModel,
    onDismiss: () -> Unit
) {
    var payItem by remember { mutableStateOf<DueItem?>(null) }
    val totalRemaining = items.sumOf { dueRemaining(it) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = name) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = tr("মোট বাকি", "Total remaining") + ": " + moneyText(totalRemaining),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                for (item in items) {
                    val remaining = dueRemaining(item)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = GreyBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(
                                text = fullDateLabel(item.date) +
                                    if (item.dueDate.isNotBlank()) " • " + tr("ফেরত", "Return") + ": " + fullDateLabel(item.dueDate) else "",
                                fontSize = 12.sp,
                                color = GreyText
                            )
                            if (item.description.isNotBlank()) {
                                Text(text = item.description, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                            if (item.note.isNotBlank()) {
                                Text(text = item.note, fontSize = 13.sp, color = GreyText, modifier = Modifier.padding(top = 2.dp))
                            }
                            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                                Text(
                                    text = moneyText(item.amount),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = tr("বাকি", "Remaining") + ": " + moneyText(remaining),
                                    fontSize = 13.sp,
                                    color = if (remaining > 0L) RedMain else GreenMain
                                )
                            }
                            if (item.paidAmount > 0L) {
                                Text(
                                    text = tr("শোধ হয়েছে", "Paid") + ": " + moneyText(item.paidAmount),
                                    fontSize = 13.sp,
                                    color = GreyText,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                if (remaining > 0L) {
                                    Text(
                                        text = if (item.direction == "দিয়েছি") tr("আদায়", "Collect") else tr("পরিশোধ", "Pay"),
                                        modifier = Modifier.clickable { payItem = item },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JomaBlue
                                    )
                                } else {
                                    Text(
                                        text = tr("শোধ", "Paid"),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenMain
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = "🗑 " + tr("মুছুন", "Delete"),
                                    modifier = Modifier.clickable { viewModel.deleteDueItem(item) },
                                    fontSize = 14.sp,
                                    color = RedMain
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = tr("ঠিক আছে", "OK")) }
        }
    )
    payItem?.let { item ->
        DuePayDialog(
            item = item,
            onDismiss = { payItem = null },
            onConfirm = { amount, date ->
                val newPaid = item.paidAmount + amount
                viewModel.updateDueItem(
                    item.copy(paidAmount = newPaid, isPaid = newPaid >= item.amount)
                )
                if (item.kind == "বাকি") {
                    // বাকি শোধ/আদায় লেনদেন হিসেবেও যোগ হয়।
                    val isPay = item.direction == "নিয়েছি"
                    viewModel.addTransaction(
                        Transaction(
                            type = if (isPay) "ব্যয়" else "আয়",
                            amount = amount,
                            category = if (isPay) "অন্যান্য ব্যয় — বাকি পরিশোধ" else "অন্যান্য — বাকি আদায়",
                            account = "হাতে নগদ",
                            date = date,
                            time = "00:00",
                            note = item.name + if (item.note.isNotBlank()) " — " + item.note else "",
                            payer = item.name
                        )
                    )
                }
                payItem = null
            }
        )
    }
}

// নতুন বাকির ডায়ালগ (জমার মতো): বিবরণ + দ্রুত-চিপ + নাম সেভ
// চেকবক্স + ক্যালকুলেটর + ফেরতের তারিখ।
@Composable
private fun NewDueDialog(viewModel: AyBayViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var saveName by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var dateKey by remember { mutableStateOf(todayString()) }
    var returnDate by remember { mutableStateOf("") }
    var showCalc by remember { mutableStateOf(false) }
    val savedNames = loadSavedDueNames(context)
    val amount = amountText.toLongOrNull() ?: 0L
    val quickChips = listOf(
        listOf("চাল ও ডাল বাজার", "ওষুধ", "মাছ ও সবজি"),
        listOf("ফোন ও রিচার্জ", "বাজার / নগদ")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "🛒 " + tr("নতুন বাকি (কি বাকি নিয়েছি)", "New Due (what I took on credit)")) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                DueInfoBox(
                    text = "🛒 " + tr(
                        "দোকানে কি বাকি নিয়েছেন এবং কত টাকা তা লিখে রাখুন। এখন হাতে নগদ কমবে না, পরে পরিশোধ করলে ব্যয় হিসেবে যোগ হবে।",
                        "Write down what you took on credit and how much. Cash in hand won't decrease now; when you pay later it will count as expense."
                    ),
                    bgColor = YellowTint
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = tr("কি বাকি নিয়েছি? (পণ্যের নাম / বিবরণ)", "What did you take on credit? (item / description)"), fontSize = 13.sp) },
                    placeholder = { Text(text = tr("যেমন: চাল ৫ কেজি, তেল, ওষুধ, মুদি বাজার...", "e.g. rice 5 kg, oil, medicine, groceries..."), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
                for (row in quickChips) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (chip in row) {
                            Text(
                                text = "+ " + chip,
                                modifier = Modifier
                                    .background(GreyBg, RoundedCornerShape(16.dp))
                                    .clickable {
                                        description = if (description.isBlank()) chip
                                        else description + ", " + chip
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = tr("দোকান বা ব্যক্তির নাম (যার থেকে বাকি নিয়েছি) *", "Shop or person name (from whom I took credit) *"), fontSize = 13.sp) },
                    placeholder = { Text(text = tr("যেমন: করিম স্টোর, ফার্মেসি...", "e.g. Karim Store, pharmacy..."), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    singleLine = true
                )
                if (savedNames.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (saved in savedNames) {
                            Text(
                                text = saved,
                                modifier = Modifier
                                    .background(BlueTint, RoundedCornerShape(16.dp))
                                    .clickable { name = saved }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = saveName, onCheckedChange = { saveName = it })
                    Text(
                        text = tr("এই নাম সেভ রাখবো (পরের বার শুধু বেছে নিলেই হবে)", "Save this name (next time just pick it)"),
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { saveName = !saveName }
                    )
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { v -> amountText = v.filter { it.isDigit() } },
                        label = { Text(text = tr("পরিমাণ (৳) *", "Amount (৳) *")) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Text(
                        text = "🧮",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(start = 8.dp).clickable { showCalc = true }
                    )
                }
                DueDateField(
                    label = tr("তারিখ", "Date"),
                    dateKey = dateKey,
                    onPick = { dateKey = it }
                )
                DueDateField(
                    label = tr("ফেরতের তারিখ (ঐচ্ছিক)", "Return date (optional)"),
                    dateKey = returnDate,
                    onPick = { returnDate = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && amount > 0L,
                onClick = {
                    if (saveName) saveDueName(context, name)
                    viewModel.addDueItem(
                        DueItem(
                            kind = "বাকি",
                            direction = "নিয়েছি",
                            name = name.trim(),
                            amount = amount,
                            date = dateKey,
                            dueDate = returnDate,
                            description = description.trim()
                        )
                    )
                    onDismiss()
                }
            ) { Text(text = tr("সংরক্ষণ", "Save")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = tr("বাতিল", "Cancel")) }
        }
    )
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
}

// ধার নেওয়া/দেওয়ার ডায়ালগ (জমার মতো): অ্যাকাউন্ট ড্রপডাউন,
// নাম, নোট/কারণ, পরিমাণ, তারিখ, ফেরতের তারিখ।
@Composable
private fun LoanDialog(
    viewModel: AyBayViewModel,
    direction: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val dueItems by viewModel.dueItems.collectAsState()
    var accountKey by remember { mutableStateOf("হাতে নগদ") }
    var name by remember { mutableStateOf("") }
    var saveName by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dateKey by remember { mutableStateOf(todayString()) }
    var returnDate by remember { mutableStateOf("") }
    var showCalc by remember { mutableStateOf(false) }
    val savedNames = loadSavedDueNames(context)
    val isTaken = direction == "নিয়েছি"
    val amount = amountText.toLongOrNull() ?: 0L
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = (if (isTaken) "🔴 " else "🟢 ") + "💵 " + tr(
                    if (isTaken) "ধার নেওয়া (Cash)" else "ধার দেওয়া (Cash)",
                    if (isTaken) "Borrow Cash" else "Lend Cash"
                )
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                DueInfoBox(
                    text = "💵 " + if (isTaken) tr(
                        "নগদ টাকা জমা হচ্ছে (Cash In) — ধার নিলে আপনার 💵 হাতে নগদ (Cash) ব্যালেন্সে এই টাকা যোগ হবে।",
                        "Cash is coming in (Cash In) — borrowing adds this amount to your 💵 cash in hand balance."
                    ) else tr(
                        "নগদ টাকা দেওয়া হচ্ছে (Cash Out) — ধার দিলে আপনার 💵 হাতে নগদ (Cash) ব্যালেন্স থেকে এই টাকা কমবে।",
                        "Cash is going out (Cash Out) — lending subtracts this amount from your 💵 cash in hand balance."
                    ),
                    bgColor = if (isTaken) RedTint else GreenTint
                )
                Text(
                    text = tr("অ্যাকাউন্ট", "Account"),
                    fontSize = 13.sp,
                    color = GreyText,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )
                DueAccountDropdown(
                    selected = accountKey,
                    accounts = accounts,
                    transactions = transactions,
                    dueItems = dueItems,
                    onSelect = { accountKey = it }
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            text = "👤 " + tr(
                                if (isTaken) "কার কাছ থেকে ধার নিয়েছি (নাম) *" else "কাকে ধার দিয়েছি (নাম) *",
                                if (isTaken) "From whom did I borrow? (name) *" else "To whom did I lend? (name) *"
                            ),
                            fontSize = 13.sp
                        )
                    },
                    placeholder = { Text(text = tr("ব্যক্তির নাম লিখুন...", "Write the person's name..."), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    singleLine = true
                )
                if (savedNames.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (saved in savedNames) {
                            Text(
                                text = saved,
                                modifier = Modifier
                                    .background(BlueTint, RoundedCornerShape(16.dp))
                                    .clickable { name = saved }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = saveName, onCheckedChange = { saveName = it })
                    Text(
                        text = tr("এই নাম সেভ রাখবো (পরের বার শুধু বেছে নিলেই হবে)", "Save this name (next time just pick it)"),
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { saveName = !saveName }
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(text = tr("নোট / কারণ (ঐচ্ছিক)", "Note / reason (optional)"), fontSize = 13.sp) },
                    placeholder = { Text(text = tr("যেমন: জরুরি প্রয়োজনে নগদ ধার...", "e.g. cash borrowed for an urgent need..."), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { v -> amountText = v.filter { it.isDigit() } },
                        label = { Text(text = tr("পরিমাণ (৳) *", "Amount (৳) *")) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Text(
                        text = "🧮",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(start = 8.dp).clickable { showCalc = true }
                    )
                }
                DueDateField(
                    label = tr("তারিখ", "Date"),
                    dateKey = dateKey,
                    onPick = { dateKey = it }
                )
                DueDateField(
                    label = tr("ফেরতের তারিখ (ঐচ্ছিক)", "Return date (optional)"),
                    dateKey = returnDate,
                    onPick = { returnDate = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && amount > 0L,
                onClick = {
                    if (saveName) saveDueName(context, name)
                    viewModel.addDueItem(
                        DueItem(
                            kind = "ধার",
                            direction = direction,
                            name = name.trim(),
                            amount = amount,
                            date = dateKey,
                            dueDate = returnDate,
                            note = note.trim(),
                            account = accountKey
                        )
                    )
                    onDismiss()
                }
            ) { Text(text = tr("সংরক্ষণ", "Save")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = tr("বাতিল", "Cancel")) }
        }
    )
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
}

// নামের সারি (বাকি/ধারের তালিকা): নাম + বাকি টাকা, ট্যাপে বিস্তারিত।
@Composable
private fun DueNameRow(
    name: String,
    count: Int,
    remaining: Long,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🏷️", fontSize = 18.sp)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = toBengaliDigits(count.toString()) + " " + tr("টি হিসাব", "entries"),
                fontSize = 12.sp,
                color = GreyText
            )
        }
        Text(
            text = moneyText(remaining),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = tr("শোধ করুন", "Mark paid"),
            fontSize = 13.sp,
            color = JomaBlue,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

// বাজেট সেট ডায়ালগ: মোট মাসিক + দৈনিক (ঐচ্ছিক) + ১৪ খাতের সীমা
// (অন্যান্য ব্যয় বাদে)। খালি রাখা খাতের আগের সীমা মুছে যায়।
@Composable
fun BudgetSetupDialog(
    context: Context,
    monthKey: String,
    budgets: List<Budget>,
    monthTotal: Long,
    monthDaily: Long,
    viewModel: AyBayViewModel,
    onDismiss: () -> Unit,
    onSaved: (Long, Long) -> Unit
) {
    var totalText by remember { mutableStateOf(if (monthTotal > 0L) monthTotal.toString() else "") }
    var dailyText by remember { mutableStateOf(if (monthDaily > 0L) monthDaily.toString() else "") }
    var catLimits by remember {
        mutableStateOf(
            expenseCategories.filter { it != "অন্যান্য ব্যয়" }
                .associateWith { cat -> budgets.find { it.category == cat }?.limitAmount?.toString() ?: "" }
        )
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(
                    text = "🎯 " + tr("মাসিক ও দৈনিক বাজেট সেট", "Set Monthly & Daily Budget"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tr("মোট মাসিক বাজেট (৳)", "Total monthly budget (৳)"),
                    fontSize = 14.sp,
                    color = GreyText,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { v -> totalText = v.filter { it.isDigit() } },
                    placeholder = { Text(text = "0") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = tr("দৈনিক বাজেট (৳) - ঐচ্ছিক", "Daily budget (৳) - optional"),
                    fontSize = 14.sp,
                    color = GreyText,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = dailyText,
                    onValueChange = { v -> dailyText = v.filter { it.isDigit() } },
                    placeholder = { Text(text = "0") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = tr("খাত অনুযায়ী সীমা (ঐচ্ছিক)", "Limits by category (optional)"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                for ((cat, value) in catLimits) {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoryIcon(cat) + " " + categoryLabel(cat),
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = value,
                            onValueChange = { v ->
                                catLimits = catLimits + (cat to v.filter { it.isDigit() })
                            },
                            placeholder = { Text(text = "0") },
                            modifier = Modifier.width(120.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BudgetCancelBg)
                    ) { Text(text = tr("বাতিল", "Cancel"), color = Color.Black) }
                    Button(
                        onClick = {
                            val total = totalText.toLongOrNull() ?: 0L
                            val daily = dailyText.toLongOrNull() ?: 0L
                            saveMonthlyBudgetTotal(context, monthKey, total)
                            saveMonthlyBudgetDaily(context, monthKey, daily)
                            for ((cat, value) in catLimits) {
                                val limit = value.toLongOrNull() ?: 0L
                                val existing = budgets.find { it.category == cat }
                                when {
                                    limit > 0L && existing != null ->
                                        viewModel.updateBudget(existing.copy(limitAmount = limit))
                                    limit > 0L ->
                                        viewModel.addBudget(Budget(category = cat, monthKey = monthKey, limitAmount = limit))
                                    existing != null -> viewModel.deleteBudget(existing)
                                }
                            }
                            onSaved(total, daily)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                    ) { Text(text = tr("সংরক্ষণ", "Save"), color = Color.White) }
                }
            }
        }
    }
}

// খাত অনুযায়ী খরচের বার: ৭০% ও ৯০%-এ সাদা দাগ।
@Composable
private fun BudgetCategoryBar(spent: Long, limit: Long) {
    val frac = if (limit > 0L) (spent.toFloat() / limit).coerceIn(0f, 1f) else 0f
    val barColor = if (spent >= limit) RedMain else GreenMain
    BoxWithConstraints(
        Modifier.fillMaxWidth().height(10.dp).background(TrackGrey, RoundedCornerShape(5.dp))
    ) {
        val w = maxWidth
        Box(
            Modifier.fillMaxWidth(frac).fillMaxHeight()
                .background(barColor, RoundedCornerShape(5.dp))
        )
        Box(
            Modifier.size(2.dp, 10.dp).background(Color.White)
                .offset(x = w * 0.7f - 1.dp)
        )
        Box(
            Modifier.size(2.dp, 10.dp).background(Color.White)
                .offset(x = w * 0.9f - 1.dp)
        )
    }
}

// নাম খাতার ৩ বাক্সের একটা (মোট / লেনদেন / সর্বোচ্চ)।
@Composable
private fun NameLedgerBox(
    title: String,
    big: String,
    small: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(text = title, fontSize = 12.sp, color = GreyText)
            Text(text = big, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = small, fontSize = 12.sp, color = GreyText)
        }
    }
}

@Composable
fun DueScreen(viewModel: AyBayViewModel, onBack: () -> Unit) {
    val items by viewModel.dueItems.collectAsState()
    val deleted by viewModel.deletedTransactions.collectAsState()
    var tab by remember { mutableStateOf(if (HomeNavState.dueTab.isNotBlank()) HomeNavState.dueTab else "বাকি") }
    HomeNavState.dueTab = ""
    var showBin by remember { mutableStateOf(false) }
    var showNewDue by remember { mutableStateOf(false) }
    var showLoanDialog by remember { mutableStateOf<String?>(null) }
    var detailName by remember { mutableStateOf<String?>(null) }

    val dueShown = items.filter { it.kind == "বাকি" }
    val loanShown = items.filter { it.kind == "ধার" }
    val dueTotal = dueShown.sumOf { dueRemaining(it) }
    val duePeople = dueShown.filter { dueRemaining(it) > 0L }.map { it.name }.distinct().size
    val loanTaken = loanShown.filter { it.direction == "নিয়েছি" }.sumOf { dueRemaining(it) }
    val loanGiven = loanShown.filter { it.direction == "দিয়েছি" }.sumOf { dueRemaining(it) }
    val loanTakenPeople = loanShown.filter { it.direction == "নিয়েছি" && dueRemaining(it) > 0L }.map { it.name }.distinct().size
    val loanGivenPeople = loanShown.filter { it.direction == "দিয়েছি" && dueRemaining(it) > 0L }.map { it.name }.distinct().size
    val dueGroups = dueShown.groupBy { it.name }
        .mapValues { (_, list) -> list.sumOf { dueRemaining(it) } to list.size }
        .filter { it.value.first > 0L }.entries.sortedByDescending { it.value.first }
    val loanGroupsTaken = loanShown.filter { it.direction == "নিয়েছি" }.groupBy { it.name }
        .mapValues { (_, list) -> list.sumOf { dueRemaining(it) } to list.size }
        .filter { it.value.first > 0L }.entries.sortedByDescending { it.value.first }
    val loanGroupsGiven = loanShown.filter { it.direction == "দিয়েছি" }.groupBy { it.name }
        .mapValues { (_, list) -> list.sumOf { dueRemaining(it) } to list.size }
        .filter { it.value.first > 0L }.entries.sortedByDescending { it.value.first }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            item {
                JomaSectionHeader(
                    dotColor = JomaBlue,
                    title = tr("🤝 বাকি ও ধার", "Due & Loan")
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
                    Text(
                        text = "‹ " + tr("ফিরে যান", "Back"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JomaBlue,
                        modifier = Modifier.padding(start = 12.dp).clickable { onBack() }
                    )
                }
                // ২ বড় ট্যাব।
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for ((key, label, color) in listOf(
                        Triple("বাকি", tr("🛒 বাকির সেকশন", "🛒 Due Section"), OrangeMain),
                        Triple("ধার", tr("💵 ধারের সেকশন (Cash)", "💵 Loan Section (Cash)"), JomaBlue)
                    )) {
                        val selected = tab == key
                        Box(
                            Modifier.weight(1f)
                                .background(
                                    if (selected) color else Color.White,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, if (selected) color else TrackGrey, RoundedCornerShape(12.dp))
                                .clickable { tab = key }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color.White else GreyText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            if (tab == "বাকি") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = OrangeTint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(text = tr("মোট বাকি", "Total due"), fontSize = 14.sp, color = GreyText)
                                Text(text = moneyText(dueTotal), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OrangeMain)
                            }
                            Text(
                                text = toBengaliDigits(duePeople.toString()) + " " + tr("লেনদেন/মানুষ বাকি", "due entries/people"),
                                fontSize = 13.sp,
                                color = GreyText
                            )
                        }
                    }
                    Button(
                        onClick = { showNewDue = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeMain)
                    ) {
                        Text(text = "+ " + tr("যোগ করুন (নতুন বাকি)", "Add (New Due)"), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    SectionTitle(
                        tr("📊 বাকির তালিকা — দেখায় কি বাকি দিয়েছি", "📊 Due List — shows what I took on credit")
                    )
                }
                if (dueGroups.isEmpty()) {
                    item { EmptyRow(tr("কোনো মানুষের বাকির হিসাব নেই", "No due record for anyone")) }
                } else {
                    items(dueGroups) { (name, pair) ->
                        DueNameRow(
                            name = name,
                            count = pair.second,
                            remaining = pair.first,
                            color = OrangeMain,
                            onClick = { detailName = name }
                        )
                    }
                }
            } else {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = RedTint),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(text = tr("ধার নেওয়া (Cash)", "Borrowed (Cash)"), fontSize = 13.sp, color = GreyText)
                                Text(text = moneyText(loanTaken), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RedMain)
                                Text(
                                    text = toBengaliDigits(loanTakenPeople.toString()) + " " + tr("জনের কাছে দেবো ›", "people to repay"),
                                    fontSize = 12.sp,
                                    color = GreyText
                                )
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = GreenTint),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(text = tr("ধার দেওয়া (Cash)", "Lent (Cash)"), fontSize = 13.sp, color = GreyText)
                                Text(text = moneyText(loanGiven), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GreenMain)
                                Text(
                                    text = toBengaliDigits(loanGivenPeople.toString()) + " " + tr("জনের কাছে পাবো ›", "people to collect from"),
                                    fontSize = 12.sp,
                                    color = GreyText
                                )
                            }
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showLoanDialog = "নিয়েছি" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedMain)
                        ) {
                            Text(text = "+ " + tr("ধার নেওয়া (Cash)", "Borrow (Cash)"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showLoanDialog = "দিয়েছি" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                        ) {
                            Text(text = "+ " + tr("ধার দেওয়া (Cash)", "Lend (Cash)"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    SectionTitle(
                        tr("🔴 ধার নেওয়া (Cash) — যাদের কাছ থেকে ধার নিয়েছি", "🔴 Borrowed (Cash) — people I borrowed from")
                    )
                }
                if (loanGroupsTaken.isEmpty()) {
                    item { EmptyRow(tr("কোনো চলমান ধারের হিসাব নেই", "No active loan records")) }
                } else {
                    items(loanGroupsTaken) { (name, pair) ->
                        DueNameRow(
                            name = name,
                            count = pair.second,
                            remaining = pair.first,
                            color = RedMain,
                            onClick = { detailName = name }
                        )
                    }
                }
                item {
                    SectionTitle(
                        tr("🟢 ধার দেওয়া (Cash) — যাদেরকে ধার দিয়েছি", "🟢 Lent (Cash) — people I lent to")
                    )
                }
                if (loanGroupsGiven.isEmpty()) {
                    item { EmptyRow(tr("কোনো চলমান ধারের হিসাব নেই", "No active loan records")) }
                } else {
                    items(loanGroupsGiven) { (name, pair) ->
                        DueNameRow(
                            name = name,
                            count = pair.second,
                            remaining = pair.first,
                            color = GreenMain,
                            onClick = { detailName = name }
                        )
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
        if (showNewDue) {
            NewDueDialog(viewModel = viewModel, onDismiss = { showNewDue = false })
        }
        showLoanDialog?.let { direction ->
            LoanDialog(viewModel = viewModel, direction = direction, onDismiss = { showLoanDialog = null })
        }
        detailName?.let { name ->
            DueDetailDialog(
                name = name,
                items = items.filter { it.name == name },
                viewModel = viewModel,
                onDismiss = { detailName = null }
            )
        }
    }
}

@Composable
fun BudgetScreen(viewModel: AyBayViewModel, onBack: () -> Unit, onOpenReport: (String) -> Unit) {
    val context = LocalContext.current
    val monthKey = LocalDate.now().toString().substring(0, 7)
    val budgets by viewModel.budgetsOfMonth(monthKey).collectAsState(initial = emptyList())
    val expenseByCat by viewModel.categoryTotals(monthKey, "ব্যয়").collectAsState(initial = emptyMap())
    val transactions by viewModel.transactions.collectAsState()
    var tab by remember { mutableStateOf("বাজেট") }
    var showSetup by remember { mutableStateOf(false) }
    var monthTotal by remember { mutableStateOf(loadMonthlyBudgetTotal(context, monthKey)) }
    var monthDaily by remember { mutableStateOf(loadMonthlyBudgetDaily(context, monthKey)) }
    var ledgerType by remember { mutableStateOf("আয়") }
    var ledgerStart by remember { mutableStateOf<LocalDate?>(null) }
    var ledgerEnd by remember { mutableStateOf<LocalDate?>(null) }
    var ledgerQuery by remember { mutableStateOf("") }

    val monthExpense = expenseByCat.values.sum()
    val monthRemaining = (monthTotal - monthExpense).coerceAtLeast(0L)
    val today = LocalDate.now()
    val daysLeft = today.lengthOfMonth() - today.dayOfMonth + 1
    val dailyAllowed = if (daysLeft > 0) monthRemaining / daysLeft else 0L

    // নাম খাতার তথ্য: প্রদানকারী দিয়ে গোষ্ঠী, নাম না থাকলে খাত দিয়ে।
    val ledgerFiltered = transactions.filter { item ->
        item.type == ledgerType &&
            (ledgerStart == null || item.date >= ledgerStart.toString()) &&
            (ledgerEnd == null || item.date <= ledgerEnd.toString())
    }
    val ledgerGroups = ledgerFiltered
        .groupBy { item ->
            if (item.payer.isNotBlank()) item.payer to false
            else item.category.substringBefore(" — ") to true
        }
        .mapValues { (_, list) ->
            if (ledgerType == "সঞ্চয়") {
                list.sumOf { if (it.direction == "উত্তোলন") -it.amount else it.amount } to list.size
            } else {
                list.sumOf { it.amount } to list.size
            }
        }
        .entries.sortedByDescending { it.value.first }
    val ledgerSearched = if (ledgerQuery.isBlank()) ledgerGroups
    else ledgerGroups.filter { (key, _) ->
        key.first.contains(ledgerQuery, ignoreCase = true) ||
            categoryLabel(key.first).contains(ledgerQuery, ignoreCase = true)
    }
    val ledgerTotal = if (ledgerType == "সঞ্চয়") {
        ledgerFiltered.sumOf { if (it.direction == "উত্তোলন") -it.amount else it.amount }
    } else ledgerFiltered.sumOf { it.amount }
    val ledgerMax = ledgerGroups.maxByOrNull { it.value.first }
    val ledgerColor = when (ledgerType) {
        "আয়" -> GreenMain
        "ব্যয়" -> RedMain
        else -> YellowMain
    }
    val ledgerBg = when (ledgerType) {
        "আয়" -> GreenTint
        "ব্যয়" -> RedTint
        else -> YellowTint
    }

    fun pickLedgerDate(initial: LocalDate?, onPick: (LocalDate) -> Unit) {
        val base = initial ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, y, m, d -> onPick(LocalDate.of(y, m + 1, d)) },
            base.year, base.monthValue - 1, base.dayOfMonth
        ).show()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp)
                        .background(Color.White, RoundedCornerShape(16.dp)).padding(6.dp)
                ) {
                    for ((key, label) in listOf(
                        "বাজেট" to tr("🎯 বাজেট", "🎯 Budget"),
                        "খাতা" to tr("📘 নাম খাতা", "📘 Name Ledger")
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
            if (tab == "বাজেট") {
                item {
                    JomaSectionHeader(
                        dotColor = JomaBlue,
                        title = tr("🎯 মাসিক বাজেট", "🎯 Monthly Budget")
                    ) {
                        Text(
                            text = "‹ " + tr("ফিরে যান", "Back"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JomaBlue,
                            modifier = Modifier.clickable { onBack() }
                        )
                    }
                    Text(
                        text = tr("মাসের খরচের সীমা ঠিক করুন, সীমা পার হলে সতর্কতা পাবেন", "Set a monthly spending limit; you'll be warned if you cross it"),
                        fontSize = 13.sp,
                        color = GreyText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    if (monthTotal <= 0L) {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                Modifier.background(BudgetPillBg, RoundedCornerShape(20.dp))
                                    .border(1.dp, TrackGrey, RoundedCornerShape(20.dp))
                                    .clickable { showSetup = true }
                                    .padding(horizontal = 22.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "➕ " + tr("বাজেট সেট করুন", "Set Budget"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    } else {
                        val pct = monthExpense.toFloat() / monthTotal
                        val bannerText = when {
                            pct >= 1f -> tr("বাজেট", "Budget") + " " + moneyText(monthTotal) + " " +
                                tr("ছাড়িয়ে গেছে! আর খরচ কমান।", "exceeded! Cut back on spending.")
                            pct >= 0.9f -> tr("প্রায় শেষ! বাজেটের ৯০%-এর বেশি খরচ হয়ে গেছে।", "Almost done! More than 90% of the budget is spent.")
                            pct >= 0.7f -> tr("সতর্ক হোন! বাজেটের ৭০%-এর বেশি খরচ হয়ে গেছে।", "Careful! More than 70% of the budget is spent.")
                            else -> tr("খরচ নিয়ন্ত্রণে আছে, চালিয়ে যান।", "Spending is under control, keep going.")
                        }
                        val bannerBg = when {
                            pct >= 1f -> RedTint
                            pct >= 0.9f -> OrangeTint
                            pct >= 0.7f -> YellowTint
                            else -> GreenTint
                        }
                        Box(
                            Modifier.fillMaxWidth().padding(top = 12.dp)
                                .background(bannerBg, RoundedCornerShape(12.dp)).padding(12.dp)
                        ) {
                            Text(text = bannerText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = "📅 " + tr("বাকি", "Remaining") + " " +
                                toBengaliDigits(daysLeft.toString()) + " " +
                                tr("দিনে গড়ে দিনে", "days, on average per day") + " " +
                                moneyText(dailyAllowed) + " " + tr("খরচ করতে পারবেন", "you can spend"),
                            fontSize = 13.sp,
                            color = GreyText,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Row(
                            Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NameLedgerBox(
                                title = tr("মোট বাজেট", "Total budget"),
                                big = moneyText(monthTotal),
                                small = "",
                                color = JomaBlue,
                                bgColor = BlueTint,
                                modifier = Modifier.weight(1f)
                            )
                            NameLedgerBox(
                                title = tr("খরচ", "Spent"),
                                big = moneyText(monthExpense),
                                small = "",
                                color = RedMain,
                                bgColor = RedTint,
                                modifier = Modifier.weight(1f)
                            )
                            NameLedgerBox(
                                title = tr("বাকি", "Remaining"),
                                big = moneyText(monthRemaining),
                                small = "",
                                color = GreenMain,
                                bgColor = GreenTint,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "✏️ " + tr("বাজেট বদলান", "Change budget"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = JomaBlue,
                                modifier = Modifier.clickable { showSetup = true }
                            )
                        }
                    }
                    SectionTitle(tr("খাত অনুযায়ী বাজেট", "Budget by Category"))
                }
                if (budgets.isEmpty()) {
                    item { EmptyRow(tr("এখনো কোনো বাজেট সেট করা হয়নি", "No budget set yet")) }
                } else {
                    items(budgets) { budget ->
                        val spent = expenseByCat[budget.category] ?: 0L
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = categoryLabel(budget.category),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = moneyText(spent) + " / " + moneyText(budget.limitAmount),
                                        fontSize = 13.sp,
                                        color = GreyText
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                BudgetCategoryBar(spent = spent, limit = budget.limitAmount)
                            }
                        }
                    }
                }
            } else {
                item {
                    JomaSectionHeader(
                        dotColor = JomaBlue,
                        title = tr("📘 নাম অনুযায়ী খাতা", "📘 Ledger by Name")
                    ) {
                        Text(
                            text = "‹ " + tr("ফিরে যান", "Back"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JomaBlue,
                            modifier = Modifier.clickable { onBack() }
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 " +
                                (ledgerStart?.let { fullDateLabel(it.toString()) } ?: tr("শুরু থেকে", "From start")) +
                                " — " +
                                (ledgerEnd?.let { fullDateLabel(it.toString()) } ?: tr("বর্তমান পর্যন্ত", "Until now")),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "↺ " + tr("সব তারিখ", "All dates"),
                            fontSize = 13.sp,
                            color = JomaBlue,
                            modifier = Modifier.clickable { ledgerStart = null; ledgerEnd = null }
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            Modifier.weight(1f).background(GreyBg, RoundedCornerShape(10.dp))
                                .clickable { pickLedgerDate(ledgerStart) { ledgerStart = it } }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = tr("শুরুর তারিখ", "Start date"), fontSize = 13.sp)
                        }
                        Box(
                            Modifier.weight(1f).background(GreyBg, RoundedCornerShape(10.dp))
                                .clickable { pickLedgerDate(ledgerEnd) { ledgerEnd = it } }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = tr("শেষের তারিখ", "End date"), fontSize = 13.sp)
                        }
                    }
                    // ৩ পিল: আয় / ব্যয় / সঞ্চয়।
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for ((key, label, colors) in listOf(
                            Triple("আয়", "💰 " + tr("আয়", "Income"), GreenMain to GreenTint),
                            Triple("ব্যয়", "💸 " + tr("ব্যয়", "Expense"), RedMain to RedTint),
                            Triple("সঞ্চয়", "🏦 " + tr("সঞ্চয়", "Savings"), YellowMain to YellowTint)
                        )) {
                            val (color, bg) = colors
                            val selected = ledgerType == key
                            val borderColor = when (key) {
                                "আয়" -> GreenDark
                                "ব্যয়" -> LedgerRed
                                else -> LedgerBrown
                            }
                            Box(
                                Modifier.weight(1f)
                                    .background(if (selected) color else bg, RoundedCornerShape(20.dp))
                                    .then(if (selected) Modifier.border(2.dp, borderColor, RoundedCornerShape(20.dp)) else Modifier)
                                    .clickable { ledgerType = key }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (if (selected) "✓ " else "") + label,
                                    color = if (selected) Color.White else Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    // ৩ বাক্স।
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NameLedgerBox(
                            title = tr(
                                "মোট " + ledgerType,
                                "Total " + when (ledgerType) {
                                    "আয়" -> "Income"
                                    "ব্যয়" -> "Expense"
                                    else -> "Savings"
                                }
                            ),
                            big = moneyText(ledgerTotal),
                            small = "",
                            color = ledgerColor,
                            bgColor = ledgerBg,
                            modifier = Modifier.weight(1f)
                        )
                        NameLedgerBox(
                            title = tr("লেনদেন", "Transactions"),
                            big = toBengaliDigits(ledgerFiltered.size.toString()),
                            small = toBengaliDigits(ledgerGroups.size.toString()) + " " + tr("টি নাম/খাত", "names/categories"),
                            color = ledgerColor,
                            bgColor = ledgerBg,
                            modifier = Modifier.weight(1f)
                        )
                        NameLedgerBox(
                            title = tr("সর্বোচ্চ", "Highest"),
                            big = ledgerMax?.let { moneyText(it.value.first) } ?: moneyText(0L),
                            small = ledgerMax?.let {
                                if (it.key.second) categoryLabel(it.key.first) else it.key.first
                            } ?: "",
                            color = ledgerColor,
                            bgColor = ledgerBg,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = ledgerQuery,
                        onValueChange = { ledgerQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        label = { Text("🔍 " + tr("নাম বা খাতা লিখে খুঁজুন...", "Search by name or ledger..."), fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("নাম / খাত", "Name / Ledger"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreyText,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = tr("লেনদেন", "Transactions"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreyText,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(
                            text = tr("মোট", "Total"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreyText
                        )
                    }
                }
                if (ledgerSearched.isEmpty()) {
                    item { EmptyRow(tr("এই সময়ে কোনো লেনদেন নেই", "No transactions in this period")) }
                } else {
                    val maxTotal = ledgerSearched.maxOf { it.value.first }.coerceAtLeast(1L)
                    items(ledgerSearched) { (key, pair) ->
                        val (groupName, isCategory) = key
                        val (total, count) = pair
                        Column(
                            Modifier.fillMaxWidth().clickable { onOpenReport(groupName) }.padding(vertical = 8.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🏷️", fontSize = 16.sp)
                                Text(
                                    text = if (isCategory) categoryLabel(groupName) else groupName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                                )
                                if (isCategory) {
                                    Text(
                                        text = tr("খাত", "Category"),
                                        fontSize = 11.sp,
                                        color = GreyText,
                                        modifier = Modifier
                                            .background(GreyBg, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(
                                    text = toBengaliDigits(count.toString()),
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(
                                    text = moneyText(total),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ledgerColor
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Box(
                                Modifier.fillMaxWidth().height(6.dp)
                                    .background(TrackGrey, RoundedCornerShape(3.dp))
                            ) {
                                Box(
                                    Modifier.fillMaxWidth(
                                        (total.toFloat() / maxTotal).coerceIn(0f, 1f)
                                    ).fillMaxHeight()
                                        .background(ledgerColor, RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp)
                            .background(GreyBg, RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("মোট", "Total"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = toBengaliDigits(ledgerFiltered.size.toString()),
                            fontSize = 15.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(
                            text = moneyText(ledgerTotal),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ledgerColor
                        )
                    }
                    Text(
                        text = tr("নামে ট্যাপ করলে ওই নামের প্রতিটি লেনদেন দেখা যাবে। নাম না থাকলে লেনদেন খাত অনুযায়ী সাজানো আছে।", "Tap a name to see all its transactions. When there is no name, transactions are grouped by category."),
                        fontSize = 12.sp,
                        color = GreyText,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
        if (showSetup) {
            BudgetSetupDialog(
                context = context,
                monthKey = monthKey,
                budgets = budgets,
                monthTotal = monthTotal,
                monthDaily = monthDaily,
                viewModel = viewModel,
                onDismiss = { showSetup = false },
                onSaved = { total, daily ->
                    monthTotal = total
                    monthDaily = daily
                    showSetup = false
                }
            )
        }
    }
}
