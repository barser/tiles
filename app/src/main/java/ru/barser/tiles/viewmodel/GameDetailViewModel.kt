package ru.barser.tiles.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ru.barser.tiles.data.GameDatabase
import ru.barser.tiles.data.GameEntity
import ru.barser.tiles.data.GameRepository
import ru.barser.tiles.data.PlayResultEntity
import ru.barser.tiles.data.PlayResultRepository
import ru.barser.tiles.data.PlayResultStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime

class GameDetailViewModel(
    application: Application,
    private val gameId: Int
) : AndroidViewModel(application) {

    private val gameRepository: GameRepository
    private val playResultRepository: PlayResultRepository

    private val _game = MutableStateFlow<GameEntity?>(null)
    val game: StateFlow<GameEntity?> = _game.asStateFlow()

    val results: StateFlow<List<PlayResultEntity>>

    init {
        val db = GameDatabase.getDatabase(application)
        gameRepository = GameRepository(db.gameDao())
        playResultRepository = PlayResultRepository(db.playResultDao())

        viewModelScope.launch {
            _game.value = gameRepository.getById(gameId)
        }

        results = playResultRepository.getByGameIdFlow(gameId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addResult(
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

    fun deleteResult(entity: PlayResultEntity) {
        viewModelScope.launch {
            playResultRepository.delete(entity)
        }
    }

    fun deleteGame(entity: GameEntity) {
        viewModelScope.launch {
            gameRepository.delete(entity)
        }
    }
}
