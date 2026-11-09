package com.aybay.app.ui

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.aybay.app.data.Transaction
import java.io.File
import java.io.FileInputStream
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

// রিপোর্টের PDF (জমার মতো): শিরোনাম + সারাংশের লাইন + লেনদেনের
// সারি। লম্বা তালিকা হলে নতুন পাতায় যায়।
fun exportReportPdf(
    context: Context,
    title: String,
    summaryLines: List<String>,
    rows: List<String>,
    fileName: String
): File? {
    return try {
        val document = PdfDocument()
        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val boldPaint = Paint().apply { textSize = 14f; isFakeBoldText = true }
        val normalPaint = Paint().apply { textSize = 13f }
        var page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        var y = 60f
        fun newPage() {
            document.finishPage(page)
            page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            y = 60f
        }
        page.canvas.drawText(title, 40f, y, titlePaint)
        y += 32f
        for (line in summaryLines) {
            if (y > 800f) newPage()
            page.canvas.drawText(line, 40f, y, boldPaint)
            y += 22f
        }
        y += 10f
        for (row in rows) {
            if (y > 800f) newPage()
            page.canvas.drawText(row, 40f, y, normalPaint)
            y += 22f
        }
        document.finishPage(page)
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "আয়-ব্যয়"
        )
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        file
    } catch (e: Exception) {
        null
    }
}

// PDF ফাইল সিস্টেমের প্রিন্ট প্রিভিউতে খোলা (Save as PDF সহ)।
// PrintManager-এ আগে থেকে তৈরি PDF ফাইল পাঠানো হয়।
fun printPdfFile(context: Context, file: File) {
    try {
        val printManager =
            context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                bundle: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(file.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<android.print.PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                try {
                    FileInputStream(file).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                }
            }
        }
        printManager.print("আয়-ব্যয়: Expense Tracker", adapter, null)
    } catch (e: Exception) {
    }
}
