package ru.barser.tiles

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.barser.tiles.R
import ru.barser.tiles.data.GameEntity
import ru.barser.tiles.data.PlayResultStatus
import ru.barser.tiles.ui.theme.TilesTheme
import ru.barser.tiles.viewmodel.ToDoViewModel
import java.time.Duration
import java.time.OffsetDateTime

class ToDoActivity : ComponentActivity() {
    private val viewModel: ToDoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TilesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ToDoScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ToDoScreen(modifier: Modifier = Modifier, viewModel: ToDoViewModel) {
    val context = LocalContext.current
    val items by viewModel.todoList.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf<Int?>(null) }

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPlayResultDialog by remember { mutableStateOf(false) }
    var pendingPlayGameId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.todo_screen_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (items.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_items),
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { entity ->
                        val isSelected = entity.id == selectedId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedId = entity.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = entity.gameTitle,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка "Сыграно" — открывает диалог результата
        Button(
            onClick = {
                if (selectedId == null) {
                    Toast.makeText(context, context.getString(R.string.select_item), Toast.LENGTH_SHORT).show()
                    return@Button
                }
                pendingPlayGameId = selectedId
                showPlayResultDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_played))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.btn_add))
            }

            Button(
                onClick = {
                    if (selectedId == null) {
                        Toast.makeText(context, context.getString(R.string.select_item), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    showEditDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.btn_edit))
            }

            Button(
                onClick = {
                    if (selectedId == null) {
                        Toast.makeText(context, context.getString(R.string.select_item), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val entity = items.find { it.id == selectedId }
                    if (entity != null) {
                        viewModel.deleteGame(entity)
                    }
                    selectedId = null
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.btn_delete))
            }
        }
    }

    // Dialog добавления
    if (showAddDialog) {
        GameTitleDialog(
            title = stringResource(R.string.add_game_title),
            onConfirm = { title ->
                viewModel.addGame(title)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Dialog редактирования
    if (showEditDialog && selectedId != null) {
        val entity = items.find { it.id == selectedId }
        if (entity != null) {
            GameTitleDialog(
                title = stringResource(R.string.dialog_edit_title),
                initialTitle = entity.gameTitle,
                onConfirm = { title ->
                    viewModel.updateTitle(selectedId!!, title)
                    showEditDialog = false
                    selectedId = null
                },
                onDismiss = {
                    showEditDialog = false
                    selectedId = null
                }
            )
        }
    }

    // Dialog результата игры
    if (showPlayResultDialog && pendingPlayGameId != null) {
        PlayResultDialog(
            onConfirm = { playedAt, result, duration, comment ->
                viewModel.markAsPlayed(pendingPlayGameId!!, playedAt, result, duration, comment)
                pendingPlayGameId = null
                showPlayResultDialog = false
                selectedId = null
            },
            onDismiss = {
                showPlayResultDialog = false
                pendingPlayGameId = null
            }
        )
    }
}

@Composable
fun GameTitleDialog(
    title: String,
    initialTitle: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.game_title_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text.trim())
                    }
                },
                enabled = text.isNotBlank()
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

@Preview(showBackground = true)
@Composable
fun ToDoScreenPreview() {
    TilesTheme {
        // Preview без ViewModel
    }
}
