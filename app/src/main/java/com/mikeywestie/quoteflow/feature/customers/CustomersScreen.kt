package com.mikeywestie.quoteflow.feature.customers

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
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(repository: QuoteFlowRepository, navController: NavController) {
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
                label = { Text("Search customers") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(customers) { customer ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(customer.customerName, fontWeight = FontWeight.Bold)

                            if (customer.contactPerson.isNotBlank()) {
                                Text("Contact: ${customer.contactPerson}")
                            }

                            if (customer.phone.isNotBlank()) {
                                Text(customer.phone)
                            }

                            Row {
                                TextButton(onClick = {
                                    editing = customer
                                    showDialog = true
                                }) {
                                    Text("Edit")
                                }

                                TextButton(onClick = {
                                    scope.launch {
                                        repository.deleteCustomer(customer)
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
private fun CustomerDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var customerName by remember { mutableStateOf(customer?.customerName ?: "") }
    var contactPerson by remember { mutableStateOf(customer?.contactPerson ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (customer == null) "Add Customer" else "Edit Customer")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(customerName, { customerName = it }, label = { Text("Customer name") })
                OutlinedTextField(contactPerson, { contactPerson = it }, label = { Text("Contact person") })
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone") })
                OutlinedTextField(email, { email = it }, label = { Text("Email") })
                OutlinedTextField(address, { address = it }, label = { Text("Address") })
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Customer(
                            id = customer?.id ?: 0,
                            customerName = customerName,
                            contactPerson = contactPerson,
                            phone = phone,
                            email = email,
                            address = address,
                            notes = notes
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