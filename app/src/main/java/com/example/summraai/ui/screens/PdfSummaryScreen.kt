package com.example.summraai.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.summraai.core.common.UiState
import com.example.summraai.domain.model.SummaryStyle
import com.example.summraai.ui.components.SummraPrimaryButton
import com.example.summraai.ui.components.SummraTopAppBar
import com.example.summraai.ui.theme.IconSize
import com.example.summraai.ui.theme.Spacing
import com.example.summraai.viewmodel.PdfSummaryViewModel

@Composable
fun PdfSummaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: PdfSummaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedStyle by remember { mutableStateOf(SummaryStyle.CONCISE) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            selectedFileName = getFileName(it, context.contentResolver)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SummraTopAppBar(
            title = "PDF Summary",
            onNavigationClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md)
        ) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pdfPickerLauncher.launch("application/pdf") }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (selectedFileName != null) selectedFileName!!
                        else "Tap to select a PDF file",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = if (selectedFileName != null) FontWeight.Medium else FontWeight.Normal,
                        color = if (selectedFileName != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    Text(
                        text = if (selectedFileName != null) "Tap to change file"
                        else "Supported: PDF documents",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

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
                onClick = {
                    selectedUri?.let { uri ->
                        viewModel.generatePdfSummary(uri, selectedStyle, context.contentResolver)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedUri != null && uiState !is UiState.Loading
            )

            Spacer(Modifier.height(Spacing.lg))

            when (val state = uiState) {
                is UiState.Idle, is UiState.Empty -> { /* nothing */ }
                is UiState.Loading -> LoadingIndicator()
                is UiState.Success -> PdfSummaryResultCard(
                    result = state.data,
                    onCopy = { clipboardManager.setText(AnnotatedString(state.data.content)) },
                    onClear = {
                        viewModel.resetState()
                        selectedUri = null
                        selectedFileName = null
                        selectedStyle = SummaryStyle.CONCISE
                    },
                    onRegenerate = {
                        selectedUri?.let { uri ->
                            viewModel.generatePdfSummary(uri, selectedStyle, context.contentResolver)
                        }
                    }
                )
                is UiState.Error -> ErrorCard(
                    message = state.message,
                    onRetry = {
                        selectedUri?.let { uri ->
                            viewModel.generatePdfSummary(uri, selectedStyle, context.contentResolver)
                        }
                    }
                )
            }
        }
    }
}

private fun getFileName(uri: Uri, contentResolver: android.content.ContentResolver): String? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
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
            text = "Processing PDF and generating summary...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PdfSummaryResultCard(
    result: com.example.summraai.data.repository.PdfSummaryResult,
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

                Text(
                    text = result.content,
                    style = MaterialTheme.typography.bodyLarge
                )

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
                        text = "${result.wordCount ?: 0} words",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(Spacing.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "File: ${result.fileName ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${result.pageCount ?: 0} pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(Spacing.md))

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
