package cc.cyliu.kegels.ui.settings

import android.app.Activity
import android.app.LocaleManager
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.cyliu.kegels.R
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val notifSettings by viewModel.notificationSettings.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    var kpm by rememberSaveable(config.kegelsPerMinute) { mutableIntStateOf(config.kegelsPerMinute) }
    var total by rememberSaveable(config.totalKegels) { mutableIntStateOf(config.totalKegels) }

    // Custom interval text state
    val intervalKey = when (notifSettings.intervalMinutes) {
        30 -> "30"; 60 -> "60"; 120 -> "120"; else -> "custom"
    }
    var customText by rememberSaveable { mutableStateOf(
        if (notifSettings.intervalMinutes !in listOf(30, 60, 120))
            notifSettings.intervalMinutes.toString() else ""
    ) }

    // Time picker visibility
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val savedMsg = stringResource(R.string.settings_saved)
    val errorMsg = stringResource(R.string.settings_error_range)
    LaunchedEffect(uiState) {
        when (uiState) {
            is SettingsUiState.Saved -> { snackbarHostState.showSnackbar(savedMsg); viewModel.clearUiState() }
            is SettingsUiState.Error -> { snackbarHostState.showSnackbar(errorMsg); viewModel.clearUiState() }
            else -> {}
        }
    }

    // Time picker dialogs
    if (showStartPicker) {
        TimePickerDialog(
            initialHour = notifSettings.nightStartHour,
            initialMinute = notifSettings.nightStartMinute,
            onConfirm = { h, m ->
                showStartPicker = false
                viewModel.saveNightMode(true, h, m, notifSettings.nightEndHour, notifSettings.nightEndMinute)
            },
            onDismiss = { showStartPicker = false }
        )
    }
    if (showEndPicker) {
        TimePickerDialog(
            initialHour = notifSettings.nightEndHour,
            initialMinute = notifSettings.nightEndMinute,
            onConfirm = { h, m ->
                showEndPicker = false
                viewModel.saveNightMode(true, notifSettings.nightStartHour, notifSettings.nightStartMinute, h, m)
            },
            onDismiss = { showEndPicker = false }
        )
    }

    val intervalMs = 60_000L / kpm
    val durationSec = (total * intervalMs) / 1000
    val durationMin = (durationSec / 60).toInt()
    val durationRemSec = (durationSec % 60).toInt()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ── Exercise Settings ──────────────────────────────────────────
            Text(stringResource(R.string.settings_exercise), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.settings_kegels_per_minute), style = MaterialTheme.typography.bodyLarge)
                Text("$kpm", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = kpm.toFloat(), onValueChange = { kpm = it.roundToInt() },
                valueRange = 10f..120f, steps = 21, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.settings_total_kegels), style = MaterialTheme.typography.bodyLarge)
                Text("$total", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = total.toFloat(), onValueChange = { total = it.roundToInt() },
                valueRange = 10f..300f, steps = 28, modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.session_duration_fmt, durationMin, durationRemSec),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { viewModel.saveConfig(kpm, total) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_save))
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // ── Reminder Interval ──────────────────────────────────────────
            Text(stringResource(R.string.settings_interval), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))

            val presets = listOf(
                "30" to stringResource(R.string.settings_interval_30min),
                "60" to stringResource(R.string.settings_interval_1hr),
                "120" to stringResource(R.string.settings_interval_2hr),
                "custom" to stringResource(R.string.settings_interval_custom)
            )
            presets.forEach { (key, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = intervalKey == key,
                        onClick = {
                            when (key) {
                                "30" -> viewModel.saveIntervalMinutes(30)
                                "60" -> viewModel.saveIntervalMinutes(60)
                                "120" -> viewModel.saveIntervalMinutes(120)
                                else -> { /* wait for user to enter value */ }
                            }
                        }
                    )
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                }
                if (key == "custom" && intervalKey == "custom") {
                    val customMinutes = customText.toIntOrNull()
                    val isValid = customMinutes != null && customMinutes in 15..480
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.settings_interval_custom_hint)) },
                        isError = customText.isNotEmpty() && !isValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (isValid) viewModel.saveIntervalMinutes(customMinutes!!)
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // ── Do Not Disturb ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.settings_night_mode), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = notifSettings.nightModeEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.saveNightMode(
                            enabled,
                            notifSettings.nightStartHour, notifSettings.nightStartMinute,
                            notifSettings.nightEndHour, notifSettings.nightEndMinute
                        )
                    }
                )
            }

            if (notifSettings.nightModeEnabled) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.settings_night_from), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = { showStartPicker = true }) {
                        Text("%02d:%02d".format(notifSettings.nightStartHour, notifSettings.nightStartMinute))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.settings_night_to), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = { showEndPicker = true }) {
                        Text("%02d:%02d".format(notifSettings.nightEndHour, notifSettings.nightEndMinute))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // ── Language ───────────────────────────────────────────────────
            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            val languageOptions = listOf(
                "system" to stringResource(R.string.settings_language_system),
                "en" to "English",
                "zh" to "中文"
            )
            val segmentShape = RoundedCornerShape(50)
            val borderColor = MaterialTheme.colorScheme.outline
            val selectedBg = MaterialTheme.colorScheme.secondaryContainer
            val selectedFg = MaterialTheme.colorScheme.onSecondaryContainer
            val unselectedFg = MaterialTheme.colorScheme.onSurface
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(segmentShape)
                    .border(1.dp, borderColor, segmentShape)
            ) {
                languageOptions.forEachIndexed { index, (tag, label) ->
                    if (index > 0) {
                        Box(Modifier.width(1.dp).fillMaxHeight().background(borderColor))
                    }
                    val isSelected = currentLanguage == tag
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (isSelected) selectedBg else MaterialTheme.colorScheme.surface)
                            .clickable {
                                viewModel.saveLanguage(tag)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val localeManager = context.getSystemService(LocaleManager::class.java)
                                    localeManager.applicationLocales = if (tag == "system") {
                                        LocaleList.getEmptyLocaleList()
                                    } else {
                                        LocaleList.forLanguageTags(tag)
                                    }
                                } else {
                                    if (tag == "system") {
                                        // Use the real device locale from the unmodified system
                                        // Resources — NOT Locale.getDefault() which may have been
                                        // overridden by a previous language selection this session.
                                        val systemLocale = android.content.res.Resources
                                            .getSystem().configuration.locales[0]
                                        Locale.setDefault(systemLocale)
                                    } else {
                                        val locale = Locale.forLanguageTag(tag)
                                        Locale.setDefault(locale)
                                        @Suppress("DEPRECATION")
                                        val cfg = Configuration(context.resources.configuration)
                                        cfg.setLocale(locale)
                                        @Suppress("DEPRECATION")
                                        context.resources.updateConfiguration(cfg, context.resources.displayMetrics)
                                    }
                                    (context as? Activity)?.recreate()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) selectedFg else unselectedFg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        text = { TimePicker(state = state) }
    )
}
