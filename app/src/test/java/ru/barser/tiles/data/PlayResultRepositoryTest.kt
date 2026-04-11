package ru.barser.tiles.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.OffsetDateTime

class PlayResultRepositoryTest {

    @Test
    fun getByGameIdFlow_returnsPlayResults() = runTest {
        val dao = mock<PlayResultDao>()
        val expected = listOf(
            PlayResultEntity(1, 1, "2024-01-01T10:00:00Z", PlayResultStatus.WIN, 60, "Great game")
        )
        whenever(dao.getByGameId(1)).thenReturn(flowOf(expected))

        val repository = PlayResultRepository(dao)
        val result = repository.getByGameIdFlow(1).first()

        assertEquals(expected, result)
    }

    @Test
    fun getByGameIdSync_returnsPlayResults() = runTest {
        val dao = mock<PlayResultDao>()
        val expected = listOf(
            PlayResultEntity(1, 1, "2024-01-01T10:00:00Z", PlayResultStatus.WIN, 60, null)
        )
        whenever(dao.getByGameIdSync(1)).thenReturn(expected)

        val repository = PlayResultRepository(dao)
        val result = repository.getByGameIdSync(1)

        assertEquals(expected, result)
    }

    @Test
    fun create_insertsPlayResultWithAllFields() = runTest {
        val dao = mock<PlayResultDao>()
        whenever(dao.insert(any())).thenReturn(42L)

        val repository = PlayResultRepository(dao)
        val playedAt = OffsetDateTime.parse("2024-03-15T14:30:00+03:00")
        val duration = Duration.ofMinutes(90)

        val result = repository.create(
            gameId = 1,
            playedAt = playedAt,
            result = PlayResultStatus.WIN,
            duration = duration,
            comment = "Excellent game"
        )

        assertEquals(42L, result)
        verify(dao).insert(
            PlayResultEntity(
                gameId = 1,
                playedAt = "2024-03-15T14:30:00+03:00",
                result = PlayResultStatus.WIN,
                durationMinutes = 90,
                comment = "Excellent game"
            )
        )
    }

    @Test
    fun create_insertsPlayResultWithNullableFields() = runTest {
        val dao = mock<PlayResultDao>()
        whenever(dao.insert(any())).thenReturn(1L)

        val repository = PlayResultRepository(dao)
        val playedAt = OffsetDateTime.parse("2024-03-15T14:30:00+03:00")

        repository.create(
            gameId = 2,
            playedAt = playedAt,
            result = PlayResultStatus.LOSS
        )

        verify(dao).insert(
            PlayResultEntity(
                gameId = 2,
                playedAt = "2024-03-15T14:30:00+03:00",
                result = PlayResultStatus.LOSS,
                durationMinutes = null,
                comment = null
            )
        )
    }

    @Test
    fun delete_deletesPlayResult() = runTest {
        val dao = mock<PlayResultDao>()
        val entity = PlayResultEntity(1, 1, "2024-01-01T10:00:00Z", PlayResultStatus.WIN, null, null)

        val repository = PlayResultRepository(dao)
        repository.delete(entity)

        verify(dao).delete(entity)
    }
}
