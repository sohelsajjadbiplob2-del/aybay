package com.aybay.app.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aybay.app.data.Note
import java.time.LocalDate

@Composable
fun ReportScreen(viewModel: AyBayViewModel) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    // হোমের "নাম অনুযায়ী খাতা" থেকে নামে ট্যাপ করে এলে ওই নাম দিয়ে খোঁজা শুরু হয়।
    var query by remember { mutableStateOf(HomeNavState.reportQuery) }
    HomeNavState.reportQuery = ""
    var filter by remember { mutableStateOf("সব") }
    val monthKey = LocalDate.now().toString().substring(0, 7)
    val monthTotals by viewModel.totalsOfMonth(monthKey).collectAsState(initial = com.aybay.app.data.Totals())
    val shown = transactions.filter { item ->
        val matchesQuery = query.isBlank() ||
            item.category.contains(query, ignoreCase = true) ||
            item.note.contains(query, ignoreCase = true) ||
            item.amount.toString().contains(query)
        val matchesFilter = filter == "সব" || item.type == filter
        matchesQuery && matchesFilter
    }
    var pdfMessage by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Text(
                text = tr("লেনদেনের তালিকা", "Transaction List"),
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                TotalCard(tr("আয়", "Income"), moneyText(monthTotals.income), GreenMain, Modifier.weight(1f))
                Spacer(modifier = Modifier.padding(4.dp))
                TotalCard(tr("ব্যয়", "Expense"), moneyText(monthTotals.expense), RedMain, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(text = tr("নাম, নোট, খাত, পরিমাণ...", "Name, note, category, amount...")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                for (f in listOf("সব", "আয়", "ব্যয়", "সঞ্চয়")) {
                    val selected = filter == f
                    Text(
                        text = if (f == "সব") tr("সব", "All") else tr(f, if (f == "আয়") "Income" else if (f == "ব্যয়") "Expense" else "Savings"),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { filter = f },
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) GreenDark else GreyText
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val file = exportTransactionsPdf(context, monthKey, shown)
                    pdfMessage = if (file != null) {
                        tr("PDF তৈরি হয়েছে", "PDF created")
                    } else {
                        tr("PDF তৈরি হয়নি", "PDF failed")
                    }
                    if (file != null) {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, tr("আয়-ব্যয় রিপোর্ট", "Income-Expense Report") + " • " + monthLabel(monthKey))
                        }
                        context.startActivity(Intent.createChooser(share, tr("শেয়ার করুন", "Share")))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) {
                Text(text = tr("প্রিন্ট / PDF", "Print / PDF"), fontSize = 15.sp)
            }
            if (pdfMessage.isNotBlank()) {
                Text(text = pdfMessage, fontSize = 13.sp, color = GreenDark, modifier = Modifier.padding(top = 6.dp))
            }
            SectionTitle(tr("লেনদেন", "Transactions"))
        }
        if (shown.isEmpty()) {
            item { EmptyRow(tr("কোনো লেনদেন পাওয়া যায়নি", "No transactions found")) }
        } else {
            items(shown) { item ->
                TransactionRow(item, onDelete = { viewModel.deleteTransaction(item) })
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun NotesScreen(viewModel: AyBayViewModel) {
    val notes by viewModel.notes.collectAsState()
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var urgent by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Text(
                text = tr("দ্রুত নোট", "Quick Notes"),
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showForm = !showForm },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) {
                Text(text = tr("নতুন নোট লিখুন", "Write New Note"), fontSize = 15.sp)
            }
            if (showForm) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(text = tr("শিরোনাম", "Title")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text(text = tr("নোটের বিবরণ", "Note details")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("অতি জরুরি নোট", "Urgent note") + ": " + if (urgent) tr("চালু", "On") else tr("বন্ধ", "Off"),
                        modifier = Modifier.clickable { urgent = !urgent },
                        fontSize = 14.sp,
                        color = GreenDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.addNote(Note(title = title, body = body, urgent = urgent))
                            title = ""
                            body = ""
                            urgent = false
                            showForm = false
                        },
                        enabled = title.isNotBlank() || body.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                    ) {
                        Text(text = tr("সংরক্ষণ করুন", "Save"), fontSize = 15.sp)
                    }
                }
            }
            SectionTitle(tr("নোটের তালিকা", "Notes List"))
        }
        if (notes.isEmpty()) {
            item { EmptyRow(tr("কোনো নোট নেই", "No notes yet")) }
        } else {
            items(notes) { note ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = note.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = note.body, fontSize = 14.sp, color = GreyText)
                        if (note.urgent) {
                            Text(text = tr("অতি জরুরি", "Urgent"), fontSize = 12.sp, color = RedMain)
                        }
                    }
                    Text(
                        text = "✕",
                        modifier = Modifier.clickable { viewModel.deleteNote(note) },
                        fontSize = 16.sp,
                        color = GreyText
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
