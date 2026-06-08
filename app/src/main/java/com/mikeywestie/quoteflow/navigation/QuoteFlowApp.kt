package com.mikeywestie.quoteflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mikeywestie.quoteflow.data.repository.QuoteFlowRepository
import com.mikeywestie.quoteflow.feature.customers.CustomersScreen
import com.mikeywestie.quoteflow.feature.dashboard.DashboardScreen
import com.mikeywestie.quoteflow.feature.importdata.ImportScreen
import com.mikeywestie.quoteflow.feature.products.ProductsScreen
import com.mikeywestie.quoteflow.feature.quotes.QuoteDetailsScreen
import com.mikeywestie.quoteflow.feature.quotes.QuotesScreen
import com.mikeywestie.quoteflow.feature.settings.SettingsScreen
import com.mikeywestie.quoteflow.feature.templates.TemplatesScreen

@Composable
fun QuoteFlowApp(repository: QuoteFlowRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(repository, navController)
        }

        composable(Routes.PRODUCTS) {
            ProductsScreen(repository, navController)
        }

        composable(Routes.CUSTOMERS) {
            CustomersScreen(repository, navController)
        }

        composable(Routes.QUOTES) {
            QuotesScreen(repository, navController)
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(repository, navController)
        }

        composable(Routes.IMPORT_DATA) {
            ImportScreen(repository, navController)
        }

        composable(Routes.TEMPLATES) {
            TemplatesScreen(repository, navController)
        }

        composable(
            route = "${Routes.QUOTE_DETAILS}/{${Routes.QUOTE_ID_ARGUMENT}}",
            arguments = listOf(
                navArgument(Routes.QUOTE_ID_ARGUMENT) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getLong(Routes.QUOTE_ID_ARGUMENT) ?: 0L

            QuoteDetailsScreen(
                repository = repository,
                navController = navController,
                quoteId = quoteId
            )
        }
    }
}