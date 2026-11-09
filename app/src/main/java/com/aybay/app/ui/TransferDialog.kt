package com.aybay.app.ui

import android.app.DatePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aybay.app.data.Account
import com.aybay.app.data.Transaction
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ট্রান্সফারের "নতুন অ্যাকাউন্ট..." অপশনের চাবি (আসল অ্যাকাউন্টের নাম নয়)।
private const val NEW_ACCOUNT_KEY = "__new__"

// ট্রান্সফারের নির্দিষ্ট ৬টি খাত: (সংরক্ষণের চাবি, আইকন)।
// চাবিগুলো অ্যাপের বিদ্যমান নামের সঙ্গে মেলে — নগদ = Account টেবিলের
// "হাতে নগদ", বাকিগুলো সঞ্চয়ের ধরনের খাতের নাম — যাতে ব্যালেন্সের
// হিসাব (অ্যাকাউন্ট ব্যালেন্স, সঞ্চয়ের ধরনের জমা) ঠিকমতো বদলায়।
// ধার ডায়ালগেও একই তালিকা ব্যবহার হয়।
val TRANSFER_FIXED_KEYS = listOf(
    "হাতে নগদ" to "💵",
    "ব্যাংক (ডিপিএস, ...)" to "🏦",
    "ফান্ড" to "📈",
    "সোনা বা মূল্যবান..." to "🎖️",
    "মাটির ব্যাংক" to "🏺",
    "অন্যান্য সঞ্চয়" to "➕"
)

// ট্রান্সফারের অ্যাকাউন্ট-চাবি থেকে দেখানোর লেবেল (ডায়ালগ ও লেনদেনের
// তালিকা — দুই জায়গায় একই লেবেল)। নিজের বানানো অ্যাকাউন্টের নাম যেমন
// আছে তেমনই দেখায়।
fun transferDisplayLabel(key: String): String = when (key) {
    "হাতে নগদ" -> tr("নগদ (হাতে)", "Cash in hand")
    "ব্যাংক (ডিপিএস, ...)" -> tr(
        "ব্যাংক (ডিপিএস, এফডিআর, সঞ্চয়পত্র ইত্যাদি)",
        "Bank (DPS, FDR, savings certificates etc.)"
    )
    "সোনা বা মূল্যবান..." -> tr("সোনা বা মূল্যবান ধাতু", "Gold or valuables")
    "ফান্ড" -> tr("ফান্ড", "Fund")
    "মাটির ব্যাংক" -> tr("মাটির ব্যাংক", "Clay bank")
    "অন্যান্য সঞ্চয়" -> tr("অন্যান্য সঞ্চয়", "Other savings")
    else -> key
}

