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
        companyName: String,
        companyPhone: String,
        companyEmail: String,
        companyAddress: String,
        vatNumber: String,
        registrationNumber: String,
        quoteNumber: String,
        status: String,
        createdDate: String,
        validUntilDate: String,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        items: List<String>,
        notes: String,
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

        val headingPaint = Paint().apply {
            textSize = 15f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            textSize = 12f
        }

        val smallPaint = Paint().apply {
            textSize = 10f
        }

        var y = 45

        fun draw(
            text: String,
            paint: Paint = textPaint,
            lineGap: Int = 18
        ) {
            if (text.isNotBlank()) {
                canvas.drawText(
                    text,
                    40f,
                    y.toFloat(),
                    paint
                )
                y += lineGap
            }
        }

        fun divider() {
            y += 8

            canvas.drawLine(
                40f,
                y.toFloat(),
                555f,
                y.toFloat(),
                textPaint
            )

            y += 22
        }

        draw(
            companyName.ifBlank { "QuoteFlow" }.uppercase(),
            titlePaint,
            28
        )

        draw(companyPhone, smallPaint, 14)
        draw(companyEmail, smallPaint, 14)
        draw(companyAddress, smallPaint, 14)

        if (vatNumber.isNotBlank()) {
            draw("VAT: $vatNumber", smallPaint, 14)
        }

        if (registrationNumber.isNotBlank()) {
            draw("Reg: $registrationNumber", smallPaint, 14)
        }

        divider()

        draw("QUOTATION", headingPaint, 24)
        draw("Quote Number: $quoteNumber")
        draw("Status: $status")
        draw("Date Issued: $createdDate")
        draw("Valid Until: $validUntilDate")

        divider()

        draw("Customer", headingPaint, 22)
        draw(customerName)
        draw(customerPhone)
        draw(customerEmail)

        divider()

        draw("Items", headingPaint, 22)

        items.forEach {
            draw(it)
        }

        divider()

        if (notes.isNotBlank()) {
            draw("Notes", headingPaint, 22)

            notes.lines().forEach {
                draw(it)
            }

            divider()
        }

        draw(
            "TOTAL: $total",
            titlePaint,
            28
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