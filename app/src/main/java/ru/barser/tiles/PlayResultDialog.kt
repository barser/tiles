package ru.barser.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.barser.tiles.R
import ru.barser.tiles.data.PlayResultStatus
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun PlayResultDialog(
    onConfirm: (OffsetDateTime, PlayResultStatus, Duration?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val now = OffsetDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    var dateTimeText by remember { mutableStateOf(now.format(formatter)) }
    var selectedStatus by remember { mutableStateOf(PlayResultStatus.WIN) }
    var durationText by remember { mutableStateOf("") }
    var commentText by remember { mutableStateOf("") }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.play_result_dialog_title)) },
        text = {
            Column {
                // Дата-время
                OutlinedTextField(
                    value = dateTimeText,
                    onValueChange = { dateTimeText = it },
                    label = { Text(stringResource(R.string.play_result_datetime_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Выпадающий список статуса
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedStatus.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.play_result_status_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        PlayResultStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.displayName) },
                                onClick = {
                                    selectedStatus = status
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Длительность (минуты, необязательно)
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text(stringResource(R.string.play_result_duration_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Комментарий (необязательно)
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text(stringResource(R.string.play_result_comment_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Парсим дату
                    val parsed = runCatching {
                        OffsetDateTime.parse(dateTimeText, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                    }.getOrNull() ?: now

                    val duration = durationText.toIntOrNull()?.let { Duration.ofMinutes(it.toLong()) }
                    val comment = commentText.takeIf { it.isNotBlank() }

                    onConfirm(parsed, selectedStatus, duration, comment)
                }
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
