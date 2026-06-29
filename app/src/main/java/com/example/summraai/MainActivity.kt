package com.example.summraai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.summraai.ui.navigation.SummraNavGraph
import com.example.summraai.ui.theme.SummraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SummraTheme {
                val navController = rememberNavController()
                SummraNavGraph(navController = navController)
            }
        }
    }
}
