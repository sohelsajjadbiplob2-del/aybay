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
    val autoSalaryDay: Int = 1,
    val autoSalaryCategory: String = "বেতন",
    val autoDailyEnabled: Boolean = false,
    val autoDailyAmount: Long = 0L,
    val autoDailyCategory: String = "ব্যবসা"
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
        autoSalaryDay = prefs.getInt("auto_salary_day", 1),
        autoSalaryCategory = prefs.getString("auto_salary_category", "বেতন") ?: "বেতন",
        autoDailyEnabled = prefs.getBoolean("auto_daily_enabled", false),
        autoDailyAmount = prefs.getLong("auto_daily_amount", 0L),
        autoDailyCategory = prefs.getString("auto_daily_category", "ব্যবসা") ?: "ব্যবসা"
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
        .putString("auto_salary_category", prefs.autoSalaryCategory)
        .putBoolean("auto_daily_enabled", prefs.autoDailyEnabled)
        .putLong("auto_daily_amount", prefs.autoDailyAmount)
        .putString("auto_daily_category", prefs.autoDailyCategory)
        .apply()
}

// হোমের সেকশনের ক্রম (কার্ড সাজান): কমা দিয়ে জোড়া key-গুলো সেভ থাকে।
val defaultHomeSectionOrder = listOf(
    "chart", "flow", "category", "due", "ledger", "budget", "accounts", "recent"
)

fun loadHomeSectionOrder(context: Context): List<String> {
    val raw = context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        .getString("home_section_order", null) ?: return defaultHomeSectionOrder
    val list = raw.split(",").map { it.trim() }.filter { it in defaultHomeSectionOrder }
    return (list + defaultHomeSectionOrder).distinct()
}

fun saveHomeSectionOrder(context: Context, order: List<String>) {
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE).edit()
        .putString("home_section_order", order.joinToString(",")).apply()
}

// নিজের যোগ করা খাত: ডিফল্ট খাতের সঙ্গে যোগ হয়, Add ফর্মেও দেখা যায়।
fun loadCustomCategories(context: Context, type: String): Set<String> =
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        .getStringSet("custom_categories_" + type, emptySet()) ?: emptySet()

fun saveCustomCategories(context: Context, type: String, cats: Set<String>) {
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE).edit()
        .putStringSet("custom_categories_" + type, cats).apply()
}

fun allCategories(context: Context, type: String): List<String> {
    val defaults = when (type) {
        "আয়" -> incomeCategories
        "ব্যয়" -> expenseCategories
        else -> listOf("সঞ্চয়", "অন্যান্য")
    }
    return defaults + loadCustomCategories(context, type).filter { it !in defaults }
}
