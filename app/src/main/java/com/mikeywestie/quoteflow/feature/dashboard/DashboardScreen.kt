package com.mikeywestie.quoteflow.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mikeywestie.quoteflow.navigation.Routes

@Composable
fun DashboardScreen(navController: NavController) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("QuoteFlow", fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("Mobile quotation builder", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(32.dp))
            DashboardButton("New Quote / Saved Quotes") { navController.navigate(Routes.QUOTES) }
            DashboardButton("Products") { navController.navigate(Routes.PRODUCTS) }
            DashboardButton("Customers") { navController.navigate(Routes.CUSTOMERS) }
        }
    }
}

@Composable
private fun DashboardButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(56.dp)) { Text(text) }
}
