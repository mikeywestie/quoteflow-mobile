package com.mikeywestie.quoteflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.feature.customers.CustomersScreen
import com.mikeywestie.quoteflow.feature.dashboard.DashboardScreen
import com.mikeywestie.quoteflow.feature.products.ProductsScreen
import com.mikeywestie.quoteflow.feature.quotes.QuotesScreen

@Composable
fun QuoteFlowApp(repository: QuoteFlowRepository) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) { DashboardScreen(navController) }
        composable(Routes.PRODUCTS) { ProductsScreen(repository, navController) }
        composable(Routes.CUSTOMERS) { CustomersScreen(repository, navController) }
        composable(Routes.QUOTES) { QuotesScreen(repository, navController) }
    }
}
