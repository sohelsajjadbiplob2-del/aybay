package com.aybay.app.ui

import android.content.Context

// অ্যাপ প্রেফারেন্স: শুধু এই ফোনে SharedPreferences-এ থাকে, কোথাও
// পাঠানো হয় না। ভাষা বাংলা (bn) বা English (en)। কারেন্সি শুধু BDT।
data class AppPrefs(
    val darkTheme: Boolean = false,
    val language: String = "bn",
    val pin: String = "",
    val biometricEnabled: Boolean = false,
    val autoSalaryEnabled: Boolean = false,
    val autoSalaryAmount: Long = 0L,
    val autoSalaryDay: Int = 1
)

fun loadAppPrefs(context: Context): AppPrefs {
    val prefs = context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
    return AppPrefs(
        darkTheme = prefs.getBoolean("dark_theme", false),
        language = prefs.getString("language", "bn") ?: "bn",
        pin = prefs.getString("pin", "") ?: "",
        biometricEnabled = prefs.getBoolean("biometric_enabled", false),
        autoSalaryEnabled = prefs.getBoolean("auto_salary_enabled", false),
        autoSalaryAmount = prefs.getLong("auto_salary_amount", 0L),
        autoSalaryDay = prefs.getInt("auto_salary_day", 1)
    )
}

fun saveAppPrefs(context: Context, prefs: AppPrefs) {
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE).edit()
        .putBoolean("dark_theme", prefs.darkTheme)
        .putString("language", prefs.language)
        .putString("pin", prefs.pin)
        .putBoolean("biometric_enabled", prefs.biometricEnabled)
        .putBoolean("auto_salary_enabled", prefs.autoSalaryEnabled)
        .putLong("auto_salary_amount", prefs.autoSalaryAmount)
        .putInt("auto_salary_day", prefs.autoSalaryDay)
        .apply()
}
