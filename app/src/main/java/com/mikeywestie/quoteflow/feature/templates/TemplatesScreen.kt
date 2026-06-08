package com.mikeywestie.quoteflow.feature.templates

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
import com.mikeywestie.quoteflow.data.local.entity.Product
import com.mikeywestie.quoteflow.data.local.entity.TemplateItem
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.util.toRand
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    repository: QuoteFlowRepository,
    navController: NavController
) {
    val templates = repository.templates().collectAsStateWithLifecycle(initialValue = emptyList())
    val products = repository.products("").collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    var expandedTemplateId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            )
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
                    text = "Build reusable quote packages by attaching products and quantities to templates."
                )
            }

            if (templates.value.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("No templates imported yet.", fontWeight = FontWeight.Bold)
                            Text("Go to Import Data and import quoteflow_templates_import.csv.")
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

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Products in template: ${templateItems.value.size}",
                                fontWeight = FontWeight.SemiBold
                            )

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
                            }

                            if (expandedTemplateId == template.id) {
                                Divider()
                                Spacer(Modifier.height(8.dp))

                                TemplateBuilderSection(
                                    repository = repository,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateBuilderSection(
    repository: QuoteFlowRepository,
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