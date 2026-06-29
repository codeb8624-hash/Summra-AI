package com.example.summraai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.summraai.ui.components.SummraTopAppBar
import com.example.summraai.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SummraTopAppBar(
            title = "Settings",
            onNavigationClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                ListItem(
                    headlineContent = { Text("Summary Style") },
                    supportingContent = { Text("Concise") }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Default Language") },
                    supportingContent = { Text("English") }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Max Length") },
                    supportingContent = { Text("Medium") }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                ListItem(
                    headlineContent = { Text("Notifications") },
                    supportingContent = { Text("Enabled") }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Theme") },
                    supportingContent = { Text("System default") }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("About") },
                    supportingContent = { Text("Version 1.0.0") },
                    modifier = Modifier.clickable(onClick = onNavigateToAbout)
                )
            }
        }
    }
}
