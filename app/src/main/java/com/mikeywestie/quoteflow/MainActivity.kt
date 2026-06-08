package com.mikeywestie.quoteflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mikeywestie.quoteflow.navigation.QuoteFlowApp
import com.mikeywestie.quoteflow.ui.theme.QuoteFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as QuoteFlowApplication
        setContent {
            QuoteFlowTheme {
                QuoteFlowApp(repository = app.repository)
            }
        }
    }
}
