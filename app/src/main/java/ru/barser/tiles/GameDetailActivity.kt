package ru.barser.tiles

import android.content.Intent
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.barser.tiles.data.GameEntity
import ru.barser.tiles.data.PlayResultEntity
import ru.barser.tiles.data.PlayResultStatus
import ru.barser.tiles.ui.theme.MyApplicationTheme
import ru.barser.tiles.viewmodel.GameDetailViewModel
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class GameDetailActivity : ComponentActivity() {

    private val viewModel: GameDetailViewModel by viewModels {
        val gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)
        GameDetailViewModelFactory(application, gameId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameDetailScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_GAME_ID = "game_id"
    }
}

@Composable
fun GameDetailScreen(modifier: Modifier = Modifier, viewModel: GameDetailViewModel) {
    val context = LocalContext.current
    val game by viewModel.game.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    var selectedResultId by remember { mutableStateOf<Int?>(null) }
    var showAddResultDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Название игры
        Text(
            text = game?.gameTitle ?: "Загрузка...",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Список результатов
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (results.isEmpty()) {
                Text(
                    text = "Результатов пока нет",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results, key = { it.id }) { result ->
                        val isSelected = result.id == selectedResultId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedResultId = result.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val dt = OffsetDateTime.parse(result.playedAt)
                                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                                Text(
                                    text = dt.format(formatter),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = result.result.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                result.durationMinutes?.let { mins ->
                                    val h = mins / 60
                                    val m = mins % 60
                                    val durText = if (h > 0) "${h}ч ${m}м" else "${m}м"
                                    Text(
                                        text = durText,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                result.comment?.takeIf { it.isNotBlank() }?.let { c ->
                                    Text(
                                        text = c,
                                        fontSize = 14.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddResultDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Добавить результат")
            }

            Button(
                onClick = {
                    if (selectedResultId == null) {
                        Toast.makeText(context, "Выберите результат", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val entity = results.find { it.id == selectedResultId }
                    if (entity != null) {
                        viewModel.deleteResult(entity)
                    }
                    selectedResultId = null
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Удалить результат")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val g = game
                if (g != null) {
                    viewModel.deleteGame(g)
                    (context as? ComponentActivity)?.finish()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Удалить игру")
        }
    }

    // Dialog добавления результата
    if (showAddResultDialog) {
        PlayResultDialog(
            onConfirm = { playedAt, result, duration, comment ->
                viewModel.addResult(playedAt, result, duration, comment)
                showAddResultDialog = false
            },
            onDismiss = { showAddResultDialog = false }
        )
    }
}

// ---- Factory для создания ViewModel с параметром ----

class GameDetailViewModelFactory(
    private val app: android.app.Application,
    private val gameId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameDetailViewModel(app, gameId) as T
    }
}
