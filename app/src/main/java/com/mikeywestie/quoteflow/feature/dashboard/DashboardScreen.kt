package com.mikeywestie.quoteflow.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.navigation.Routes
import com.mikeywestie.quoteflow.util.toRand

@Composable
fun DashboardScreen(
    repository: QuoteFlowRepository,
    navController: NavController
) {
    val products = repository.products("").collectAsStateWithLifecycle(initialValue = emptyList())
    val customers = repository.customers("").collectAsStateWithLifecycle(initialValue = emptyList())
    val quotes = repository.quotes().collectAsStateWithLifecycle(initialValue = emptyList())
    val templates = repository.templates().collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("QuoteFlow", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text("Mobile quotation builder", style = MaterialTheme.typography.bodyLarge)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardMetricCard("Products", products.value.size.toString(), Modifier.weight(1f))
                    DashboardMetricCard("Customers", customers.value.size.toString(), Modifier.weight(1f))
                    DashboardMetricCard("Quotes", quotes.value.size.toString(), Modifier.weight(1f))
                }
            }

            item {
                DashboardMetricCard(
                    title = "Templates",
                    value = templates.value.size.toString(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                DashboardButton("New Quote / Saved Quotes") {
                    navController.navigate(Routes.QUOTES)
                }

                DashboardButton("Templates") {
                    navController.navigate(Routes.TEMPLATES)
                }

                DashboardButton("Products") {
                    navController.navigate(Routes.PRODUCTS)
                }

                DashboardButton("Customers") {
                    navController.navigate(Routes.CUSTOMERS)
                }

                DashboardButton("Settings") {
                    navController.navigate(Routes.SETTINGS)
                }

                DashboardButton("Import Data") {
                    navController.navigate(Routes.IMPORT_DATA)
                }
            }

            item {
                Text("Recent Quotes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (quotes.value.isEmpty()) {
                item {
                    Text("No quotes yet.")
                }
            } else {
                items(quotes.value.take(5)) { quote ->
                    val customerName = customers.value
                        .firstOrNull { it.id == quote.customerId }
                        ?.customerName
                        ?: "Unknown customer"

                    Card(
                        onClick = { navController.navigate(Routes.quoteDetails(quote.id)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
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
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DashboardButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .height(52.dp)
    ) {
        Text(text)
    }
}