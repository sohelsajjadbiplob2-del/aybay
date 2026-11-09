package com.aybay.app.ui

import android.content.Context

// অ্যাপ প্রেফারেন্স: শুধু এই ফোনে SharedPreferences-এ থাকে, কোথাও
// পাঠানো হয় না। ভাষা বাংলা (bn) বা English (en)। কারেন্সি শুধু BDT।
data class AppPrefs(
    val darkTheme: Boolean = false,
    val language: String = "bn",
    val pin: String = "",
    val securityQuestion: String = "",
    val securityAnswer: String = "",
    val biometricEnabled: Boolean = false,
    val appPinEnabled: Boolean = false,
    val lockedSections: Set<String> = emptySet(),
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
        securityQuestion = prefs.getString("security_question", "") ?: "",
        securityAnswer = prefs.getString("security_answer", "") ?: "",
        biometricEnabled = prefs.getBoolean("biometric_enabled", false),
        appPinEnabled = prefs.getBoolean("app_pin_enabled", false),
        lockedSections = (prefs.getString("locked_sections", "") ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
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
        .putString("security_question", prefs.securityQuestion)
        .putString("security_answer", prefs.securityAnswer)
        .putBoolean("biometric_enabled", prefs.biometricEnabled)
        .putBoolean("app_pin_enabled", prefs.appPinEnabled)
        .putString("locked_sections", prefs.lockedSections.joinToString(","))
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
// সেভ ফরম্যাট: "নাম" (পুরনো) বা "নাম|আইকন" (নতুন)। নামে '|' থাকা যাবে না।
private fun customCategoryRaw(context: Context, type: String): Set<String> =
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        .getStringSet("custom_categories_" + type, emptySet()) ?: emptySet()

private fun splitCustomEntry(entry: String): Pair<String, String> {
    val idx = entry.indexOf('|')
    return if (idx < 0) entry to "🏷️" else entry.substring(0, idx) to entry.substring(idx + 1)
}

private fun joinCustomEntry(name: String, icon: String): String =
    if (icon == "🏷️" || icon.isBlank()) name else name + "|" + icon

private fun saveCustomCategoryRaw(context: Context, type: String, entries: Set<String>) {
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE).edit()
        .putStringSet("custom_categories_" + type, entries).apply()
}

private fun cleanCatName(name: String): String = name.replace("|", "").trim()

fun loadCustomCategories(context: Context, type: String): Set<String> =
    customCategoryRaw(context, type).map { splitCustomEntry(it).first }.toSet()

fun loadCustomCategoryMap(context: Context, type: String): Map<String, String> =
    customCategoryRaw(context, type).map { splitCustomEntry(it) }.toMap()

fun saveCustomCategories(context: Context, type: String, cats: Set<String>) {
    // আইকন না হারিয়ে সেভ: আগে থেকে থাকা নামের আইকন ধরে রাখা হয়।
    val existing = loadCustomCategoryMap(context, type)
    saveCustomCategoryRaw(
        context, type,
        cats.map { joinCustomEntry(it, existing[it] ?: "🏷️") }.toSet()
    )
}

fun saveCustomCategory(context: Context, type: String, name: String, icon: String) {
    val cleanName = cleanCatName(name)
    if (cleanName.isBlank()) return
    val next = loadCustomCategoryMap(context, type).toMutableMap()
    next[cleanName] = icon.ifBlank { "🏷️" }
    saveCustomCategoryRaw(
        context, type,
        next.entries.map { joinCustomEntry(it.key, it.value) }.toSet()
    )
}

fun updateCustomCategoryIcon(context: Context, type: String, name: String, icon: String) {
    saveCustomCategory(context, type, name, icon)
}

fun deleteCustomCategory(context: Context, type: String, name: String) {
    val next = loadCustomCategoryMap(context, type).toMutableMap()
    next.remove(name)
    saveCustomCategoryRaw(
        context, type,
        next.entries.map { joinCustomEntry(it.key, it.value) }.toSet()
    )
    deleteCustomSubcatsFor(context, type, name)
}

fun renameCustomCategory(context: Context, type: String, oldName: String, newName: String) {
    val cleanNew = cleanCatName(newName)
    if (cleanNew.isBlank() || oldName == cleanNew) return
    val map = loadCustomCategoryMap(context, type).toMutableMap()
    val icon = map.remove(oldName) ?: "🏷️"
    map[cleanNew] = icon
    saveCustomCategoryRaw(
        context, type,
        map.entries.map { joinCustomEntry(it.key, it.value) }.toSet()
    )
    renameCustomSubcatCategory(context, type, oldName, cleanNew)
}

// নিজের যোগ করা উপখাত: "খাত|উপখাত" আকারে সেভ থাকে, খাতের ধরন অনুযায়ী।
private fun customSubcatRaw(context: Context, type: String): Set<String> =
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        .getStringSet("custom_subcats_" + type, emptySet()) ?: emptySet()

private fun saveCustomSubcatRaw(context: Context, type: String, entries: Set<String>) {
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE).edit()
        .putStringSet("custom_subcats_" + type, entries).apply()
}

