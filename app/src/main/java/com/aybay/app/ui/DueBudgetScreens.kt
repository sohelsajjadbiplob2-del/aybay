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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import com.aybay.app.data.Budget
import com.aybay.app.data.DueItem
import java.time.LocalDate

@Composable
fun DueScreen(viewModel: AyBayViewModel) {
    val items by viewModel.dueItems.collectAsState()
    var tab by remember { mutableStateOf("বাকি") }
    var showForm by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf("নিয়েছি") }
    val shown = items.filter { it.kind == tab }
    val total = shown.filter { !it.isPaid }.sumOf { it.amount }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (t in listOf("বাকি", "ধার")) {
                    val selected = tab == t
                    Text(
                        text = tr(t, if (t == "বাকি") "Due" else "Loan"),
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selected) GreenDark else GreyBg, RoundedCornerShape(16.dp))
                            .padding(vertical = 10.dp)
                            .clickable { tab = t },
                        color = if (selected) Color.White else Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GreenLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = tr("মোট", "Total") + " " + tr(tab, if (tab == "বাকি") "Due" else "Loan"), fontSize = 14.sp, color = GreyText)
                    Text(text = moneyText(total), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GreenDark)
                }
            }
            Button(
                onClick = { showForm = !showForm },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) {
                Text(text = tr("যোগ করুন (নতুন", "Add (New") + " " + tr(tab, if (tab == "বাকি") "Due" else "Loan") + ")", fontSize = 15.sp)
            }
            if (showForm) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(text = tr("দোকান / ব্যক্তির নাম", "Shop / Person name")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (d in listOf("নিয়েছি", "দিয়েছি")) {
                            val selected = direction == d
                            Text(
                                text = tr(d, if (d == "নিয়েছি") "Taken" else "Given"),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (selected) GreenDark else GreyBg, RoundedCornerShape(10.dp))
                                    .padding(vertical = 10.dp)
                                    .clickable { direction = d },
                                color = if (selected) Color.White else Color.Black,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { v -> amountText = v.filter { it.isDigit() } },
                        label = { Text(text = tr("পরিমাণ *", "Amount *")) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val amount = amountText.toLongOrNull() ?: 0L
                    Button(
                        onClick = {
                            viewModel.addDueItem(
                                DueItem(
                                    kind = tab,
                                    direction = direction,
                                    name = name,
                                    amount = amount,
                                    date = todayString()
                                )
                            )
                            name = ""
                            amountText = ""
                            showForm = false
                        },
                        enabled = amount > 0L && name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                    ) {
                        Text(text = tr("সংরক্ষণ করুন", "Save"), fontSize = 15.sp)
                    }
                }
            }
            SectionTitle(tr("তালিকা", "List"))
        }
        if (shown.isEmpty()) {
            item { EmptyRow(tr("কোনো বাকি নেই", "No due items")) }
        } else {
            items(shown) { item ->
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
                            Text(text = item.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = tr(item.direction, if (item.direction == "নিয়েছি") "Taken" else "Given") +
                                    " • " + dateLabel(item.date),
                                fontSize = 12.sp,
                                color = GreyText
                            )
                        }
                        Text(text = moneyText(item.amount), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (item.isPaid) tr("শোধ", "Paid") else tr("শোধ করুন", "Mark paid"),
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .clickable {
                                    viewModel.updateDueItem(item.copy(isPaid = !item.isPaid))
                                },
                            fontSize = 13.sp,
                            color = GreenDark
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun BudgetScreen(viewModel: AyBayViewModel) {
    val monthKey = LocalDate.now().toString().substring(0, 7)
    val budgets by viewModel.budgetsOfMonth(monthKey).collectAsState(initial = emptyList())
    val expenseByCat by viewModel.categoryTotals(monthKey, "ব্যয়").collectAsState(initial = emptyMap())
    var showSet by remember { mutableStateOf(false) }
    var selectedCat by remember { mutableStateOf("বাজার") }
    var limitText by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Text(
                text = tr("মাসিক বাজেট", "Monthly Budget") + " • " + monthLabel(monthKey),
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showSet = !showSet },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) {
                Text(text = tr("বাজেট সেট করুন", "Set Budget"), fontSize = 15.sp)
            }
            if (showSet) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text(text = tr("খাত", "Category"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (cat in expenseCategories.take(6)) {
                            val selected = selectedCat == cat
                            Text(
                                text = categoryLabel(cat),
                                modifier = Modifier
                                    .background(if (selected) GreenDark else GreyBg, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .clickable { selectedCat = cat },
                                color = if (selected) Color.White else Color.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { v -> limitText = v.filter { it.isDigit() } },
                        label = { Text(text = tr("মাসিক সীমা *", "Monthly limit *")) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val limit = limitText.toLongOrNull() ?: 0L
                    Button(
                        onClick = {
                            val existing = budgets.find { it.category == selectedCat }
                            if (existing != null) {
                                viewModel.updateBudget(existing.copy(limitAmount = limit))
                            } else {
                                viewModel.addBudget(Budget(category = selectedCat, monthKey = monthKey, limitAmount = limit))
                            }
                            limitText = ""
                            showSet = false
                        },
                        enabled = limit > 0L,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                    ) {
                        Text(text = tr("সংরক্ষণ করুন", "Save"), fontSize = 15.sp)
                    }
                }
            }
            SectionTitle(tr("খাত অনুযায়ী বাজেট", "Budget by Category"))
        }
        if (budgets.isEmpty()) {
            item { EmptyRow(tr("এখনো কোনো বাজেট সেট করা হয়নি", "No budget set yet")) }
        } else {
            items(budgets) { budget ->
                val spent = expenseByCat[budget.category] ?: 0L
                val frac = if (budget.limitAmount > 0L) (spent.toFloat() / budget.limitAmount).coerceIn(0f, 1f) else 0f
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(text = categoryLabel(budget.category), modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = moneyText(spent) + " / " + moneyText(budget.limitAmount), fontSize = 13.sp, color = GreyText)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { frac },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (frac >= 1f) RedMain else GreenMain
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
