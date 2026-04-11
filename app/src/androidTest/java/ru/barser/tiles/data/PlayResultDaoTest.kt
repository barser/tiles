package ru.barser.tiles.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.Duration
import java.time.OffsetDateTime

@RunWith(AndroidJUnit4::class)
class PlayResultDaoTest {

    private lateinit var db: GameDatabase
    private lateinit var gameDao: GameDao
    private lateinit var dao: PlayResultDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java)
            .allowMainThreadQueries()
            .addMigrations(MIGRATION_1_2)
            .build()
        gameDao = db.gameDao()
        dao = db.playResultDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_andGetByGameId_returnsPlayResult() = runTest {
        val gameId = gameDao.insert(GameEntity(gameTitle = "Chess")).toInt()

        val result = PlayResultEntity(
            gameId = gameId,
            playedAt = "2024-03-15T14:30:00+03:00",
            result = PlayResultStatus.WIN,
            durationMinutes = 90,
            comment = "Great game"
        )
        dao.insert(result)

        val results = dao.getByGameId(gameId).first()
        assertEquals(1, results.size)
        assertEquals(PlayResultStatus.WIN, results[0].result)
        assertEquals(90, results[0].durationMinutes)
        assertEquals("Great game", results[0].comment)
    }

    @Test
    fun getByGameIdSync_returnsPlayResults() = runTest {
        val gameId = gameDao.insert(GameEntity(gameTitle = "Chess")).toInt()

        dao.insert(
            PlayResultEntity(
                gameId = gameId,
                playedAt = "2024-01-01T10:00:00Z",
                result = PlayResultStatus.LOSS,
                durationMinutes = null,
                comment = null
            )
        )

        val results = dao.getByGameIdSync(gameId)
        assertEquals(1, results.size)
        assertEquals(PlayResultStatus.LOSS, results[0].result)
    }

    @Test
    fun getByGameId_returnsResultsOrderedByDateDescending() = runTest {
        val gameId = gameDao.insert(GameEntity(gameTitle = "Chess")).toInt()

        dao.insert(
            PlayResultEntity(
                gameId = gameId,
                playedAt = "2024-01-01T10:00:00Z",
                result = PlayResultStatus.WIN,
                durationMinutes = null,
                comment = null
            )
        )
        dao.insert(
            PlayResultEntity(
                gameId = gameId,
                playedAt = "2024-03-15T14:30:00+03:00",
                result = PlayResultStatus.LOSS,
                durationMinutes = null,
                comment = null
            )
        )

        val results = dao.getByGameId(gameId).first()
        assertEquals(2, results.size)
        // Более свежий результат должен быть первым
        assertEquals("2024-03-15T14:30:00+03:00", results[0].playedAt)
    }

    @Test
    fun delete_removesPlayResult() = runTest {
        val gameId = gameDao.insert(GameEntity(gameTitle = "Chess")).toInt()

        val result = PlayResultEntity(
            gameId = gameId,
            playedAt = "2024-01-01T10:00:00Z",
            result = PlayResultStatus.WIN,
            durationMinutes = null,
            comment = null
        )
        dao.insert(result)

        dao.delete(result)

        val results = dao.getByGameIdSync(gameId)
        assertEquals(0, results.size)
    }

    @Test
    fun cascadeDelete_removesPlayResultsWhenGameDeleted() = runTest {
        val game = GameEntity(gameTitle = "Chess")
        gameDao.insert(game)

        val result = PlayResultEntity(
            gameId = game.id,
            playedAt = "2024-01-01T10:00:00Z",
            result = PlayResultStatus.WIN,
            durationMinutes = null,
            comment = null
        )
        dao.insert(result)

        gameDao.delete(game)

        val results = dao.getByGameIdSync(game.id)
        assertEquals(0, results.size)
    }
}
