package com.highliuk.manai.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.highliuk.manai.R
import com.highliuk.manai.domain.model.ReadingDirection
import com.highliuk.manai.domain.model.ReadingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Reading Mode
            SectionHeader(stringResource(R.string.reading_mode))
            RadioOption(
                label = stringResource(R.string.single_page),
                selected = settings.readingMode == ReadingMode.SINGLE_PAGE,
                onClick = { viewModel.updateReadingMode(ReadingMode.SINGLE_PAGE) },
            )
            RadioOption(
                label = stringResource(R.string.double_page),
                selected = settings.readingMode == ReadingMode.DOUBLE_PAGE,
                onClick = { viewModel.updateReadingMode(ReadingMode.DOUBLE_PAGE) },
            )
            RadioOption(
                label = stringResource(R.string.long_strip),
                selected = settings.readingMode == ReadingMode.LONG_STRIP,
                onClick = { viewModel.updateReadingMode(ReadingMode.LONG_STRIP) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reading Direction
            SectionHeader(stringResource(R.string.reading_direction))
            RadioOption(
                label = stringResource(R.string.right_to_left),
                selected = settings.readingDirection == ReadingDirection.RTL,
                onClick = { viewModel.updateReadingDirection(ReadingDirection.RTL) },
            )
            RadioOption(
                label = stringResource(R.string.left_to_right),
                selected = settings.readingDirection == ReadingDirection.LTR,
                onClick = { viewModel.updateReadingDirection(ReadingDirection.LTR) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Switches
            SwitchOption(
                label = stringResource(R.string.tap_navigation),
                checked = settings.tapNavigationEnabled,
                onCheckedChange = { viewModel.updateTapNavigationEnabled(it) },
            )
            SwitchOption(
                label = stringResource(R.string.cover_alone),
                checked = settings.coverAlone,
                onCheckedChange = { viewModel.updateCoverAlone(it) },
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun RadioOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun SwitchOption(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
