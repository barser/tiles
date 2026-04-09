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

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val gameRepository: GameRepository
    private val playResultRepository: PlayResultRepository

    val historyList: StateFlow<List<GameEntity>>

    init {
        val db = GameDatabase.getDatabase(application)
        gameRepository = GameRepository(db.gameDao())
        playResultRepository = PlayResultRepository(db.playResultDao())

        historyList = gameRepository.getHistoryFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun deleteGame(entity: GameEntity) {
        viewModelScope.launch {
            gameRepository.delete(entity)
        }
    }

    fun addResult(
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
