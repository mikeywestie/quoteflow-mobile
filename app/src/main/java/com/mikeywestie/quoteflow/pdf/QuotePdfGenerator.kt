package com.mikeywestie.quoteflow.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object QuotePdfGenerator {

    fun generateSimpleQuotePdf(
        context: Context,
        quoteNumber: String,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        items: List<String>,
        total: String
    ): File {

        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(
            595,
            842,
            1
        ).create()

        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            textSize = 12f
        }

        var y = 50

        canvas.drawText(
            "QUOTEFLOW QUOTATION",
            40f,
            y.toFloat(),
            titlePaint
        )

        y += 40

        canvas.drawText(
            quoteNumber,
            40f,
            y.toFloat(),
            textPaint
        )

        y += 40

        canvas.drawText(
            "Customer:",
            40f,
            y.toFloat(),
            titlePaint
        )

        y += 25

        canvas.drawText(
            customerName,
            40f,
            y.toFloat(),
            textPaint
        )

        y += 20

        canvas.drawText(
            customerPhone,
            40f,
            y.toFloat(),
            textPaint
        )

        y += 20

        canvas.drawText(
            customerEmail,
            40f,
            y.toFloat(),
            textPaint
        )

        y += 40

        canvas.drawText(
            "Items",
            40f,
            y.toFloat(),
            titlePaint
        )

        y += 25

        items.forEach {
            canvas.drawText(
                it,
                40f,
                y.toFloat(),
                textPaint
            )

            y += 20
        }

        y += 20

        canvas.drawText(
            "TOTAL: $total",
            40f,
            y.toFloat(),
            titlePaint
        )

        pdfDocument.finishPage(page)

        val pdfDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
            ),
            "QuoteFlow"
        )

        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }

        val pdfFile = File(
            pdfDir,
            "$quoteNumber.pdf"
        )

        pdfDocument.writeTo(
            FileOutputStream(pdfFile)
        )

        pdfDocument.close()

        return pdfFile
    }
}