package com.aybay.app.ui

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aybay.app.BuildConfig
import com.aybay.app.data.Transaction
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

// লোকাল ব্যাকআপ: লেনদেনের JSON ফাইল Downloads/আয়-ব্যয় ফোল্ডারে সেভ হয়।
fun exportBackupJson(context: Context, items: List<Transaction>): File? {
    return try {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("type", item.type)
            obj.put("amount", item.amount)
            obj.put("category", item.category)
            obj.put("account", item.account)
            obj.put("date", item.date)
            obj.put("time", item.time)
            obj.put("note", item.note)
            arr.put(obj)
        }
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "আয়-ব্যয়"
        )
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "backup.json")
        FileOutputStream(file).use { it.write(arr.toString().toByteArray()) }
        file
    } catch (e: Exception) {
        null
    }
}

fun parseBackupJson(text: String): List<Transaction> {
    val out = mutableListOf<Transaction>()
    try {
        val arr = JSONArray(text)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            out.add(
                Transaction(
                    type = obj.optString("type", "ব্যয়"),
                    amount = obj.optLong("amount", 0L),
                    category = obj.optString("category", "অন্যান্য"),
                    account = obj.optString("account", "হাতে নগদ"),
                    date = obj.optString("date", todayString()),
                    time = obj.optString("time", "00:00"),
                    note = obj.optString("note", "")
                )
            )
        }
    } catch (e: Exception) {
    }
    return out
}

@Composable
fun SettingsScreen(
    viewModel: AyBayViewModel,
    prefs: AppPrefs,
    onPrefsChange: (AppPrefs) -> Unit,
    onImportBackup: (Uri) -> Unit
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    var pinText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Text(
                text = tr("সেটিংস", "Settings"),
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            SectionTitle(tr("ভাষা", "Language"))
            Row(modifier = Modifier.fillMaxWidth()) {
                for (lang in listOf("bn" to "বাংলা", "en" to "English")) {
                    val selected = prefs.language == lang.first
                    Text(
                        text = lang.second,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { onPrefsChange(prefs.copy(language = lang.first)) },
                        fontSize = 15.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) GreenDark else GreyText
                    )
                }
            }
            SectionTitle(tr("কারেন্সি", "Currency"))
            Text(text = "BDT (৳)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            SectionTitle(tr("থিম", "Theme"))
            Text(
                text = if (prefs.darkTheme) tr("গাঢ়", "Dark") else tr("হালকা", "Light"),
                modifier = Modifier.clickable { onPrefsChange(prefs.copy(darkTheme = !prefs.darkTheme)) },
                fontSize = 15.sp,
                color = GreenDark
            )
            SectionTitle(tr("পিন ও গোপনীয়তা", "PIN & Privacy"))
            OutlinedTextField(
                value = pinText,
                onValueChange = { v -> pinText = v.filter { it.isDigit() }.take(6) },
                label = { Text(text = tr("নতুন পিন (৪-৬ সংখ্যা)", "New PIN (4-6 digits)")) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (pinText.length >= 4) {
                        onPrefsChange(prefs.copy(pin = pinText))
                        pinText = ""
                        message = tr("পিন সেট হয়েছে", "PIN set")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) {
                Text(text = tr("পিন সেট করুন", "Set PIN"), fontSize = 15.sp)
            }
            if (prefs.pin.isNotBlank()) {
                Text(
                    text = tr("পিন মুছে ফেলুন", "Remove PIN"),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { onPrefsChange(prefs.copy(pin = "")) },
                    fontSize = 14.sp,
                    color = RedMain
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tr("ফিঙ্গারপ্রিন্ট আনলক", "Fingerprint unlock") + ": " +
                    if (prefs.biometricEnabled) tr("চালু", "On") else tr("বন্ধ", "Off"),
                modifier = Modifier.clickable {
                    onPrefsChange(prefs.copy(biometricEnabled = !prefs.biometricEnabled))
                },
                fontSize = 15.sp,
                color = GreenDark
            )
            SectionTitle(tr("লাইভ সিঙ্ক", "Live Sync"))
            val syncOn = viewModel.sync.isAvailable
            Text(
                text = if (syncOn) {
                    tr("সিঙ্ক চালু", "Sync On") + " • " + (viewModel.sync.signedInEmail ?: "")
                } else {
                    tr("সিঙ্ক বন্ধ আছে", "Sync is off") + " — " +
                        tr("Firebase সেটআপ বাকি", "Firebase setup pending")
                },
                fontSize = 14.sp,
                color = GreyText
            )
            SectionTitle(tr("অটো আয়", "Auto Income"))
            var autoAmountText by remember { mutableStateOf(if (prefs.autoSalaryAmount > 0L) prefs.autoSalaryAmount.toString() else "") }
            var autoDayText by remember { mutableStateOf(prefs.autoSalaryDay.toString()) }
            Text(
                text = tr("অটো আয়", "Auto income") + ": " +
                    if (prefs.autoSalaryEnabled) tr("চালু", "On") else tr("বন্ধ", "Off"),
                modifier = Modifier.clickable {
                    onPrefsChange(prefs.copy(autoSalaryEnabled = !prefs.autoSalaryEnabled))
                },
                fontSize = 15.sp,
                color = GreenDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = autoAmountText,
                onValueChange = { v -> autoAmountText = v.filter { it.isDigit() } },
                label = { Text(text = tr("মাসিক অটো আয়ের পরিমাণ", "Monthly auto income amount")) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = autoDayText,
                onValueChange = { v -> autoDayText = v.filter { it.isDigit() }.take(2) },
                label = { Text(text = tr("মাসের কত তারিখ (১-২৮)", "Day of month (1-28)")) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val amt = autoAmountText.toLongOrNull() ?: 0L
                    val day = (autoDayText.toIntOrNull() ?: 1).coerceIn(1, 28)
                    onPrefsChange(prefs.copy(autoSalaryAmount = amt, autoSalaryDay = day))
                    message = tr("অটো আয় সেট হয়েছে", "Auto income set")
                },
                enabled = (autoAmountText.toLongOrNull() ?: 0L) > 0L,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) {
                Text(text = tr("অটো আয় সংরক্ষণ করুন", "Save Auto Income"), fontSize = 15.sp)
            }
            SectionTitle(tr("ব্যাকআপ", "Backup"))
            Button(
                onClick = {
                    val file = exportBackupJson(context, transactions)
                    message = if (file != null) {
                        tr("ব্যাকআপ তৈরি হয়েছে", "Backup created")
                    } else {
                        tr("ব্যাকআপ হয়নি", "Backup failed")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
            ) {
                Text(text = tr("লোকাল ব্যাকআপ নিন", "Take Local Backup"), fontSize = 15.sp)
            }
            if (message.isNotBlank()) {
                Text(text = message, fontSize = 13.sp, color = GreenDark, modifier = Modifier.padding(top = 6.dp))
            }
            SectionTitle(tr("অ্যাপ সম্পর্কে", "About App"))
            Text(text = tr("আয় - ব্যয়", "Ay - Bay"), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                text = tr("সংস্করণ", "Version") + " " + BuildConfig.VERSION_NAME,
                fontSize = 14.sp,
                color = GreyText
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
