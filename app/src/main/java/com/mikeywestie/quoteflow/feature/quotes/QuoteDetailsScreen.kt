package com.mikeywestie.quoteflow.feature.quotes

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.pdf.QuotePdfGenerator
import com.mikeywestie.quoteflow.util.toRand

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
                            val customerName = customer?.customerName ?: "Unknown customer"
                            val customerPhone = customer?.phone.orEmpty()
                            val customerEmail = customer?.email.orEmpty()

                            val itemLines = quoteItems.value.mapIndexed { index, item ->
                                val itemNumber = (index + 1).toString().padStart(2, '0')
                                "$itemNumber) ${item.quantity.cleanQuantity()} x ${item.itemName}. ${item.lineTotal.toRand()}"
                            }

                            val pdfFile = QuotePdfGenerator.generateSimpleQuotePdf(
                                context = context,
                                quoteNumber = quote.quoteNumber,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                customerEmail = customerEmail,
                                items = itemLines,
                                total = quote.totalAmount.toRand()
                            )

                            Toast.makeText(
                                context,
                                "PDF generated: ${pdfFile.name}",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate PDF")
                    }

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share WhatsApp")
                    }

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Email Quote")
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