package com.aybay.app.ui

import android.content.Context

// ভাষা: অ্যাপের সব UI লেখা tr() দিয়ে লেখা হয় — প্রথম আর্গুমেন্ট বাংলা,
// দ্বিতীয়টা English। ভাষা শুধু বাংলা আর English; হিন্দি নেই।
object LangState {
    var isEnglish: Boolean = false
}

fun tr(bn: String, en: String): String = if (LangState.isEnglish) en else bn

fun isEnglishPref(context: Context): Boolean =
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        .getString("language", "bn") == "en"

fun categoryLabel(category: String): String = when (category) {
    "বাজার" -> tr("বাজার", "Groceries")
    "খাবার" -> tr("খাবার", "Food")
    "যাতায়াত" -> tr("যাতায়াত", "Transport")
    "বিল" -> tr("বিল", "Bills")
    "ধর্মীয় দান" -> tr("ধর্মীয় দান", "Donation")
    "ধর্মীয় দান/সহযোগিতা" -> tr("ধর্মীয় দান/সহযোগিতা", "Donation/Support")
    "হাতখরচ" -> tr("হাতখরচ", "Pocket money")
    "তেল/গ্যাস" -> tr("তেল/গ্যাস", "Fuel/Gas")
    "যানবাহনের তেল/গ্যাস" -> tr("যানবাহনের তেল/গ্যাস", "Vehicle Fuel/Gas")
    "মার্কেট/শপিং" -> tr("মার্কেট/শপিং", "Shopping")
    "গাড়ি ভাড়া" -> tr("গাড়ি ভাড়া", "Vehicle rent")
    "রেস্টুরেন্ট" -> tr("রেস্টুরেন্ট", "Restaurant")
    "বাসা ভাড়া" -> tr("বাসা ভাড়া", "House rent")
    "দোকান ভাড়া" -> tr("দোকান ভাড়া", "Shop rent")
    "ফি" -> tr("ফি", "Fee")
    "মেনটেনেন্স" -> tr("মেনটেনেন্স", "Maintenance")
    "উন্নয়নমূলক খরচ" -> tr("উন্নয়নমূলক খরচ", "Development Expense")
    "কিস্তি" -> tr("কিস্তি", "Installment")
    "অন্যান্য ব্যয়" -> tr("অন্যান্য ব্যয়", "Other Expenses")
    "বেতন" -> tr("বেতন", "Salary")
    "দৈনিক মজুরি" -> tr("দৈনিক মজুরি", "Daily wage")
    "সাপ্তাহিক বেতন" -> tr("সাপ্তাহিক বেতন", "Weekly salary")
    "মাসিক বেতন" -> tr("মাসিক বেতন", "Monthly salary")
    "সঞ্চয়" -> tr("সঞ্চয়", "Savings")
    "ব্যাংক" -> tr("ব্যাংক", "Bank")
    "মোবাইল ব্যাংকিং" -> tr("মোবাইল ব্যাংকিং", "Mobile Banking")
    "ব্যাংক (ডিপিএস, ...)" -> tr("ব্যাংক (ডিপিএস, ...)", "Bank (DPS, ...)")
    "ফান্ড" -> tr("ফান্ড", "Fund")
    "সোনা বা মূল্যবান..." -> tr("সোনা বা মূল্যবান...", "Gold or valuables...")
    "মাটির ব্যাংক" -> tr("মাটির ব্যাংক", "Clay bank")
    "অন্যান্য সঞ্চয়" -> tr("অন্যান্য সঞ্চয়", "Other savings")
    "অন্যান্য" -> tr("অন্যান্য", "Others")
    else -> category
}

fun periodLabel(period: String): String = when (period) {
    "সাপ্তাহিক" -> tr("সাপ্তাহিক", "Weekly")
    "মাসিক" -> tr("মাসিক", "Monthly")
    "বার্ষিক" -> tr("বাৎসরিক", "Yearly")
    else -> tr("দৈনিক", "Daily")
}
