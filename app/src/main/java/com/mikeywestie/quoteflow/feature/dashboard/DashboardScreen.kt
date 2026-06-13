package com.mikeywestie.quoteflow.feature.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val context = LocalContext.current
    val bugReportUrl =
        "https://github.com/mikeywestie/quoteflow-mobile/issues/new?labels=bug&title=Bug%20Report%3A%20"

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroCard()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Products",
                        value = products.value.size.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Customers",
                        value = customers.value.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Quotes",
                        value = quotes.value.size.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Templates",
                        value = templates.value.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                ActionCard(
                    title = "Quotes",
                    subtitle = "Create, duplicate and manage customer quotes",
                    actionText = "Open Quotes",
                    onClick = { navController.navigate(Routes.QUOTES) }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SmallActionCard(
                        title = "Templates",
                        subtitle = "Reusable quote packages",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.TEMPLATES) }
                    )

                    SmallActionCard(
                        title = "Products",
                        subtitle = "Catalogue and prices",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.PRODUCTS) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SmallActionCard(
                        title = "Customers",
                        subtitle = "Client records",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.CUSTOMERS) }
                    )

                    SmallActionCard(
                        title = "Settings",
                        subtitle = "Company details",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.SETTINGS) }
                    )
                }
            }

            item {
                ActionCard(
                    title = "Import Data",
                    subtitle = "Load products and quote templates from CSV files",
                    actionText = "Open Import",
                    onClick = { navController.navigate(Routes.IMPORT_DATA) }
                )
            }

            item {
                ActionCard(
                    title = "Report a Bug",
                    subtitle = "Found a problem or have a suggestion? Open a GitHub issue.",
                    actionText = "Report Bug",
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(bugReportUrl)
                        )
                        context.startActivity(intent)
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Quotes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = { navController.navigate(Routes.QUOTES) }
                    ) {
                        Text("View All")
                    }
                }
            }

            if (quotes.value.isEmpty()) {
                item {
                    EmptyRecentQuotesCard()
                }
            } else {
                items(quotes.value.take(5)) { quote ->
                    val customerName = customers.value
                        .firstOrNull { it.id == quote.customerId }
                        ?.customerName
                        ?: "Unknown customer"

                    RecentQuoteCard(
                        quoteNumber = quote.quoteNumber,
                        customerName = customerName,
                        status = quote.status,
                        total = quote.totalAmount.toRand(),
                        onClick = { navController.navigate(Routes.quoteDetails(quote.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard() {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "QuoteFlow Mobile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Quotation workspace",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Build quotes faster with products, templates, labour lines and PDFs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    actionText: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(actionText)
            }
        }
    }
}

@Composable
private fun SmallActionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.heightIn(min = 130.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Open",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyRecentQuotesCard() {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "No recent quotes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Create a quote from scratch or generate one from a template.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentQuoteCard(
    quoteNumber: String,
    customerName: String,
    status: String,
    total: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = quoteNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DashboardStatusChip(status)
            }

            Text(
                text = total,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DashboardStatusChip(status: String) {
    val normalized = status.trim().lowercase()

    val label = when (normalized) {
        "draft" -> "DRAFT"
        "sent" -> "SENT"
        "accepted" -> "ACCEPTED"
        "rejected" -> "REJECTED"
        "paid" -> "PAID"
        else -> status.uppercase()
    }

    val containerColor = when (normalized) {
        "draft" -> MaterialTheme.colorScheme.surfaceVariant
        "sent" -> MaterialTheme.colorScheme.primaryContainer
        "accepted" -> MaterialTheme.colorScheme.tertiaryContainer
        "rejected" -> MaterialTheme.colorScheme.errorContainer
        "paid" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val labelColor = when (normalized) {
        "draft" -> MaterialTheme.colorScheme.onSurfaceVariant
        "sent" -> MaterialTheme.colorScheme.onPrimaryContainer
        "accepted" -> MaterialTheme.colorScheme.onTertiaryContainer
        "rejected" -> MaterialTheme.colorScheme.onErrorContainer
        "paid" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = labelColor
        )
    )
}
