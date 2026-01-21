package com.chonkcheck.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chonkcheck.android.presentation.navigation.ChonkCheckNavHost
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChonkCheckTheme {
                ChonkCheckNavHost()
            }
        }
    }
}
