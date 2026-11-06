package com.aybay.app.ui

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.os.Environment
import com.aybay.app.data.Transaction
import java.io.File
import java.io.FileOutputStream

// PDF রিপোর্ট: Android-এর PdfDocument দিয়ে সাদা পাতায় লেনদেনের
// তালিকা লিখে Downloads/আয়-ব্যয় ফোল্ডারে সেভ করা হয়।
fun exportTransactionsPdf(context: Context, monthKey: String, items: List<Transaction>): File? {
    return try {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val normalPaint = Paint().apply { textSize = 13f }
        var y = 60f
        canvas.drawText("আয় - ব্যয়", 40f, y, titlePaint)
        y += 30f
        canvas.drawText(monthLabel(monthKey), 40f, y, normalPaint)
        y += 30f
        for (item in items) {
            if (y > 800f) break
            val sign = if (item.type == "আয়") "+" else "−"
            val line = item.date + "  " + item.category + "  " + sign + item.amount.toString()
            canvas.drawText(line, 40f, y, normalPaint)
            y += 22f
        }
        document.finishPage(page)
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "আয়-ব্যয়"
        )
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "report-" + monthKey + ".pdf")
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        file
    } catch (e: Exception) {
        null
    }
}
