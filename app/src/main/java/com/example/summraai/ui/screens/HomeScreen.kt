package com.example.summraai.ui.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.summraai.core.common.UiState
import com.example.summraai.domain.model.Summary
import com.example.summraai.viewmodel.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.summraai.ui.components.FeatureCard
import com.example.summraai.ui.components.SummraConfirmationDialog
import com.example.summraai.ui.components.SummraIconButton
import com.example.summraai.ui.components.SummraSearchBar
import com.example.summraai.ui.components.SummraSectionHeader
import com.example.summraai.ui.components.SummraTopAppBar
import com.example.summraai.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    onNavigateToTextSummary: () -> Unit,
    onNavigateToPdfSummary: () -> Unit,
    onNavigateToWebsiteSummary: () -> Unit,
    onNavigateToYoutubeSummary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val recentState by viewModel.recentSummaries.collectAsState()
    val collections by viewModel.collections.collectAsState()
    val context = LocalContext.current

    var expandedMenuSummaryId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var summaryToDelete by remember { mutableStateOf<Summary?>(null) }
    var showCollectionDialog by remember { mutableStateOf(false) }
    var summaryForCollection by remember { mutableStateOf<Summary?>(null) }

    if (showDeleteDialog && summaryToDelete != null) {
        val s = summaryToDelete!!
        SummraConfirmationDialog(
            title = "Delete Summary",
            message = "Are you sure you want to delete \"${s.title}\"?",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm = {
                viewModel.deleteSummary(s.id)
                showDeleteDialog = false
                summaryToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                summaryToDelete = null
            }
        )
    }

    if (showCollectionDialog && summaryForCollection != null) {
        val s = summaryForCollection!!
        AlertDialog(
            onDismissRequest = {
                showCollectionDialog = false
                summaryForCollection = null
            },
            title = {
                Text(
                    text = "Add to Collection",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                if (collections.isEmpty()) {
                    Text(
                        text = "No collections yet. Create one from the Collections tab.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column {
                        collections.forEach { collection ->
                            TextButton(
                                onClick = {
                                    viewModel.addToCollection(s.id, collection.id)
                                    showCollectionDialog = false
                                    summaryForCollection = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Text(
                                    text = collection.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${collection.summaryCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCollectionDialog = false
                    summaryForCollection = null
                }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SummraTopAppBar(
            title = "Summra AI",
            actions = {
                SummraIconButton(
                    icon = Icons.Filled.Settings,
                    onClick = onNavigateToSettings,
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
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = "Search summaries...",
                leadingIcon = Icons.Filled.Search,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                if (searchQuery.isBlank()) {
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

                    when (val s = recentState) {
                        is UiState.Success -> {
                            items(items = s.data) { summary ->
                                SummaryCardWithMenu(
                                    summary = summary,
                                    isExpanded = expandedMenuSummaryId == summary.id,
                                    onTap = { onNavigateToSummary(summary.id) },
                                    onLongClick = { expandedMenuSummaryId = summary.id },
                                    onDismissMenu = { expandedMenuSummaryId = null },
                                    onDelete = {
                                        summaryToDelete = summary
                                        showDeleteDialog = true
                                        expandedMenuSummaryId = null
                                    },
                                    onShare = {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, summary.title)
                                            putExtra(Intent.EXTRA_TEXT, "${summary.title}\n\n${summary.content}")
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Summary"))
                                        expandedMenuSummaryId = null
                                    },
                                    onAddToCollection = {
                                        summaryForCollection = summary
                                        showCollectionDialog = true
                                        expandedMenuSummaryId = null
                                    },
                                    onToggleBookmark = {
                                        viewModel.toggleBookmark(summary.id)
                                        expandedMenuSummaryId = null
                                    }
                                )
                            }
                        }
                        is UiState.Empty -> {
                            item {
                                Text(
                                    text = "No summaries yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = Spacing.md)
                                )
                            }
                        }
                        is UiState.Error -> {
                            item {
                                Text(
                                    text = s.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(vertical = Spacing.md)
                                )
                            }
                        }
                        is UiState.Loading, is UiState.Idle -> {}
                    }
                } else {
                    when (val results = searchResults) {
                        is UiState.Success -> {
                            items(items = results.data) { summary ->
                                SummaryCardWithMenu(
                                    summary = summary,
                                    isExpanded = expandedMenuSummaryId == summary.id,
                                    onTap = { onNavigateToSummary(summary.id) },
                                    onLongClick = { expandedMenuSummaryId = summary.id },
                                    onDismissMenu = { expandedMenuSummaryId = null },
                                    onDelete = {
                                        summaryToDelete = summary
                                        showDeleteDialog = true
                                        expandedMenuSummaryId = null
                                    },
                                    onShare = {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, summary.title)
                                            putExtra(Intent.EXTRA_TEXT, "${summary.title}\n\n${summary.content}")
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Summary"))
                                        expandedMenuSummaryId = null
                                    },
                                    onAddToCollection = {
                                        summaryForCollection = summary
                                        showCollectionDialog = true
                                        expandedMenuSummaryId = null
                                    },
                                    onToggleBookmark = {
                                        viewModel.toggleBookmark(summary.id)
                                        expandedMenuSummaryId = null
                                    }
                                )
                            }
                        }
                        is UiState.Empty -> {
                            item {
                                Text(
                                    text = "No summaries found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = Spacing.md)
                                )
                            }
                        }
                        is UiState.Error -> {
                            item {
                                Text(
                                    text = results.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(vertical = Spacing.md)
                                )
                            }
                        }
                        is UiState.Loading, is UiState.Idle -> {}
                    }
                }

                item { Spacer(modifier = Modifier.height(Spacing.xl)) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SummaryCardWithMenu(
    summary: Summary,
    isExpanded: Boolean,
    onTap: () -> Unit,
    onLongClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onAddToCollection: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Box {
        com.example.summraai.ui.components.HistoryCard(
            title = summary.title,
            date = summary.createdAt.toFormattedDate(),
            summary = summary.content,
            category = summary.type.name,
            onClick = onTap,
            modifier = Modifier.combinedClickable(
                onClick = onTap,
                onLongClick = onLongClick
            )
        )

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = onDismissMenu
        ) {
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = onDelete,
                leadingIcon = {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("Share") },
                onClick = onShare,
                leadingIcon = {
                    Icon(Icons.Filled.Share, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("Add to Collection") },
                onClick = onAddToCollection,
                leadingIcon = {
                    Icon(Icons.Filled.Folder, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { if (summary.isBookmarked) Text("Remove Favorite") else Text("Favorite") },
                onClick = onToggleBookmark,
                leadingIcon = {
                    Icon(
                        imageVector = if (summary.isBookmarked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

private fun Long.toFormattedDate(): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}
