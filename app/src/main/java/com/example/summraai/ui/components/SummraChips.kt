package com.example.summraai.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.example.summraai.ui.theme.Spacing

@Composable
fun TagChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    contentDescription: String? = null
) {
    SuggestionChip(
        onClick = onClick,
        modifier = modifier.semantics {
            this.contentDescription = contentDescription ?: label
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        },
        shape = MaterialTheme.shapes.small,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = null
    )
}

@Composable
fun SummaryStyleChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    contentDescription: String? = null
) {
    SuggestionChip(
        onClick = onClick,
        modifier = modifier.semantics {
            this.contentDescription = contentDescription ?: "$label chip"
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        },
        shape = MaterialTheme.shapes.medium,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = null
    )
}

@Composable
fun AiBadge(
    modifier: Modifier = Modifier,
    contentDescription: String = "AI powered"
) {
    SuggestionChip(
        onClick = {},
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
        },
        label = {
            Text(
                text = "AI",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        shape = MaterialTheme.shapes.small,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        border = null
    )
}