fun loadCustomSubcats(context: Context, type: String, category: String): List<String> =
    customSubcatRaw(context, type).mapNotNull { entry ->
        val idx = entry.indexOf('|')
        if (idx < 0) null
        else if (entry.substring(0, idx) == category) entry.substring(idx + 1)
        else null
    }

fun addCustomSubcat(context: Context, type: String, category: String, subcat: String) {
    val cleanSub = cleanCatName(subcat)
    if (cleanSub.isBlank()) return
    val next = customSubcatRaw(context, type).toMutableSet()
    next.add(category + "|" + cleanSub)
    saveCustomSubcatRaw(context, type, next)
}

fun deleteCustomSubcatsFor(context: Context, type: String, category: String) {
    saveCustomSubcatRaw(
        context, type,
        customSubcatRaw(context, type).filter { it.substringBefore('|') != category }.toSet()
    )
}

fun renameCustomSubcatCategory(context: Context, type: String, oldName: String, newName: String) {
    saveCustomSubcatRaw(
        context, type,
        customSubcatRaw(context, type).map { entry ->
            if (entry.substringBefore('|') == oldName) newName + "|" + entry.substringAfter('|')
            else entry
        }.toSet()
    )
}

// লেনদেন পাতার "খাত অনুযায়ী বিশ্লেষণ"-এ লুকানো খাত: "ধরন|খাত" আকারে
// কমা দিয়ে সেভ থাকে (লক করা সেকশনের মতোই প্যাটার্ন)।
fun loadHiddenAnalysisCats(context: Context): Set<String> =
    (context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        .getString("hidden_analysis_cats", "") ?: "")
        .split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

fun saveHiddenAnalysisCats(context: Context, hidden: Set<String>) {
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE).edit()
        .putString("hidden_analysis_cats", hidden.joinToString(",")).apply()
}

// জমা খরচের আয়ের খাতগুলোর উপখাত (হুবহু); ব্যয় ও সঞ্চয়ে শুধু অন্যান্য।
fun defaultSubcats(type: String, category: String): List<String> = when (type) {
    "আয়" -> when (category) {
        "বেতন" -> listOf("মাসিক বেতন", "দৈনিক মজুরি", "সাপ্তাহিক বেতন", "অন্যান্য")
        "ভাড়া" -> listOf("বাসা ভাড়া", "দোকান ভাড়া", "যানবাহন ভাড়া", "অন্যান্য")
        "অনলাইন ইনকাম" -> listOf("ইউটিউব", "ফেসবুক", "টিকটক", "ওয়েবসাইট", "ফ্রিল্যান্সিং", "অন্যান্য")
        else -> listOf("অন্যান্য")
    }
    else -> listOf("অন্যান্য")
}

fun allSubcats(context: Context, type: String, category: String): List<String> {
    val defaults = defaultSubcats(type, category)
    val base = defaults.filter { it != "অন্যান্য" }
    val customs = loadCustomSubcats(context, type, category).filter { it !in defaults }
    return base + customs + listOf("অন্যান্য")
}

fun allCategories(context: Context, type: String): List<String> {
    val defaults = when (type) {
        "আয়" -> incomeCategories
        "ব্যয়" -> expenseCategories
        else -> listOf("সঞ্চয়", "অন্যান্য")
    }
    return defaults + loadCustomCategories(context, type).filter { it !in defaults }
}

// মাসিক বাজেটের মোট সীমা আর দৈনিক সীমা: মাস অনুযায়ী (yyyy-MM) আলাদা
// চাবিতে থাকে। Budget টেবিলে শুধু খাত-প্রতি সীমা থাকে, মোট/দৈনিক
// এখানে রাখা হয়।
fun loadMonthlyBudgetTotal(context: Context, monthKey: String): Long =
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        .getLong("monthly_budget_total_" + monthKey, 0L)

fun saveMonthlyBudgetTotal(context: Context, monthKey: String, total: Long) {
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE).edit()
        .putLong("monthly_budget_total_" + monthKey, total).apply()
}

fun loadMonthlyBudgetDaily(context: Context, monthKey: String): Long =
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        .getLong("monthly_budget_daily_" + monthKey, 0L)

fun saveMonthlyBudgetDaily(context: Context, monthKey: String, daily: Long) {
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE).edit()
        .putLong("monthly_budget_daily_" + monthKey, daily).apply()
}

// বাকি/ধারে সেভ করা নাম: "এই নাম সেভ রাখবো" চেকবক্সে টিক দিলে নাম
// এখানে জমা হয়, পরের বার নামের ঘরে চিপ হিসেবে দেখায়। কমা দিয়ে
// জোড়া থাকে (লক করা সেকশনের মতোই প্যাটার্ন); নামে কমা থাকা যাবে না।
fun loadSavedDueNames(context: Context): Set<String> =
    (context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        .getString("saved_due_names", "") ?: "")
        .split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

fun saveDueName(context: Context, name: String) {
    val clean = name.trim().replace(",", "")
    if (clean.isBlank()) return
    val next = loadSavedDueNames(context) + clean
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE).edit()
        .putString("saved_due_names", next.joinToString(",")).apply()
}
