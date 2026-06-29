package com.example.summraai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.summraai.ui.components.BottomNavItem
import com.example.summraai.ui.components.FeatureCard
import com.example.summraai.ui.components.SummraBottomNavigation
import com.example.summraai.ui.components.SummraIconButton
import com.example.summraai.ui.components.SummraSearchBar
import com.example.summraai.ui.components.SummraSectionHeader
import com.example.summraai.ui.components.SummraTopAppBar
import com.example.summraai.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    onNavigateToTextSummary: () -> Unit,
    onNavigateToPdfSummary: () -> Unit,
    onNavigateToWebsiteSummary: () -> Unit,
    onNavigateToYoutubeSummary: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        SummraTopAppBar(
            title = "Summra AI",
            actions = {
                SummraIconButton(
                    icon = Icons.Filled.Settings,
                    onClick = { /* Settings */ },
                    contentDescription = "Settings"
                )
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            SummraSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search summaries...",
                onSearch = { /* trigger search */ },
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "What would you like to summarize?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item { Spacer(modifier = Modifier.height(Spacing.sm)) }

                item {
                    FeatureCard(
                        icon = Icons.Filled.ContentPaste,
                        title = "Paste Text",
                        description = "Summarize any text or article you paste",
                        onClick = onNavigateToTextSummary
                    )
                }

                item {
                    FeatureCard(
                        icon = Icons.Filled.Language,
                        title = "Website URL",
                        description = "Summarize content from any webpage",
                        onClick = onNavigateToWebsiteSummary
                    )
                }

                item {
                    FeatureCard(
                        icon = Icons.Filled.VideoLibrary,
                        title = "YouTube Video",
                        description = "Get a summary of any YouTube video",
                        onClick = onNavigateToYoutubeSummary
                    )
                }

                item {
                    FeatureCard(
                        icon = Icons.Filled.AutoFixHigh,
                        title = "Upload Document",
                        description = "Summarize PDF or DOCX files",
                        onClick = onNavigateToPdfSummary
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    SummraSectionHeader(
                        title = "Recent Summaries",
                        actionText = "View all",
                        onActionClick = onNavigateToHistory
                    )
                }

                items(3) {
                    com.example.summraai.ui.components.HistoryCard(
                        title = "Sample Summary ${it + 1}",
                        date = "Today",
                        summary = "This is a placeholder summary...",
                        category = if (it == 0) "Text" else if (it == 1) "URL" else "Video",
                        onClick = { onNavigateToSummary("sample_$it") }
                    )
                }

                item { Spacer(modifier = Modifier.height(Spacing.xl)) }
            }
        }

        SummraBottomNavigation(
            items = listOf(
                BottomNavItem("Home", Icons.Filled.Home, "home", "Home tab"),
                BottomNavItem("History", Icons.Filled.History, "history", "History tab"),
                BottomNavItem("Collections", Icons.Filled.Folder, "collections", "Collections tab"),
            ),
            selectedRoute = "home",
            onItemSelected = { route ->
                when (route) {
                    "history" -> onNavigateToHistory()
                    "collections" -> onNavigateToCollections()
                }
            }
        )
    }
}
