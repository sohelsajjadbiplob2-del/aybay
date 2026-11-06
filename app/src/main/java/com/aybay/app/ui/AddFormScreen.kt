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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aybay.app.data.Account
import com.aybay.app.data.Transaction
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AddFormScreen(viewModel: AyBayViewModel, initialType: String, onSaved: () -> Unit) {
    var type by remember { mutableStateOf(if (initialType == "ট্রান্সফার") "ব্যয়" else initialType) }
    var category by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("হাতে নগদ") }
    var note by remember { mutableStateOf("") }
    var payer by remember { mutableStateOf("") }
    var showAccountMenu by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val accounts by viewModel.accounts.collectAsState()
    val categories = when (type) {
        "আয়" -> incomeCategories
        "ব্যয়" -> expenseCategories
        else -> listOf("সঞ্চয়", "অন্যান্য")
    }
    val mainColor = when (type) {
        "আয়" -> GreenMain
        "ব্যয়" -> RedMain
        else -> BlueMain
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // ট্যাব
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (t in listOf("আয়", "ব্যয়", "সঞ্চয়")) {
                val selected = type == t
                Text(
                    text = tr(t, if (t == "আয়") "Income" else if (t == "ব্যয়") "Expense" else "Savings"),
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selected) mainColor else GreyBg,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 10.dp)
                        .clickable {
                            type = t
                            category = ""
                        },
                    color = if (selected) Color.White else Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // খাত
        Text(text = tr("খাত", "Category"), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val selected = category == cat
                Text(
                    text = categoryLabel(cat),
                    modifier = Modifier
                        .background(
                            if (selected) GreenDark else GreyBg,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(vertical = 10.dp)
                        .clickable { category = cat },
                    color = if (selected) Color.White else Color.Black,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // টাকা কোথায়
        Text(text = tr("টাকা কোথায় জমা হচ্ছে", "Where is the money"), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreyBg, RoundedCornerShape(10.dp))
                .padding(12.dp)
                .clickable { showAccountMenu = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = account, modifier = Modifier.weight(1f), fontSize = 15.sp)
            Text(text = "▼", fontSize = 14.sp, color = GreyText)
            DropdownMenu(expanded = showAccountMenu, onDismissRequest = { showAccountMenu = false }) {
                val allAccounts: List<Account> = accounts
                DropdownMenuItem(
                    text = { Text(text = tr("হাতে নগদ", "Cash in hand")) },
                    onClick = { account = "হাতে নগদ"; showAccountMenu = false }
                )
                for (acc in allAccounts) {
                    if (acc.name != "হাতে নগদ") {
                        DropdownMenuItem(
                            text = { Text(text = acc.name) },
                            onClick = { account = acc.name; showAccountMenu = false }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // পরিমাণ
        OutlinedTextField(
            value = amountText,
            onValueChange = { v -> amountText = v.filter { it.isDigit() } },
            label = { Text(text = tr("পরিমাণ *", "Amount *")) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        // তারিখ ও সময়, নোট
        Text(
            text = tr("তারিখ ও সময়", "Date & time") + ": " + dateLabel(todayString()),
            fontSize = 14.sp,
            color = GreyText
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(text = tr("নোট (ঐচ্ছিক)", "Note (optional)")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = payer,
            onValueChange = { payer = it },
            label = { Text(text = tr("প্রদানকারীর তথ্য (ঐচ্ছিক)", "Payer info (optional)")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        // সংখ্যা কীবোর্ড
        NumberKeypad(onKey = { key ->
            amountText = when (key) {
                "⌫" -> if (amountText.isNotEmpty()) amountText.dropLast(1) else ""
                else -> amountText + key
            }
        })
        Spacer(modifier = Modifier.height(16.dp))
        val amount = amountText.toLongOrNull() ?: 0L
        val canSave = amount > 0L && category.isNotBlank()
        Button(
            onClick = {
                val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                val finalType = if (type == "ট্রান্সফার") "ব্যয়" else type
                viewModel.addTransaction(
                    Transaction(
                        type = finalType,
                        amount = amount,
                        category = category,
                        account = account,
                        date = todayString(),
                        time = now,
                        note = note,
                        payer = payer
                    )
                )
                saved = true
                onSaved()
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = mainColor)
        ) {
            Text(
                text = tr(type + " সংরক্ষণ করুন", "Save " + if (type == "আয়") "Income" else if (type == "ব্যয়") "Expense" else "Savings"),
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
