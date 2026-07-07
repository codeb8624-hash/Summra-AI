package com.example.summraai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.summraai.ui.components.BottomNavItem
import com.example.summraai.ui.components.SummraBottomNavigation
import com.example.summraai.ui.navigation.SummraNavGraph
import com.example.summraai.ui.navigation.SummraScreen
import com.example.summraai.ui.theme.SummraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SummraTheme {
                val navController = rememberNavController()
                MainScaffold(navController = navController)
            }
        }
    }
}

@Composable
private fun MainScaffold(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(
                    SummraScreen.Home.route,
                    SummraScreen.History.route,
                    SummraScreen.Collections.route
                )
            ) {
                SummraBottomNavigation(
                    items = listOf(
                        BottomNavItem("Home", Icons.Filled.Home, SummraScreen.Home.route, "Home tab"),
                        BottomNavItem("History", Icons.Filled.History, SummraScreen.History.route, "History tab"),
                        BottomNavItem("Collections", Icons.Filled.Folder, SummraScreen.Collections.route, "Collections tab"),
                    ),
                    selectedRoute = currentRoute ?: SummraScreen.Home.route,
                    onItemSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(SummraScreen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        SummraNavGraph(
            navController = navController,
            modifier = Modifier.padding(padding)
        )
    }
}
