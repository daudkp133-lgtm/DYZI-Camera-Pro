package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GpsCamViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GpsCamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingLocationIntent(intent)
        setContent {
            MyApplicationTheme(darkTheme = true) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingLocationIntent(intent)
    }

    private fun handleIncomingLocationIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrBlank()) {
                        viewModel.importGoogleMapsLocation(sharedText)
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                val data = intent.data
                if (data != null && data.scheme == "geo") {
                    val geoUriString = data.toString()
                    viewModel.importGoogleMapsLocation(geoUriString)
                }
            }
        }
    }
}
