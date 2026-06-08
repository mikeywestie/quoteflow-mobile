package com.mikeywestie.quoteflow.feature.templates

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                }
            ) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "N&S Quote Templates",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Create, edit and build reusable quote packages."
                )
            }

            if (templates.value.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("No templates yet.", fontWeight = FontWeight.Bold)
                            Text("Tap + Add to create one, or import templates from Import Data.")
                        }
                    }
                }
            } else {
                items(templates.value) { template ->
                    val templateItems = repository
                        .templateItems(template.id)
                        .collectAsStateWithLifecycle(initialValue = emptyList())

                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(template.templateName, fontWeight = FontWeight.Bold)

                            if (template.category.isNotBlank()) {
                                Text("Category: ${template.category}")
                            }

                            if (template.description.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(template.description)
                            }

                            if (template.exampleQuotes.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text("Examples: ${template.exampleQuotes}")
                            }

                            if (template.confidence.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text("Confidence: ${template.confidence}")
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Products in template: ${templateItems.value.size}",
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(8.dp))

                            Button(
                                enabled = templateItems.value.isNotEmpty(),
                                onClick = {
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
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Create Quote")
                            }

                            Row {
                                TextButton(
                                    onClick = {
                                        expandedTemplateId =
                                            if (expandedTemplateId == template.id) null else template.id
                                    }
                                ) {
                                    Text(
                                        if (expandedTemplateId == template.id) {
                                            "Hide Builder"
                                        } else {
                                            "Build Template"
                                        }
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        editingTemplate = template
                                        showTemplateDialog = true
                                    }
                                ) {
                                    Text("Edit")
                                }

                                TextButton(
                                    onClick = {
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
                                    }
                                ) {
                                    Text("Delete")
                                }
                            }

                            if (expandedTemplateId == template.id) {
                                Divider()
                                Spacer(Modifier.height(8.dp))

                                TemplateBuilderSection(
                                    templateId = template.id,
                                    products = products.value,
                                    templateItems = templateItems.value,
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

    Text("Add Product", fontWeight = FontWeight.Bold)

    if (products.isEmpty()) {
        Text("No products available. Import or create products first.")
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

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = quantityText,
            onValueChange = { quantityText = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

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
            Text("Add Product To Template")
        }
    }

    Spacer(Modifier.height(12.dp))
    Divider()
    Spacer(Modifier.height(8.dp))

    Text("Template Products", fontWeight = FontWeight.Bold)

    if (templateItems.isEmpty()) {
        Text("No products attached to this template yet.")
    } else {
        templateItems.forEach { item ->
            val product = products.firstOrNull { it.id == item.productId }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = product?.name ?: "Unknown product",
                        fontWeight = FontWeight.SemiBold
                    )

                    Text("Quantity: ${item.quantity.cleanQuantity()}")

                    if (product != null) {
                        Text("Unit Price: ${product.unitPrice.toRand()}")
                        Text("Estimated Line Total: ${(product.unitPrice * item.quantity).toRand()}")
                    }

                    TextButton(
                        onClick = { onDeleteItem(item) }
                    ) {
                        Text("Remove")
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