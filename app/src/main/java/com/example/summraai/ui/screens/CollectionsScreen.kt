package com.example.summraai.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.summraai.ui.common.EmptyState
import com.example.summraai.ui.components.BottomNavItem
import com.example.summraai.ui.components.SummraBottomNavigation
import com.example.summraai.ui.components.SummraTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SummraTopAppBar(
            title = "Collections",
            onNavigationClick = onNavigateBack
        )

        EmptyState(
            icon = Icons.Filled.Folder,
            title = "No collections yet",
            message = "Organize your summaries into collections",
            modifier = Modifier.weight(1f)
        )

        SummraBottomNavigation(
            items = listOf(
                BottomNavItem("Home", Icons.Filled.Home, "home", "Home tab"),
                BottomNavItem("History", Icons.Filled.History, "history", "History tab"),
                BottomNavItem("Collections", Icons.Filled.Folder, "collections", "Collections tab"),
            ),
            selectedRoute = "collections",
            onItemSelected = { route ->
                when (route) {
                    "home" -> onNavigateBack()
                }
            }
        )
    }
}
