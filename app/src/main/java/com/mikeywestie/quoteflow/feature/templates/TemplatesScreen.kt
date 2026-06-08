package com.mikeywestie.quoteflow.feature.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    repository: QuoteFlowRepository,
    navController: NavController
) {
    val templates = repository.templates().collectAsStateWithLifecycle(initialValue = emptyList())

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
                    text = "Reusable quote foundations for common N&S jobs."
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

                            Text(
                                text = if (template.enabled) "Enabled" else "Disabled",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}