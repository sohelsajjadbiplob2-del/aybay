package com.aybay.app.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aybay.app.ui.loadAppPrefs
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

// অটো আয় Worker: মাসিক বেতন/আয় (বেছে নেওয়া তারিখে) আর প্রতিদিনের আয়
// নিজে থেকে লেনদেন হিসেবে যোগ করে। একই দিনে/মাসে ডাবল যোগ হবে না —
// শেষ যোগের তারিখ SharedPreferences-এ গার্ড হিসেবে থাকে।
class AutoIncomeWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val prefs = loadAppPrefs(context)
        val today = LocalDate.now()
        val dao = AppDatabase.get(context).dao()
        val sp = context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        val timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

        if (prefs.autoSalaryEnabled && prefs.autoSalaryAmount > 0L) {
            val monthKey = today.toString().substring(0, 7)
            val lastMonthly = sp.getString("auto_monthly_last", "") ?: ""
            val daysInMonth = YearMonth.of(today.year, today.monthValue).lengthOfMonth()
            val effectiveDay = prefs.autoSalaryDay.coerceIn(1, daysInMonth)
            if (today.dayOfMonth == effectiveDay && lastMonthly != monthKey) {
                dao.insertTransaction(
                    Transaction(
                        type = "আয়",
                        amount = prefs.autoSalaryAmount,
                        category = prefs.autoSalaryCategory,
                        account = "হাতে নগদ",
                        date = today.toString(),
                        time = timeNow
                    )
                )
                sp.edit().putString("auto_monthly_last", monthKey).apply()
            }
        }

        if (prefs.autoDailyEnabled && prefs.autoDailyAmount > 0L) {
            val todayKey = today.toString()
            val lastDaily = sp.getString("auto_daily_last", "") ?: ""
            if (lastDaily != todayKey) {
                dao.insertTransaction(
                    Transaction(
                        type = "আয়",
                        amount = prefs.autoDailyAmount,
                        category = prefs.autoDailyCategory,
                        account = "হাতে নগদ",
                        date = todayKey,
                        time = timeNow
                    )
                )
                sp.edit().putString("auto_daily_last", todayKey).apply()
            }
        }
        return Result.success()
    }
}

// প্রতিদিন একবার চেক হবে; সেভ করার সঙ্গে সঙ্গে একবার এখনই চেক হবে,
// যাতে আজকের তারিখ মিললে আজই যোগ হয়।
fun scheduleAutoIncome(context: Context) {
    val periodic = PeriodicWorkRequestBuilder<AutoIncomeWorker>(1, TimeUnit.DAYS).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "auto_income",
        ExistingPeriodicWorkPolicy.KEEP,
        periodic
    )
    val once = OneTimeWorkRequestBuilder<AutoIncomeWorker>().build()
    WorkManager.getInstance(context).enqueue(once)
}
