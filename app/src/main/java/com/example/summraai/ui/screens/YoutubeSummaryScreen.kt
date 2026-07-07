package com.example.summraai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.summraai.core.common.UiState
import com.example.summraai.data.repository.YoutubeSummaryResult
import com.example.summraai.domain.model.SummaryStyle
import com.example.summraai.ui.components.SummraPrimaryButton
import com.example.summraai.ui.components.SummraTopAppBar
import com.example.summraai.ui.theme.Spacing
import com.example.summraai.viewmodel.ChatUiMessage
import com.example.summraai.viewmodel.YoutubeSummaryViewModel
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults

@Composable
fun YoutubeSummaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: YoutubeSummaryViewModel = viewModel(factory = YoutubeSummaryViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    var url by rememberSaveable { mutableStateOf("") }
    var selectedStyle by rememberSaveable { mutableStateOf(SummaryStyle.CONCISE) }
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        SummraTopAppBar(
            title = "YouTube Summary",
            onNavigationClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item { Spacer(Modifier.height(Spacing.sm)) }

                item {
                    OutlinedTextField(
                        value = url,
                        onValueChange = {
                            if (it.length <= 2000) url = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://www.youtube.com/watch?v=...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        supportingText = { Text("${url.length} / 2000") }
                    )
                }

                item {
                    Column {
                        Text(
                            text = "Summary Style",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(Spacing.sm))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            items(SummaryStyle.entries) { style ->
                                FilterChip(
                                    selected = selectedStyle == style,
                                    onClick = { selectedStyle = style },
                                    label = { Text(style.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(Spacing.lg))

                        SummraPrimaryButton(
                            text = "Generate Summary",
                            onClick = { viewModel.generateSummary(url, selectedStyle) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = url.isNotBlank() && (url.contains("youtube.com/") || url.contains("youtu.be/")) && uiState !is UiState.Loading
                        )
                    }
                }

                when (val state = uiState) {
                    is UiState.Loading -> {
                        item { LoadingIndicator() }
                    }
                    is UiState.Success -> {
                        item {
                            YoutubeSummaryResultCard(
                                result = state.data,
                                onCopy = { clipboardManager.setText(AnnotatedString(state.data.content)) },
                                onClear = {
                                    viewModel.resetState()
                                    url = ""
                                    selectedStyle = SummaryStyle.CONCISE
                                },
                                onRegenerate = { viewModel.generateSummary(url, selectedStyle) }
                            )
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))
                            Text(
                                text = "Ask about this video",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(Spacing.sm))
                            TaskChips(onTaskClick = { viewModel.performTask(it) })
                        }

                        itemsIndexed(chatMessages) { index, message ->
                            ChatBubble(
                                message = message,
                                onCopy = { clipboardManager.setText(AnnotatedString(message.content)) }
                            )
                        }

                        if (isChatLoading) {
                            item {
                                ChatLoadingIndicator()
                            }
                        }
                        }

                    is UiState.Error -> {
                        item {
                            ErrorCard(
                                message = state.message,
                                onRetry = { viewModel.generateSummary(url, selectedStyle) }
                            )
                        }
                    }
                    else -> {}
                }

                item { Spacer(Modifier.height(Spacing.lg)) }
            }

            if (uiState is UiState.Success) {
                ChatInput(
                    onSend = { viewModel.askQuestion(it) },
                    enabled = !isChatLoading
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatUiMessage,
    onCopy: () -> Unit
) {
    val isUser = message.role == "user"
    val arrangement = if (isUser) Arrangement.End else Arrangement.Start
    val containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer
                         else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = arrangement
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (message.sources.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        message.sources.forEach { source ->
                            AssistChip(
                                onClick = { },
                                label = { Text(source, style = MaterialTheme.typography.labelSmall) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
            )
        }
    }
}

                if (!isUser) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.align(Alignment.End).size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy answer",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
        }
    }
}
}
}

}

private fun formatViewCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.0fK", count / 1_000.0)
        else -> count.toString()
    }
}

@Composable
private fun ChatLoadingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "AI is thinking...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatInput(
    onSend: (String) -> Unit,
    enabled: Boolean
) {
    var text by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(Spacing.md)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask anything about this video...") },
                maxLines = 4,
                enabled = enabled,
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (text.isNotBlank()) {
                        onSend(text)
                        text = ""
                    }
                })
            )

            Spacer(Modifier.width(Spacing.sm))

            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text)
                        text = ""
                    }
                },
                enabled = enabled && text.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun TaskChips(onTaskClick: (String) -> Unit) {
    val tasks = listOf(
        "notes" to "Notes",
        "quiz" to "Quiz",
        "flashcards" to "Flashcards",
        "important_questions" to "Exam Questions",
        "explain" to "Explain",
        "explain_10" to "Explain (10y/o)"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(tasks) { (type, label) ->
            SuggestionChip(
                onClick = { onTaskClick(type) },
                label = { Text(label) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "Generating summary...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun YoutubeSummaryResultCard(
    result: YoutubeSummaryResult,
    onCopy: () -> Unit,
    onClear: () -> Unit,
    onRegenerate: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row {
                        IconButton(onClick = onCopy) {
                            Text("Copy", style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = onClear) {
                            Text("Clear", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

                if (result.title != null) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Spacing.sm))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    if (result.channel != null) {
                        YoutubeMetadataChip(
                            icon = Icons.Default.Person,
                            text = result.channel
                        )
                    }
                    if (result.videoDurationFormatted != null) {
                        YoutubeMetadataChip(
                            icon = Icons.Default.Schedule,
                            text = result.videoDurationFormatted
                        )
                    }
                    if (result.viewCount != null) {
                        YoutubeMetadataChip(
                            icon = Icons.Default.Visibility,
                            text = formatViewCount(result.viewCount)
                        )
                    }
                }

                if (result.publishDate != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Published: ${result.publishDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(Spacing.sm))
                HorizontalDivider()
                Spacer(Modifier.height(Spacing.sm))

                Text(
                    text = result.content,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(Spacing.sm))

                HorizontalDivider()

                Spacer(Modifier.height(Spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Style: ${result.style.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${result.wordCount ?: result.content.split("\\s+".toRegex()).size} words",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(Spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (result.transcriptWordCount != null && result.transcriptWordCount > 0) {
                        val ratio = ((1.0 - (result.wordCount?.toDouble() ?: 0.0) / result.transcriptWordCount) * 100).toInt()
                        Text(
                            text = "Compressed by $ratio%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (result.durationSeconds != null && result.durationSeconds > 0) {
                        val totalSec = result.durationSeconds
                        val min = totalSec / 60
                        val sec = totalSec % 60
                        Text(
                            text = if (min > 0) "${min}m ${sec}s read" else "${sec}s read",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.sm))

                SummraPrimaryButton(
                    text = "Regenerate",
                    onClick = onRegenerate,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun YoutubeMetadataChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    AssistChip(
        onClick = { },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.md))
            SummraPrimaryButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
