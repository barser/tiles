package ru.barser.tiles.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ru.barser.tiles.data.GameDatabase
import ru.barser.tiles.data.GameEntity
import ru.barser.tiles.data.GameRepository
import ru.barser.tiles.data.PlayResultRepository
import ru.barser.tiles.data.PlayResultStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime

sealed interface AddGameResult {
    data object Success : AddGameResult
    sealed interface Duplicate : AddGameResult {
        data object InTodo : Duplicate
        data object InHistory : Duplicate
    }
}

class ToDoViewModel(application: Application) : AndroidViewModel(application) {
    private val gameRepository: GameRepository
    private val playResultRepository: PlayResultRepository

    val todoList: StateFlow<List<GameEntity>>

    init {
        val db = GameDatabase.getDatabase(application)
        gameRepository = GameRepository(db.gameDao())
        playResultRepository = PlayResultRepository(db.playResultDao())

        todoList = gameRepository.getTodoFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addGame(title: String, onResult: (AddGameResult) -> Unit) {
        viewModelScope.launch {
            val inHistory = gameRepository.isTitleInHistory(title)
            val exists = gameRepository.isTitleExists(title)

            val inTodo = exists && !inHistory

            when {
                inTodo -> onResult(AddGameResult.Duplicate.InTodo)
                inHistory -> onResult(AddGameResult.Duplicate.InHistory)
                else -> {
                    gameRepository.createGame(title)
                    onResult(AddGameResult.Success)
                }
            }
        }
    }

    fun updateTitle(id: Int, newTitle: String) {
        viewModelScope.launch {
            gameRepository.updateTitle(id, newTitle)
        }
    }

    fun deleteGame(entity: GameEntity) {
        viewModelScope.launch {
            gameRepository.delete(entity)
        }
    }

    fun markAsPlayed(
        gameId: Int,
        playedAt: OffsetDateTime,
        result: PlayResultStatus,
        duration: Duration?,
        comment: String?
    ) {
        viewModelScope.launch {
            playResultRepository.create(
                gameId = gameId,
                playedAt = playedAt,
                result = result,
                duration = duration,
                comment = comment
            )
        }
    }
}
