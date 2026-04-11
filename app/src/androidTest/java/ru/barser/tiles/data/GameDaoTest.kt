package ru.barser.tiles.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class GameDaoTest {

    private lateinit var db: GameDatabase
    private lateinit var dao: GameDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java)
            .allowMainThreadQueries()
            .addMigrations(MIGRATION_1_2)
            .build()
        dao = db.gameDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_andGetById_returnsGame() = runTest {
        val game = GameEntity(gameTitle = "Chess")
        val id = dao.insert(game).toInt()

        val result = dao.getById(id)
        assertNotNull(result)
        assertEquals("Chess", result!!.gameTitle)
    }

    @Test
    fun getTodoGames_returnsGamesWithoutPlayResults() = runTest {
        val game1 = GameEntity(gameTitle = "Chess")
        val game2 = GameEntity(gameTitle = "Monopoly")
        dao.insert(game1)
        dao.insert(game2)

        val todoGames = dao.getTodoGames().first()
        assertEquals(2, todoGames.size)
    }

    @Test
    fun getHistoryGames_returnsGamesWithPlayResults() = runTest {
        val game = GameEntity(gameTitle = "Chess")
        val gameId = dao.insert(game).toInt()

        val playResultDao = db.playResultDao()
        playResultDao.insert(
            PlayResultEntity(
                gameId = gameId,
                playedAt = "2024-01-01T10:00:00Z",
                result = PlayResultStatus.WIN,
                durationMinutes = 60,
                comment = null
            )
        )

        val historyGames = dao.getHistoryGames().first()
        assertEquals(1, historyGames.size)
        assertEquals("Chess", historyGames[0].gameTitle)
    }

    @Test
    fun update_changesGameTitle() = runTest {
        val game = GameEntity(gameTitle = "Old Title")
        dao.insert(game)

        game.gameTitle = "New Title"
        dao.update(game)

        val result = dao.getById(game.id)
        assertEquals("New Title", result!!.gameTitle)
    }

    @Test
    fun delete_removesGame() = runTest {
        val game = GameEntity(gameTitle = "Chess")
        dao.insert(game)

        dao.delete(game)

        val result = dao.getById(game.id)
        assertEquals(null, result)
    }

    @Test
    fun existsByTitle_returnsCount_whenTitleExists() = runTest {
        val game = GameEntity(gameTitle = "Chess")
        dao.insert(game)

        val count = dao.existsByTitle("Chess")
        assertEquals(1, count)
    }

    @Test
    fun existsByTitle_returnsZero_whenTitleDoesNotExist() = runTest {
        val count = dao.existsByTitle("Nonexistent")
        assertEquals(0, count)
    }

    @Test
    fun existsInHistoryByTitle_returnsCount_whenGameHasPlayResults() = runTest {
        val game = GameEntity(gameTitle = "Chess")
        val gameId = dao.insert(game).toInt()

        val playResultDao = db.playResultDao()
        playResultDao.insert(
            PlayResultEntity(
                gameId = gameId,
                playedAt = "2024-01-01T10:00:00Z",
                result = PlayResultStatus.WIN,
                durationMinutes = null,
                comment = null
            )
        )

        val count = dao.existsInHistoryByTitle("Chess")
        assertEquals(1, count)
    }

    @Test
    fun existsInHistoryByTitle_returnsZero_whenGameHasNoPlayResults() = runTest {
        val game = GameEntity(gameTitle = "Chess")
        dao.insert(game)

        val count = dao.existsInHistoryByTitle("Chess")
        assertEquals(0, count)
    }
}
