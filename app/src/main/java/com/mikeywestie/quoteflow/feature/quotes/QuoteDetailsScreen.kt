package com.mikeywestie.quoteflow.feature.quotes

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.pdf.QuotePdfGenerator
import com.mikeywestie.quoteflow.util.toRand
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailsScreen(
    repository: QuoteFlowRepository,
    navController: NavController,
    quoteId: Long
) {
    val context = LocalContext.current

    val quotes = repository.quotes().collectAsStateWithLifecycle(initialValue = emptyList())
    val customers = repository.customers("").collectAsStateWithLifecycle(initialValue = emptyList())
    val quoteItems = repository.quoteItems(quoteId).collectAsStateWithLifecycle(initialValue = emptyList())

    val quote = quotes.value.firstOrNull { it.id == quoteId }
    val customer = customers.value.firstOrNull { it.id == quote?.customerId }

    fun generatePdf(): File? {
        if (quote == null) return null

        val customerName = customer?.customerName ?: "Unknown customer"
        val customerPhone = customer?.phone.orEmpty()
        val customerEmail = customer?.email.orEmpty()

        val itemLines = quoteItems.value.mapIndexed { index, item ->
            val itemNumber = (index + 1).toString().padStart(2, '0')
            "$itemNumber) ${item.quantity.cleanQuantity()} x ${item.itemName}. ${item.lineTotal.toRand()}"
        }

        return QuotePdfGenerator.generateSimpleQuotePdf(
            context = context,
            quoteNumber = quote.quoteNumber,
            customerName = customerName,
            customerPhone = customerPhone,
            customerEmail = customerEmail,
            items = itemLines,
            total = quote.totalAmount.toRand()
        )
    }

    fun getPdfUri(file: File) =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

    fun openPdf() {
        val pdfFile = generatePdf() ?: return
        val uri = getPdfUri(pdfFile)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No PDF viewer found. File saved to Documents/QuoteFlow.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun sharePdf(packageName: String? = null, chooserTitle: String = "Share Quote") {
        val pdfFile = generatePdf() ?: return
        val uri = getPdfUri(pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Quote ${quote?.quoteNumber.orEmpty()}")
            putExtra(Intent.EXTRA_TEXT, "Please find attached quote ${quote?.quoteNumber.orEmpty()}.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (packageName != null) {
                setPackage(packageName)
            }
        }

        try {
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No app found to share this PDF.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun sharePdf() {
        val pdfFile = generatePdf() ?: return
        val uri = getPdfUri(pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(customer?.email.orEmpty()))
            putExtra(Intent.EXTRA_SUBJECT, "Quote ${quote?.quoteNumber.orEmpty()}")
            putExtra(Intent.EXTRA_TEXT, "Good day,\n\nPlease find attached quote ${quote?.quoteNumber.orEmpty()}.\n\nKind regards")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Share"))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No email app found.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quote Details") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        if (quote == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Quote not found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = quote.quoteNumber,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Text("Customer", fontWeight = FontWeight.Bold)
                    Divider()
                    Text(customer?.customerName ?: "Unknown customer")

                    if (!customer?.phone.isNullOrBlank()) {
                        Text(customer?.phone.orEmpty())
                    }

                    if (!customer?.email.isNullOrBlank()) {
                        Text(customer?.email.orEmpty())
                    }
                }

                item {
                    Text("Items", fontWeight = FontWeight.Bold)
                    Divider()
                }

                quoteItems.value.forEachIndexed { index, item ->
                    item {
                        val itemNumber = (index + 1).toString().padStart(2, '0')

                        Column {
                            Text(
                                text = "$itemNumber) ${item.quantity.cleanQuantity()} x ${item.itemName}. ${item.lineTotal.toRand()}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                item {
                    Divider()

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Total ${quote.totalAmount.toRand()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val pdfFile = generatePdf()

                            if (pdfFile != null) {
                                Toast.makeText(
                                    context,
                                    "PDF saved to Documents/QuoteFlow/${pdfFile.name}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate PDF")
                    }

                    OutlinedButton(
                        onClick = { openPdf() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open PDF")
                    }

                    OutlinedButton(
                        onClick = { sharePdf() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share")
                    }
                }
            }
        }
    }
}

private fun Double.cleanQuantity(): String {
    return if (this % 1.0 == 0.0) {
        this.toInt().toString()
    } else {
        this.toString()
    }
}