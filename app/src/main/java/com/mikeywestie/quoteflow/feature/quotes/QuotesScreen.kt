package com.mikeywestie.quoteflow.feature.quotes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.local.entity.Customer
import com.mikeywestie.quoteflow.data.local.entity.Product
import com.mikeywestie.quoteflow.data.local.entity.QuoteItem
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.util.toRand
import kotlinx.coroutines.launch
import com.mikeywestie.quoteflow.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(repository: QuoteFlowRepository, navController: NavController) {
    val quotes by repository.quotes().collectAsStateWithLifecycle(initialValue = emptyList())
    val customers by repository.customers("").collectAsStateWithLifecycle(initialValue = emptyList())
    val products by repository.products("").collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    var showBuilder by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Quotes") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBuilder = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Create and manage on-site quotes", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(16.dp))

            if (quotes.isEmpty()) {
                Text("No quotes yet. Tap + to create your first quote.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quotes) { quote ->
                    val customerName = customers
                        .firstOrNull { it.id == quote.customerId }
                        ?.customerName
                        ?: "Unknown customer"

                    Card(
    onClick = {
        navController.navigate(Routes.quoteDetails(quote.id))
    },
    modifier = Modifier.fillMaxWidth()
) {
                        Column(Modifier.padding(16.dp)) {
                            Text(quote.quoteNumber, fontWeight = FontWeight.Bold)
                            Text(customerName)
                            Text("Status: ${quote.status}")
                            Text("Total: ${quote.totalAmount.toRand()}")
                        }
                    }
                }
            }
        }
    }

    if (showBuilder) {
        QuoteBuilderDialog(
            customers = customers,
            products = products,
            onDismiss = { showBuilder = false },
            onSave = { customerId, notes, draftLines ->
                scope.launch {
                    val quoteItems = draftLines.map { line ->
                        QuoteItem(
                            quoteId = 0,
                            productId = line.productId,
                            itemName = line.itemName,
                            quantity = line.quantity,
                            unitPrice = line.unitPrice,
                            lineTotal = line.lineTotal
                        )
                    }

                    repository.saveQuoteWithItems(
                        customerId = customerId,
                        notes = notes,
                        items = quoteItems
                    )

                    showBuilder = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuoteBuilderDialog(
    customers: List<Customer>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onSave: (Long, String, List<QuoteDraftLine>) -> Unit
) {
    var selectedCustomerId by remember { mutableStateOf<Long?>(customers.firstOrNull()?.id) }
    var customerExpanded by remember { mutableStateOf(false) }

    var selectedProductId by remember { mutableStateOf<Long?>(products.firstOrNull()?.id) }
    var productExpanded by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf("1") }
    var customItemName by remember { mutableStateOf("") }
    var customPriceText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var draftLines by remember { mutableStateOf<List<QuoteDraftLine>>(emptyList()) }

    val total = draftLines.sumOf { it.lineTotal }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Quote") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (customers.isEmpty()) {
                    Text("Add a customer first before creating a quote.")
                } else {
                    ExposedDropdownMenuBox(
                        expanded = customerExpanded,
                        onExpandedChange = { customerExpanded = !customerExpanded }
                    ) {
                        OutlinedTextField(
                            value = customers.firstOrNull { it.id == selectedCustomerId }?.customerName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Customer") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = customerExpanded,
                            onDismissRequest = { customerExpanded = false }
                        ) {
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text(customer.customerName) },
                                    onClick = {
                                        selectedCustomerId = customer.id
                                        customerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Divider()

                    Text("Add existing product", fontWeight = FontWeight.Bold)

                    if (products.isEmpty()) {
                        Text("No products available yet.")
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = productExpanded,
                            onExpandedChange = { productExpanded = !productExpanded }
                        ) {
                            OutlinedTextField(
                                value = products.firstOrNull { it.id == selectedProductId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Product") },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = productExpanded,
                                onDismissRequest = { productExpanded = false }
                            ) {
                                products.forEach { product ->
                                    DropdownMenuItem(
                                        text = { Text("${product.name} - ${product.unitPrice.toRand()}") },
                                        onClick = {
                                            selectedProductId = product.id
                                            productExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Quantity") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val product = products.firstOrNull { it.id == selectedProductId }
                                val quantity = quantityText.toDoubleOrNull() ?: 1.0

                                if (product != null) {
                                    draftLines = draftLines + QuoteDraftLine(
                                        itemName = product.name,
                                        quantity = quantity,
                                        unitPrice = product.unitPrice,
                                        productId = product.id
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Product")
                        }
                    }

                    Divider()

                    Text("Add custom item", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = customItemName,
                        onValueChange = { customItemName = it },
                        label = { Text("Custom item name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customPriceText,
                        onValueChange = { customPriceText = it },
                        label = { Text("Custom price") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val price = customPriceText.toDoubleOrNull() ?: 0.0

                            if (customItemName.isNotBlank() && price > 0.0) {
                                draftLines = draftLines + QuoteDraftLine(
                                    itemName = customItemName,
                                    quantity = 1.0,
                                    unitPrice = price,
                                    productId = null
                                )

                                customItemName = ""
                                customPriceText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Custom Item")
                    }

                    Divider()

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Quote notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Items", fontWeight = FontWeight.Bold)

                    if (draftLines.isEmpty()) {
                        Text("No items added yet.")
                    } else {
                        draftLines.forEach { line ->
                            Text("${line.quantity} x ${line.itemName} = ${line.lineTotal.toRand()}")
                        }
                    }

                    Text("Total: ${total.toRand()}", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedCustomerId != null && draftLines.isNotEmpty(),
                onClick = {
                    val customerId = selectedCustomerId
                    if (customerId != null) {
                        onSave(customerId, notes, draftLines)
                    }
                }
            ) {
                Text("Save Quote")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}