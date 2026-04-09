package com.example.myapplication

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.GameEntity
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.HistoryViewModel

class HistoryActivity : ComponentActivity() {
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HistoryScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier, viewModel: HistoryViewModel) {
    val context = LocalContext.current
    val items by viewModel.historyList.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf<Int?>(null) }
    var showAddResultDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "History",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (items.isEmpty()) {
                Text(
                    text = "Пока пусто",
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
                                .clickable {
                                    // Переход в карточку игры
                                    val intent = Intent(context, GameDetailActivity::class.java).apply {
                                        putExtra(GameDetailActivity.EXTRA_GAME_ID, entity.id)
                                    }
                                    context.startActivity(intent)
                                },
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

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (selectedId == null) {
                        Toast.makeText(context, "Выберите элемент", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    showAddResultDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Добавить результат")
            }

            Button(
                onClick = {
                    if (selectedId == null) {
                        Toast.makeText(context, "Выберите элемент", Toast.LENGTH_SHORT).show()
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
                Text("Удалить")
            }
        }
    }

    // Dialog добавления результата
    if (showAddResultDialog && selectedId != null) {
        PlayResultDialog(
            onConfirm = { playedAt, result, duration, comment ->
                viewModel.addResult(selectedId!!, playedAt, result, duration, comment)
                showAddResultDialog = false
                selectedId = null
            },
            onDismiss = {
                showAddResultDialog = false
                selectedId = null
            }
        )
    }
}
