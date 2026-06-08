package com.mikeywestie.quoteflow.feature.quotes

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.local.entity.Customer
import com.mikeywestie.quoteflow.data.local.entity.Product
import com.mikeywestie.quoteflow.data.local.entity.QuoteItem
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.pdf.QuotePdfGenerator
import com.mikeywestie.quoteflow.util.toRand
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailsScreen(
    repository: QuoteFlowRepository,
    navController: NavController,
    quoteId: Long
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val quotes = repository.quotes().collectAsStateWithLifecycle(initialValue = emptyList())
    val customers = repository.customers("").collectAsStateWithLifecycle(initialValue = emptyList())
    val products = repository.products("").collectAsStateWithLifecycle(initialValue = emptyList())
    val quoteItems = repository.quoteItems(quoteId).collectAsStateWithLifecycle(initialValue = emptyList())
    val companySettings = repository.companySettings().collectAsStateWithLifecycle(initialValue = null)

    val quote = quotes.value.firstOrNull { it.id == quoteId }
    val customer = customers.value.firstOrNull { it.id == quote?.customerId }
    val settings = companySettings.value

    var showEditDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<QuoteItem?>(null) }

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(timestamp))

    fun validUntil(timestamp: Long): String {
        val thirtyDays = TimeUnit.DAYS.toMillis(30)
        return formatDate(timestamp + thirtyDays)
    }

    fun generatePdf(): File? {
        if (quote == null) return null

        val itemLines = quoteItems.value.mapIndexed { index, item ->
            val itemNumber = (index + 1).toString().padStart(2, '0')
            "$itemNumber) ${item.quantity.cleanQuantity()} x ${item.itemName}. ${item.lineTotal.toRand()}"
        }

        return QuotePdfGenerator.generateSimpleQuotePdf(
            context = context,
            companyName = settings?.companyName.orEmpty(),
            companyPhone = settings?.phone.orEmpty(),
            companyEmail = settings?.email.orEmpty(),
            companyAddress = settings?.address.orEmpty(),
            vatNumber = settings?.vatNumber.orEmpty(),
            registrationNumber = settings?.registrationNumber.orEmpty(),
            quoteNumber = quote.quoteNumber,
            status = quote.status,
            createdDate = formatDate(quote.createdAt),
            validUntilDate = validUntil(quote.createdAt),
            customerName = customer?.customerName ?: "Unknown customer",
            customerPhone = customer?.phone.orEmpty(),
            customerEmail = customer?.email.orEmpty(),
            items = itemLines,
            notes = quote.notes,
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
            Toast.makeText(context, "No PDF viewer found.", Toast.LENGTH_LONG).show()
        }
    }

    fun sharePdf() {
        val pdfFile = generatePdf() ?: return
        val uri = getPdfUri(pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(customer?.email.orEmpty()))
            putExtra(Intent.EXTRA_SUBJECT, "Quote ${quote?.quoteNumber.orEmpty()}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Good day,\n\nPlease find attached quote ${quote?.quoteNumber.orEmpty()}.\n\nKind regards"
            )
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Share"))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "No app found to share this PDF.", Toast.LENGTH_LONG).show()
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
                },
                actions = {
                    TextButton(
                        enabled = quote != null,
                        onClick = { showEditDialog = true }
                    ) {
                        Text("Edit")
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
                    Text("Status: ${quote.status}")
                    Text("Date Issued: ${formatDate(quote.createdAt)}")
                    Text("Valid Until: ${validUntil(quote.createdAt)}")
                }

                item {
                    Text("Customer", fontWeight = FontWeight.Bold)
                    Divider()
                    Text(customer?.customerName ?: "Unknown customer")
                    if (!customer?.phone.isNullOrBlank()) Text(customer?.phone.orEmpty())
                    if (!customer?.email.isNullOrBlank()) Text(customer?.email.orEmpty())
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Items", fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showAddItemDialog = true }) {
                            Text("+ Add Item")
                        }
                    }
                    Divider()
                }

                quoteItems.value.forEachIndexed { index, item ->
                    item {
                        val itemNumber = (index + 1).toString().padStart(2, '0')

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = "$itemNumber) ${item.quantity.cleanQuantity()} x ${item.itemName}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text("Unit Price: ${item.unitPrice.toRand()}")
                                Text("Line Total: ${item.lineTotal.toRand()}")

                                Row {
                                    TextButton(onClick = { editingItem = item }) {
                                        Text("Edit Item")
                                    }

                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                repository.deleteQuoteItem(item)
                                                Toast.makeText(
                                                    context,
                                                    "Item removed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    ) {
                                        Text("Delete Item")
                                    }
                                }
                            }
                        }
                    }
                }

                if (quote.notes.isNotBlank()) {
                    item {
                        Text("Notes", fontWeight = FontWeight.Bold)
                        Divider()
                        Text(quote.notes)
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

    if (quote != null && showEditDialog) {
        EditQuoteDialog(
            customers = customers.value,
            currentCustomerId = quote.customerId,
            currentStatus = quote.status,
            currentNotes = quote.notes,
            onDismiss = { showEditDialog = false },
            onSave = { customerId, status, notes ->
                scope.launch {
                    repository.updateQuoteStatusNotesAndCustomer(
                        quoteId = quote.id,
                        customerId = customerId,
                        status = status,
                        notes = notes
                    )
                    showEditDialog = false
                    Toast.makeText(context, "Quote updated", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (quote != null && showAddItemDialog) {
        AddQuoteItemDialog(
            products = products.value,
            quoteId = quote.id,
            onDismiss = { showAddItemDialog = false },
            onSave = { quoteItem ->
                scope.launch {
                    repository.addQuoteItem(quoteItem)
                    showAddItemDialog = false
                    Toast.makeText(context, "Item added", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (editingItem != null) {
        EditQuoteItemDialog(
            item = editingItem!!,
            onDismiss = { editingItem = null },
            onSave = { updated ->
                scope.launch {
                    repository.updateQuoteItem(updated)
                    editingItem = null
                    Toast.makeText(context, "Item updated", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditQuoteDialog(
    customers: List<Customer>,
    currentCustomerId: Long,
    currentStatus: String,
    currentNotes: String,
    onDismiss: () -> Unit,
    onSave: (Long, String, String) -> Unit
) {
    val statuses = listOf("Draft", "Sent", "Accepted", "Rejected", "Paid")

    var selectedCustomerId by remember(currentCustomerId) { mutableStateOf(currentCustomerId) }
    var selectedStatus by remember(currentStatus) { mutableStateOf(currentStatus) }
    var notes by remember(currentNotes) { mutableStateOf(currentNotes) }

    var customerExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Quote") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = customerExpanded,
                    onExpandedChange = { customerExpanded = !customerExpanded }
                ) {
                    OutlinedTextField(
                        value = customers.firstOrNull { it.id == selectedCustomerId }?.customerName
                            ?: "Unknown customer",
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

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    selectedStatus = status
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Quote notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedCustomerId, selectedStatus, notes) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditQuoteItemDialog(
    item: QuoteItem,
    onDismiss: () -> Unit,
    onSave: (QuoteItem) -> Unit
) {
    var itemName by remember(item.id) { mutableStateOf(item.itemName) }
    var quantityText by remember(item.id) { mutableStateOf(item.quantity.cleanQuantity()) }
    var priceText by remember(item.id) { mutableStateOf(item.unitPrice.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Unit Price") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantity = quantityText.toDoubleOrNull() ?: item.quantity
                    val price = priceText.toDoubleOrNull() ?: item.unitPrice

                    onSave(
                        item.copy(
                            itemName = itemName,
                            quantity = quantity,
                            unitPrice = price,
                            lineTotal = quantity * price
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddQuoteItemDialog(
    products: List<Product>,
    quoteId: Long,
    onDismiss: () -> Unit,
    onSave: (QuoteItem) -> Unit
) {
    var selectedProductId by remember { mutableStateOf<Long?>(products.firstOrNull()?.id) }
    var productExpanded by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf("1") }
    var customItemName by remember { mutableStateOf("") }
    var customPriceText by remember { mutableStateOf("") }

    var labourDescription by remember { mutableStateOf("") }
    var labourPriceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Existing product", fontWeight = FontWeight.Bold)

                if (products.isEmpty()) {
                    Text("No products available.")
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
                                onSave(
                                    QuoteItem(
                                        quoteId = quoteId,
                                        productId = product.id,
                                        itemName = product.name,
                                        quantity = quantity,
                                        unitPrice = product.unitPrice,
                                        lineTotal = quantity * product.unitPrice
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Product")
                    }
                }

                Divider()

                Text("Custom item", fontWeight = FontWeight.Bold)

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
                            onSave(
                                QuoteItem(
                                    quoteId = quoteId,
                                    productId = null,
                                    itemName = customItemName,
                                    quantity = 1.0,
                                    unitPrice = price,
                                    lineTotal = price
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Custom Item")
                }

                Divider()

                Text("Labour", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = labourDescription,
                    onValueChange = { labourDescription = it },
                    label = { Text("Labour description") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = labourPriceText,
                    onValueChange = { labourPriceText = it },
                    label = { Text("Labour charge") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val price = labourPriceText.toDoubleOrNull() ?: 0.0

                        if (labourDescription.isNotBlank() && price > 0.0) {
                            onSave(
                                QuoteItem(
                                    quoteId = quoteId,
                                    productId = null,
                                    itemName = "Labour: $labourDescription",
                                    quantity = 1.0,
                                    unitPrice = price,
                                    lineTotal = price
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Labour")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun Double.cleanQuantity(): String {
    return if (this % 1.0 == 0.0) {
        this.toInt().toString()
    } else {
        this.toString()
    }
}