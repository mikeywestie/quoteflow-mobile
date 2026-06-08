package com.mikeywestie.quoteflow.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(companyName, { companyName = it }, label = { Text("Company name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(address, { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(vatNumber, { vatNumber = it }, label = { Text("VAT number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(registrationNumber, { registrationNumber = it }, label = { Text("Registration number") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        repository.saveCompanySettings(
                            CompanySettings(
                                companyName = companyName,
                                phone = phone,
                                email = email,
                                address = address,
                                vatNumber = vatNumber,
                                registrationNumber = registrationNumber
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
}
