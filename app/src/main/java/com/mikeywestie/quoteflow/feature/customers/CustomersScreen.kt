package com.mikeywestie.quoteflow.feature.customers

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
import com.mikeywestie.quoteflow.data.local.entity.Customer
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    repository: QuoteFlowRepository,
    navController: NavController
) {
    var search by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Customer?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val customers by repository.customers(search).collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers") },
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
                text = "Customer records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${customers.size} customers available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search customers") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(customers) { customer ->
                    CustomerCard(
                        customer = customer,
                        onEdit = {
                            editing = customer
                            showDialog = true
                        },
                        onDelete = {
                            scope.launch {
                                repository.deleteCustomer(customer)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        CustomerDialog(
            customer = editing,
            onDismiss = { showDialog = false },
            onSave = { customer ->
                scope.launch {
                    repository.saveCustomer(customer)
                    showDialog = false
                }
            }
        )
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = customer.customerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (customer.contactPerson.isNotBlank()) {
                Text(
                    text = customer.contactPerson,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (customer.phone.isNotBlank()) {
                Text(
                    text = customer.phone,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (customer.email.isNotBlank()) {
                Text(
                    text = customer.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (customer.address.isNotBlank()) {
                Text(
                    text = customer.address,
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
private fun CustomerDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var customerName by remember(customer?.id) { mutableStateOf(customer?.customerName ?: "") }
    var contactPerson by remember(customer?.id) { mutableStateOf(customer?.contactPerson ?: "") }
    var phone by remember(customer?.id) { mutableStateOf(customer?.phone ?: "") }
    var email by remember(customer?.id) { mutableStateOf(customer?.email ?: "") }
    var address by remember(customer?.id) { mutableStateOf(customer?.address ?: "") }
    var notes by remember(customer?.id) { mutableStateOf(customer?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (customer == null) "Add Customer" else "Edit Customer")
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
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("Contact person") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = customerName.isNotBlank(),
                onClick = {
                    onSave(
                        Customer(
                            id = customer?.id ?: 0,
                            customerName = customerName.trim(),
                            contactPerson = contactPerson.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            notes = notes.trim()
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