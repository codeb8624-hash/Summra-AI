package com.example.summraai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.summraai.core.common.UiState
import com.example.summraai.domain.model.HistoryItem
import com.example.summraai.ui.common.EmptyState
import com.example.summraai.ui.components.HistoryCard
import com.example.summraai.ui.components.SummraIconButton
import com.example.summraai.ui.components.SummraTopAppBar
import com.example.summraai.ui.theme.Spacing
import com.example.summraai.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val state by viewModel.historyItems.collectAsState()

    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        SummraTopAppBar(
            title = "History",
            onNavigationClick = onNavigateBack,
            actions = {
                SummraIconButton(
                    icon = Icons.Filled.DeleteSweep,
                    onClick = { viewModel.clearHistory() },
                    contentDescription = "Clear history"
                )
            }
        )

        when (val current = state) {
            is UiState.Empty, is UiState.Idle -> {
                EmptyState(
                    icon = Icons.Filled.History,
                    title = "No summaries yet",
                    message = "Your summarized content will appear here",
                    modifier = Modifier.weight(1f)
                )
            }
            is UiState.Loading -> {
                EmptyState(
                    icon = Icons.Filled.History,
                    title = "Loading...",
                    message = "",
                    modifier = Modifier.weight(1f)
                )
            }
            is UiState.Error -> {
                EmptyState(
                    icon = Icons.Filled.History,
                    title = "Error",
                    message = current.message,
                    modifier = Modifier.weight(1f)
                )
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(current.data) { item ->
                        HistoryCard(
                            title = item.title,
                            date = item.date,
                            summary = item.summary,
                            category = item.type.name,
                            onClick = { onNavigateToSummary(item.id) }
                        )
                    }
                }
            }
        }

    }
}
