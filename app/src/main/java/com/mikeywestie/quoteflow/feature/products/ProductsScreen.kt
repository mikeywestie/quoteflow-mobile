package com.mikeywestie.quoteflow.feature.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.local.entity.Product
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.util.toRand
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    repository: QuoteFlowRepository,
    navController: NavController
) {
    var search by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Product?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val products by repository.products(search).collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editing = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Product catalogue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${products.size} products available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search products") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onEdit = {
                            editing = product
                            showDialog = true
                        },
                        onDelete = {
                            scope.launch {
                                repository.deleteProduct(product)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        ProductDialog(
            product = editing,
            onDismiss = { showDialog = false },
            onSave = { product ->
                scope.launch {
                    repository.saveProduct(product)
                    showDialog = false
                }
            }
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (product.unitPrice <= 0.0) {
                AssistChip(
                    onClick = {},
                    label = { Text("PRICE MISSING") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            } else {
                Text(
                    text = product.unitPrice.toRand(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "${product.category} • ${product.unit}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (product.sku.isNotBlank()) {
                Text(
                    text = "SKU: ${product.sku}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }

                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun ProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember(product?.id) { mutableStateOf(product?.name ?: "") }
    var sku by remember(product?.id) { mutableStateOf(product?.sku ?: "") }
    var category by remember(product?.id) { mutableStateOf(product?.category ?: "General") }
    var price by remember(product?.id) { mutableStateOf(product?.unitPrice?.toString() ?: "") }
    var unit by remember(product?.id) { mutableStateOf(product?.unit ?: "Each") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (product == null) "Add Product" else "Edit Product")
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("VAT inclusive price") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        Product(
                            id = product?.id ?: 0,
                            name = name.trim(),
                            sku = sku.trim(),
                            description = product?.description ?: "",
                            category = category.trim().ifBlank { "General" },
                            unitPrice = price.toDoubleOrNull() ?: 0.0,
                            unit = unit.trim().ifBlank { "Each" },
                            supplier = product?.supplier ?: "",
                            active = product?.active ?: true,
                            updatedAt = System.currentTimeMillis()
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