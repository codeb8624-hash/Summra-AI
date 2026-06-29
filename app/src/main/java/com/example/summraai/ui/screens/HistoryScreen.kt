package com.example.summraai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.summraai.ui.common.EmptyState
import com.example.summraai.ui.components.BottomNavItem
import com.example.summraai.ui.components.HistoryCard
import com.example.summraai.ui.components.SummraBottomNavigation
import com.example.summraai.ui.components.SummraTopAppBar
import com.example.summraai.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (String) -> Unit
) {
    val items = emptyList<HistoryItem>()

    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        SummraTopAppBar(
            title = "History",
            onNavigationClick = onNavigateBack
        )

        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.History,
                title = "No summaries yet",
                message = "Your summarized content will appear here",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentPadding = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(items) { item ->
                    HistoryCard(
                        title = item.title,
                        date = item.date,
                        summary = item.summary,
                        category = item.category,
                        onClick = { onNavigateToSummary(item.id) }
                    )
                }
            }
        }

        SummraBottomNavigation(
            items = listOf(
                BottomNavItem("Home", Icons.Filled.Home, "home", "Home tab"),
                BottomNavItem("History", Icons.Filled.History, "history", "History tab"),
                BottomNavItem("Collections", Icons.Filled.Folder, "collections", "Collections tab"),
            ),
            selectedRoute = "history",
            onItemSelected = { route ->
                when (route) {
                    "home" -> onNavigateBack()
                }
            }
        )
    }
}

private data class HistoryItem(
    val id: String,
    val title: String,
    val date: String,
    val summary: String,
    val category: String
)
