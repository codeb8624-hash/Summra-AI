package com.example.summraai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.summraai.core.common.UiState
import com.example.summraai.ui.common.EmptyState
import com.example.summraai.ui.components.CollectionCard
import com.example.summraai.ui.components.SummraTopAppBar
import com.example.summraai.ui.theme.Spacing
import com.example.summraai.viewmodel.CollectionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    viewModel: CollectionsViewModel = viewModel(factory = CollectionsViewModel.Factory)
) {
    val state by viewModel.collections.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        SummraTopAppBar(
            title = "Collections",
            onNavigationClick = onNavigateBack
        )

        when (val current = state) {
            is UiState.Empty, is UiState.Idle -> {
                EmptyState(
                    icon = Icons.Filled.Folder,
                    title = "No collections yet",
                    message = "Organize your summaries into collections",
                    modifier = Modifier.weight(1f)
                )
            }
            is UiState.Loading -> {
                EmptyState(
                    icon = Icons.Filled.Folder,
                    title = "Loading...",
                    message = "",
                    modifier = Modifier.weight(1f)
                )
            }
            is UiState.Error -> {
                EmptyState(
                    icon = Icons.Filled.Folder,
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
                    items(current.data) { collection ->
                        CollectionCard(
                            name = collection.name,
                            description = collection.description,
                            summaryCount = collection.summaryCount,
                            onClick = { onNavigateToSummary(collection.id) }
                        )
                    }
                }
            }
        }

    }
}