// ট্রান্সফার ডায়ালগ — জমার মতো। এক অ্যাকাউন্ট থেকে অন্য অ্যাকাউন্টে
// টাকা সরানো: আয় বা ব্যয় হিসেবে গণনা হয় না, শুধু দুই অ্যাকাউন্টের
// ব্যালেন্স বদলায়। একটি লেনদেনেই সেভ হয় — type "ট্রান্সফার",
// account = কোথা থেকে, payer = কোথায়।
@Composable
fun TransferDialog(
    viewModel: AyBayViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()

    var fromKey by remember { mutableStateOf("হাতে নগদ") }
    var toKey by remember { mutableStateOf(NEW_ACCOUNT_KEY) }
    var newFromAccountName by remember { mutableStateOf("") }
    var newAccountName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dateKey by remember { mutableStateOf(todayString()) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    // কোন ড্রপডাউনটা খোলা আছে ("from" / "to"), না থাকলে null।
    var expandedField by remember { mutableStateOf<String?>(null) }

    // ড্রপডাউনের তালিকা: নির্দিষ্ট ৬টি + ট্রান্সফার দিয়ে বানানো নিজের
    // অ্যাকাউন্টগুলো + "নতুন অ্যাকাউন্ট..."।
    val customKeys = accounts
        .filter { it.kind == "ক্যাশ" && it.name != "হাতে নগদ" }
        .map { it.name to "🗂️" }
    val options = TRANSFER_FIXED_KEYS + customKeys + (NEW_ACCOUNT_KEY to "➕")

    fun optionLabel(key: String): String =
        if (key == NEW_ACCOUNT_KEY) tr("নতুন অ্যাকাউন্ট...", "New account...")
        else transferDisplayLabel(key)

    fun optionIcon(key: String): String =
        options.firstOrNull { it.first == key }?.second ?: "🗂️"

    fun pickDate() {
        val initial = LocalDate.parse(dateKey)
        DatePickerDialog(
            context,
            { _, y, m, d -> dateKey = LocalDate.of(y, m + 1, d).toString() },
            initial.year, initial.monthValue - 1, initial.dayOfMonth
        ).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "🔁 " + tr(
                        "এক অ্যাকাউন্ট থেকে অন্য অ্যাকাউন্টে ট্রান্সফার",
                        "Transfer from one account to another"
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(GreyBg, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = tr(
                            "ট্রান্সফার আয় বা ব্যয় হিসেবে গণনা হয় না — শুধু দুই অ্যাকাউন্টের ব্যালেন্স বদলায়।",
                            "Transfer is not counted as income or expense — only the two accounts' balances change."
                        ),
                        fontSize = 14.sp,
                        color = GreyText,
                        lineHeight = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // কোথা থেকে
                Text(
                    text = tr("কোথা থেকে", "From"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                TransferAccountField(
                    selectedKey = fromKey,
                    selectedLabel = optionIcon(fromKey) + " " + optionLabel(fromKey),
                    expanded = expandedField == "from",
                    options = options,
                    onToggle = { expandedField = if (expandedField == "from") null else "from" },
                    onSelect = { key ->
                        fromKey = key
                        expandedField = null
                        error = null
                    }
                )
                if (fromKey == NEW_ACCOUNT_KEY) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newFromAccountName,
                        onValueChange = { newFromAccountName = it; error = null },
                        placeholder = {
                            Text(
                                text = tr("নতুন অ্যাকাউন্টের নাম", "New account name"),
                                color = GreyText
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // কোথায়
                Text(
                    text = tr("কোথায়", "To"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                TransferAccountField(
                    selectedKey = toKey,
                    selectedLabel = optionIcon(toKey) + " " + optionLabel(toKey),
                    expanded = expandedField == "to",
                    options = options,
                    onToggle = { expandedField = if (expandedField == "to") null else "to" },
                    onSelect = { key ->
                        toKey = key
                        expandedField = null
                        error = null
                    }
                )
                if (toKey == NEW_ACCOUNT_KEY) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newAccountName,
                        onValueChange = { newAccountName = it; error = null },
                        placeholder = {
                            Text(
                                text = tr("নতুন অ্যাকাউন্টের নাম", "New account name"),
                                color = GreyText
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // পরিমাণ
                Text(
                    text = tr("পরিমাণ (৳)", "Amount (৳)") + " *",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeMain
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { ch -> ch.isDigit() }.take(9)
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text(text = "0", color = GreyText) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeMain,
                        unfocusedBorderColor = TrackGrey
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // তারিখ
                Text(
                    text = tr("তারিখ", "Date"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(GreyBg, RoundedCornerShape(12.dp))
                        .clickable { pickDate() }
                        .padding(14.dp)
                ) {
                    Text(text = fullDateLabel(dateKey), fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                // নোট
                Text(
                    text = tr("নোট (ঐচ্ছিক)", "Note (optional)"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = error!!,
                        fontSize = 13.sp,
                        color = RedMain,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

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
                        Text(
                            text = tr("বাতিল", "Cancel"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            val amount = amountText.toLongOrNull() ?: 0L
                            val fromName =
                                if (fromKey == NEW_ACCOUNT_KEY) newFromAccountName.trim() else fromKey
                            val toName =
                                if (toKey == NEW_ACCOUNT_KEY) newAccountName.trim() else toKey
                            when {
                                amount <= 0L -> error = tr("পরিমাণ লিখুন", "Enter an amount")
                                fromKey == NEW_ACCOUNT_KEY && fromName.isBlank() ->
                                    error = tr(
                                        "নতুন অ্যাকাউন্টের নাম লিখুন",
                                        "Enter the new account name"
                                    )
                                toKey == NEW_ACCOUNT_KEY && toName.isBlank() ->
                                    error = tr(
                                        "নতুন অ্যাকাউন্টের নাম লিখুন",
                                        "Enter the new account name"
                                    )
                                fromName == toName ->
                                    error = tr(
                                        "একই অ্যাকাউন্টে ট্রান্সফার করা যাবে না",
                                        "Cannot transfer to the same account"
                                    )
                                else -> {
                                    if (fromKey == NEW_ACCOUNT_KEY &&
                                        accounts.none { it.name == fromName }
                                    ) {
                                        viewModel.addAccount(
                                            Account(name = fromName, kind = "ক্যাশ")
                                        )
                                    }
                                    if (toKey == NEW_ACCOUNT_KEY &&
                                        accounts.none { it.name == toName }
                                    ) {
                                        viewModel.addAccount(
                                            Account(name = toName, kind = "ক্যাশ")
                                        )
                                    }
                                    viewModel.addTransaction(
                                        Transaction(
                                            type = "ট্রান্সফার",
                                            amount = amount,
                                            category = "ট্রান্সফার",
                                            account = fromName,
                                            date = dateKey,
                                            time = LocalTime.now().format(
                                                DateTimeFormatter.ofPattern("HH:mm")
                                            ),
                                            note = note.trim(),
                                            payer = toName
                                        )
                                    )
                                    onSaved()
                                }
                            }
                        },
                        modifier = Modifier.weight(1.6f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                    ) {
                        Text(
                            text = "💾 " + tr(
                                "ট্রান্সফার সংরক্ষণ করুন",
                                "Save Transfer"
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ড্রপডাউন ঘর: বাছা অ্যাকাউন্ট দেখায়; চাপ দিলে নিচে তালিকা খোলে।
@Composable
private fun TransferAccountField(
    selectedKey: String,
    selectedLabel: String,
    expanded: Boolean,
    options: List<Pair<String, String>>,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(GreyBg, RoundedCornerShape(12.dp))
                .clickable { onToggle() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedLabel,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = if (expanded) "▲" else "▼", fontSize = 13.sp, color = GreyText)
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Color(0xFFFDF4FF), RoundedCornerShape(14.dp))
                    .border(1.dp, TrackGrey, RoundedCornerShape(14.dp))
                    .padding(vertical = 4.dp)
            ) {
                for ((key, icon) in options) {
                    val label = if (key == NEW_ACCOUNT_KEY) {
                        tr("নতুন অ্যাকাউন্ট...", "New account...")
                    } else {
                        transferDisplayLabel(key)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onSelect(key) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = icon + " " + label,
                            fontSize = 16.sp,
                            fontWeight = if (key == selectedKey) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
