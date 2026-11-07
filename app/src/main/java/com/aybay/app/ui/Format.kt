package com.aybay.app.ui

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val bengaliDigits = listOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

// সংখ্যাকে বাংলা অঙ্কে বদলায়। ইংরেজি ভাষা বেছে নিলেও টাকার অঙ্ক
// বাংলাতেই দেখানো হয়, কারণ কারেন্সি শুধু BDT।
fun toBengaliDigits(text: String): String {
    val out = StringBuilder()
    for (ch in text) {
        if (ch in '0'..'9') out.append(bengaliDigits[ch - '0']) else out.append(ch)
    }
    return out.toString()
}

fun moneyText(amount: Long): String {
    val grouped = NumberFormat.getNumberInstance(Locale("en", "IN")).format(amount)
    return "৳" + toBengaliDigits(grouped)
}

fun moneyTextSigned(amount: Long, positive: Boolean): String {
    val sign = if (positive) "+" else "−"
    return sign + moneyText(amount)
}

fun todayString(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

fun monthKeyOf(date: String): String = if (date.length >= 7) date.substring(0, 7) else date

fun monthLabel(monthKey: String): String {
    if (monthKey.length < 7) return monthKey
    val year = monthKey.substring(0, 4)
    val month = monthKey.substring(5, 7).toIntOrNull() ?: 1
    val bnMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val enMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val name = if (LangState.isEnglish) enMonths[month - 1] else bnMonths[month - 1]
    return name + " " + toBengaliDigits(year)
}

fun dateLabel(date: String): String {
    if (date.length < 10) return date
    val day = date.substring(8, 10)
    val month = date.substring(5, 7).toIntOrNull() ?: 1
    val bnMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val enMonths = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val name = if (LangState.isEnglish) enMonths[month - 1] else bnMonths[month - 1]
    return toBengaliDigits(day) + " " + name
}

// পূর্ণ তারিখ: "৬ নভেম্বর ২০২৬"।
fun fullDateLabel(date: String): String {
    if (date.length < 10) return date
    val year = date.substring(0, 4)
    val month = date.substring(5, 7).toIntOrNull() ?: 1
    val day = date.substring(8, 10)
    val bnMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val enMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val name = if (LangState.isEnglish) enMonths[month - 1] else bnMonths[month - 1]
    return toBengaliDigits(day) + " " + name + " " + toBengaliDigits(year)
}

// সময়: "রাত ৮:৫৬"।
fun timeLabel(time: String): String {
    val parts = time.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1) ?: "00"
    val h12 = if (h % 12 == 0) 12 else h % 12
    val period = when (h) {
        in 4..5 -> tr("ভোর", "Early morning")
        in 6..11 -> tr("সকাল", "Morning")
        in 12..15 -> tr("দুপুর", "Afternoon")
        in 16..17 -> tr("বিকেল", "Evening")
        in 18..19 -> tr("সন্ধ্যা", "Evening")
        else -> tr("রাত", "Night")
    }
    return period + " " + toBengaliDigits(h12.toString()) + ":" + toBengaliDigits(m)
}
