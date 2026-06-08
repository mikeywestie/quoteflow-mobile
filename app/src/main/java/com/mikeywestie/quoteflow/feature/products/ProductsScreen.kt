package com.mikeywestie.quoteflow.feature.products

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
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.util.toRand
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(repository: QuoteFlowRepository, navController: NavController) {
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
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search products") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products) { product ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text("${product.category} • ${product.unitPrice.toRand()} / ${product.unit}")

                            if (product.sku.isNotBlank()) {
                                Text("SKU: ${product.sku}")
                            }

                            Row {
                                TextButton(onClick = {
                                    editing = product
                                    showDialog = true
                                }) {
                                    Text("Edit")
                                }

                                TextButton(onClick = {
                                    scope.launch {
                                        repository.deleteProduct(product)
                                    }
                                }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
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
private fun ProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "General") }
    var price by remember { mutableStateOf(product?.unitPrice?.toString() ?: "") }
    var unit by remember { mutableStateOf(product?.unit ?: "Each") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (product == null) "Add Product" else "Edit Product")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") })
                OutlinedTextField(sku, { sku = it }, label = { Text("SKU") })
                OutlinedTextField(category, { category = it }, label = { Text("Category") })
                OutlinedTextField(price, { price = it }, label = { Text("VAT inclusive price") })
                OutlinedTextField(unit, { unit = it }, label = { Text("Unit") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Product(
                            id = product?.id ?: 0,
                            name = name,
                            sku = sku,
                            category = category,
                            unitPrice = price.toDoubleOrNull() ?: 0.0,
                            unit = unit
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