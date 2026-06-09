package com.mikeywestie.quoteflow.feature.templates

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.local.entity.Product
import com.mikeywestie.quoteflow.data.local.entity.QuoteTemplate
import com.mikeywestie.quoteflow.data.local.entity.TemplateItem
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.navigation.Routes
import com.mikeywestie.quoteflow.util.toRand
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    repository: QuoteFlowRepository,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val templates = repository.templates().collectAsStateWithLifecycle(initialValue = emptyList())
    val products = repository.products("").collectAsStateWithLifecycle(initialValue = emptyList())

    var expandedTemplateId by remember { mutableStateOf<Long?>(null) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<QuoteTemplate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            editingTemplate = null
                            showTemplateDialog = true
                        }
                    ) {
                        Text("+ Add")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTemplate = null
                    showTemplateDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Template library",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${templates.value.size} reusable quote packages",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (templates.value.isEmpty()) {
                item {
                    EmptyTemplatesCard()
                }
            } else {
                items(templates.value) { template ->
                    val templateItems = repository
                        .templateItems(template.id)
                        .collectAsStateWithLifecycle(initialValue = emptyList())

                    TemplateCard(
                        template = template,
                        products = products.value,
                        templateItems = templateItems.value,
                        expanded = expandedTemplateId == template.id,
                        onToggleBuilder = {
                            expandedTemplateId =
                                if (expandedTemplateId == template.id) null else template.id
                        },
                        onCreateQuote = {
                            scope.launch {
                                val quoteId = repository.createQuoteFromTemplate(
                                    templateId = template.id
                                )

                                Toast.makeText(
                                    context,
                                    "Quote created from template",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.navigate(Routes.quoteDetails(quoteId))
                            }
                        },
                        onEdit = {
                            editingTemplate = template
                            showTemplateDialog = true
                        },
                        onDelete = {
                            scope.launch {
                                repository.deleteTemplate(template)

                                if (expandedTemplateId == template.id) {
                                    expandedTemplateId = null
                                }

                                Toast.makeText(
                                    context,
                                    "Template deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onAddProduct = { productId, quantity ->
                            scope.launch {
                                repository.addProductToTemplate(
                                    templateId = template.id,
                                    productId = productId,
                                    quantity = quantity
                                )
                            }
                        },
                        onDeleteItem = { item ->
                            scope.launch {
                                repository.deleteTemplateItem(item)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showTemplateDialog) {
        TemplateDialog(
            template = editingTemplate,
            onDismiss = {
                showTemplateDialog = false
                editingTemplate = null
            },
            onSave = { template ->
                scope.launch {
                    repository.saveTemplate(template)
                    showTemplateDialog = false
                    editingTemplate = null

                    Toast.makeText(
                        context,
                        "Template saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

@Composable
private fun EmptyTemplatesCard() {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "No templates yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tap + Add to create one, or import templates from Import Data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: QuoteTemplate,
    products: List<Product>,
    templateItems: List<TemplateItem>,
    expanded: Boolean,
    onToggleBuilder: () -> Unit,
    onCreateQuote: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddProduct: (Long, Double) -> Unit,
    onDeleteItem: (TemplateItem) -> Unit
) {
    val estimatedTotal = templateItems.sumOf { item ->
        val product = products.firstOrNull { it.id == item.productId }
        (product?.unitPrice ?: 0.0) * item.quantity
    }

    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = template.templateName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${template.category} • ${template.confidence.ifBlank { "No confidence set" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TemplateCountChip(templateItems.size)
            }

            if (template.description.isNotBlank()) {
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Estimated material total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = estimatedTotal.toRand(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                enabled = templateItems.isNotEmpty(),
                onClick = onCreateQuote,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Quote")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onToggleBuilder) {
                    Text(if (expanded) "Hide Builder" else "Build")
                }

                TextButton(onClick = onEdit) {
                    Text("Edit")
                }

                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }

            if (expanded) {
                HorizontalDivider()

                TemplateBuilderSection(
                    templateId = template.id,
                    products = products,
                    templateItems = templateItems,
                    onAddProduct = onAddProduct,
                    onDeleteItem = onDeleteItem
                )
            }
        }
    }
}

@Composable
private fun TemplateCountChip(count: Int) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = "$count products",
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}

@Composable
private fun TemplateDialog(
    template: QuoteTemplate?,
    onDismiss: () -> Unit,
    onSave: (QuoteTemplate) -> Unit
) {
    var templateName by remember(template?.id) { mutableStateOf(template?.templateName ?: "") }
    var category by remember(template?.id) { mutableStateOf(template?.category ?: "General") }
    var description by remember(template?.id) { mutableStateOf(template?.description ?: "") }
    var exampleQuotes by remember(template?.id) { mutableStateOf(template?.exampleQuotes ?: "") }
    var confidence by remember(template?.id) { mutableStateOf(template?.confidence ?: "") }
    var enabled by remember(template?.id) { mutableStateOf(template?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (template == null) "Add Template" else "Edit Template")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = exampleQuotes,
                    onValueChange = { exampleQuotes = it },
                    label = { Text("Example Quotes") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confidence,
                    onValueChange = { confidence = it },
                    label = { Text("Confidence") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enabled")
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = templateName.isNotBlank(),
                onClick = {
                    onSave(
                        QuoteTemplate(
                            id = template?.id ?: 0,
                            templateName = templateName.trim(),
                            category = category.trim().ifBlank { "General" },
                            description = description.trim(),
                            exampleQuotes = exampleQuotes.trim(),
                            confidence = confidence.trim(),
                            enabled = enabled,
                            createdAt = template?.createdAt ?: System.currentTimeMillis()
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
private fun TemplateBuilderSection(
    templateId: Long,
    products: List<Product>,
    templateItems: List<TemplateItem>,
    onAddProduct: (Long, Double) -> Unit,
    onDeleteItem: (TemplateItem) -> Unit
) {
    var selectedProductId by remember(templateId, products) {
        mutableStateOf(products.firstOrNull()?.id)
    }

    var productExpanded by remember { mutableStateOf(false) }
    var quantityText by remember { mutableStateOf("1") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Build template",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (products.isEmpty()) {
            Text(
                text = "No products available. Import or create products first.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                            text = {
                                Text("${product.name} - ${product.unitPrice.toRand()}")
                            },
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
                    val productId = selectedProductId
                    val quantity = quantityText.toDoubleOrNull() ?: 1.0

                    if (productId != null && quantity > 0.0) {
                        onAddProduct(productId, quantity)
                        quantityText = "1"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Product")
            }
        }

        HorizontalDivider()

        Text(
            text = "Template products",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (templateItems.isEmpty()) {
            Text(
                text = "No products attached yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            templateItems.forEachIndexed { index, item ->
                TemplateProductLine(
                    number = index + 1,
                    item = item,
                    product = products.firstOrNull { it.id == item.productId },
                    onDelete = { onDeleteItem(item) }
                )
            }
        }
    }
}

@Composable
private fun TemplateProductLine(
    number: Int,
    item: TemplateItem,
    product: Product?,
    onDelete: () -> Unit
) {
    val lineTotal = (product?.unitPrice ?: 0.0) * item.quantity

    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$number. ${product?.name ?: "Unknown product"}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Qty: ${item.quantity.cleanQuantity()}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Unit Price: ${(product?.unitPrice ?: 0.0).toRand()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Line Total: ${lineTotal.toRand()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            TextButton(onClick = onDelete) {
                Text("Remove")
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