package com.mikeywestie.quoteflow.feature.importdata

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.local.entity.Product
import com.mikeywestie.quoteflow.data.local.entity.QuoteTemplate
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    repository: QuoteFlowRepository,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var lastResult by remember { mutableStateOf("No import completed yet.") }

    val productCsvPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            try {
                val text = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()

                val products = parseProductsCsv(text)
                val importedCount = repository.importProducts(products)

                lastResult = "Imported $importedCount products successfully."

                Toast.makeText(
                    context,
                    lastResult,
                    Toast.LENGTH_LONG
                ).show()
            } catch (ex: Exception) {
                lastResult = "Product import failed: ${ex.message}"

                Toast.makeText(
                    context,
                    lastResult,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val templateCsvPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            try {
                val text = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()

                val templates = parseTemplatesCsv(text)
                val importedCount = repository.importTemplates(templates)

                lastResult = "Imported $importedCount templates successfully."

                Toast.makeText(
                    context,
                    lastResult,
                    Toast.LENGTH_LONG
                ).show()
            } catch (ex: Exception) {
                lastResult = "Template import failed: ${ex.message}"

                Toast.makeText(
                    context,
                    lastResult,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Data") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Import Pipeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Use this screen to preload QuoteFlow with products and reusable quote templates."
            )

            Divider()

            Text(
                text = "Product Catalogue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Expected CSV columns:\nsku, name, category, defaultUnit, sourceAppearanceCount, active"
            )

            Button(
                onClick = {
                    productCsvPicker.launch(
                        arrayOf(
                            "text/*",
                            "text/comma-separated-values",
                            "application/csv",
                            "application/vnd.ms-excel",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Products CSV")
            }

            Divider()

            Text(
                text = "Templates Import",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Expected CSV columns:\ntemplateName, category, description, exampleQuotes, confidence, enabled"
            )

            Button(
                onClick = {
                    templateCsvPicker.launch(
                        arrayOf(
                            "text/*",
                            "text/comma-separated-values",
                            "application/csv",
                            "application/vnd.ms-excel",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Templates CSV")
            }

            Divider()

            Text(
                text = "Last Result",
                fontWeight = FontWeight.Bold
            )

            Text(lastResult)
        }
    }
}

private fun parseProductsCsv(csv: String): List<Product> {
    val lines = csv
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    if (lines.size <= 1) return emptyList()

    return lines
        .drop(1)
        .mapNotNull { line ->
            val columns = parseCsvLine(line)

            val sku = columns.getOrNull(0).orEmpty().trim()
            val name = columns.getOrNull(1).orEmpty().trim()
            val category = columns.getOrNull(2).orEmpty().trim().ifBlank { "General" }
            val unit = columns.getOrNull(3).orEmpty().trim().ifBlank { "Each" }
            val sourceAppearanceCount = columns.getOrNull(4).orEmpty().trim()
            val activeValue = columns.getOrNull(5).orEmpty().trim()

            if (name.isBlank()) return@mapNotNull null

            Product(
                sku = sku,
                name = name,
                description = if (sourceAppearanceCount.isNotBlank()) {
                    "Imported from catalogue. Source appearances: $sourceAppearanceCount"
                } else {
                    "Imported from catalogue."
                },
                category = category,
                unitPrice = 0.0,
                unit = unit,
                supplier = "Import",
                active = isTruthy(activeValue)
            )
        }
}

private fun parseTemplatesCsv(csv: String): List<QuoteTemplate> {
    val lines = csv
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    if (lines.size <= 1) return emptyList()

    return lines
        .drop(1)
        .mapNotNull { line ->
            val columns = parseCsvLine(line)

            val templateName = columns.getOrNull(0).orEmpty().trim()
            val category = columns.getOrNull(1).orEmpty().trim().ifBlank { "General" }
            val description = columns.getOrNull(2).orEmpty().trim()
            val exampleQuotes = columns.getOrNull(3).orEmpty().trim()
            val confidence = columns.getOrNull(4).orEmpty().trim()
            val enabledValue = columns.getOrNull(5).orEmpty().trim()

            if (templateName.isBlank()) return@mapNotNull null

            QuoteTemplate(
                templateName = templateName,
                category = category,
                description = description,
                exampleQuotes = exampleQuotes,
                confidence = confidence,
                enabled = enabledValue.isBlank() || isTruthy(enabledValue)
            )
        }
}

private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var insideQuotes = false

    var index = 0

    while (index < line.length) {
        val char = line[index]

        when {
            char == '"' -> {
                if (insideQuotes && index + 1 < line.length && line[index + 1] == '"') {
                    current.append('"')
                    index++
                } else {
                    insideQuotes = !insideQuotes
                }
            }

            char == ',' && !insideQuotes -> {
                result.add(current.toString())
                current.clear()
            }

            else -> current.append(char)
        }

        index++
    }

    result.add(current.toString())

    return result
}

private fun isTruthy(value: String): Boolean {
    return value.equals("true", ignoreCase = true) ||
            value.equals("yes", ignoreCase = true) ||
            value.equals("y", ignoreCase = true) ||
            value == "1"
}