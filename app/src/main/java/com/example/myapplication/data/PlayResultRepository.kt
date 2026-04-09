package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.OffsetDateTime

class PlayResultRepository(private val dao: PlayResultDao) {

    fun getByGameIdFlow(gameId: Int): Flow<List<PlayResultEntity>> =
        dao.getByGameId(gameId)

    suspend fun getByGameIdSync(gameId: Int): List<PlayResultEntity> =
        dao.getByGameIdSync(gameId)

    suspend fun create(
        gameId: Int,
        playedAt: OffsetDateTime = OffsetDateTime.now(),
        result: PlayResultStatus,
        duration: Duration? = null,
        comment: String? = null
    ): Long {
        return dao.insert(
            PlayResultEntity(
                gameId = gameId,
                playedAt = playedAt.toString(),
                result = result,
                durationMinutes = duration?.toMinutes()?.toInt(),
                comment = comment
            )
        )
    }

    suspend fun delete(entity: PlayResultEntity) {
        dao.delete(entity)
    }
}
