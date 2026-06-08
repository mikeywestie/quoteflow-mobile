package com.mikeywestie.quoteflow.feature.quotes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.util.toRand
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(repository: QuoteFlowRepository, navController: NavController) {
    val quotes = repository.quotes().collectAsStateWithLifecycle(initialValue = emptyList())
    Scaffold(
        topBar = { TopAppBar(title = { Text("Saved Quotes") }, navigationIcon = { TextButton(onClick = { navController.popBackStack() }) { Text("Back") } }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Quote builder foundation", style = MaterialTheme.typography.titleMedium)
            Text("v0.3 will add customer selection, product line items, and custom items.")
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quotes.value) { quote ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(quote.quoteNumber, fontWeight = FontWeight.Bold)
                            Text("Status: ${quote.status}")
                            Text("Total: ${quote.totalAmount.toRand()}")
                        }
                    }
                }
            }
        }
    }
}
