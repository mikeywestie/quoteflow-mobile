package com.mikeywestie.quoteflow.feature.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.data.local.entity.CompanySettings
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: QuoteFlowRepository,
    navController: NavController
) {
    val context = LocalContext.current
    val savedSettings by repository.companySettings().collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()

    var companyName by remember(savedSettings) { mutableStateOf(savedSettings?.companyName ?: "") }
    var phone by remember(savedSettings) { mutableStateOf(savedSettings?.phone ?: "") }
    var email by remember(savedSettings) { mutableStateOf(savedSettings?.email ?: "") }
    var address by remember(savedSettings) { mutableStateOf(savedSettings?.address ?: "") }
    var vatNumber by remember(savedSettings) { mutableStateOf(savedSettings?.vatNumber ?: "") }
    var registrationNumber by remember(savedSettings) { mutableStateOf(savedSettings?.registrationNumber ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Company Settings") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsHeaderCard(
                companyName = companyName,
                email = email,
                phone = phone
            )

            SettingsSectionCard(
                title = "Company Information",
                subtitle = "These details appear on generated quote PDFs."
            ) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company name") },
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
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            SettingsSectionCard(
                title = "Compliance Details",
                subtitle = "Optional business registration information for official documents."
            ) {
                OutlinedTextField(
                    value = vatNumber,
                    onValueChange = { vatNumber = it },
                    label = { Text("VAT number") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = registrationNumber,
                    onValueChange = { registrationNumber = it },
                    label = { Text("Registration number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SettingsSectionCard(
                title = "PDF Branding",
                subtitle = "Logo and branded document styling will be added in a future update."
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("Logo upload coming soon") }
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        repository.saveCompanySettings(
                            CompanySettings(
                                companyName = companyName.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                address = address.trim(),
                                vatNumber = vatNumber.trim(),
                                registrationNumber = registrationNumber.trim()
                            )
                        )

                        Toast.makeText(
                            context,
                            "Company settings saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = companyName.isNotBlank()
            ) {
                Text("Save Settings")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsHeaderCard(
    companyName: String,
    email: String,
    phone: String
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = companyName.ifBlank { "QuoteFlow Company Profile" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Quote PDF identity and business details",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (email.isNotBlank()) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (phone.isNotBlank()) {
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            content()
        }
    }
}