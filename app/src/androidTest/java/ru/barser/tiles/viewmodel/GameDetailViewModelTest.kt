package ru.barser.tiles.viewmodel

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import ru.barser.tiles.data.GameDatabase
import ru.barser.tiles.data.GameEntity
import ru.barser.tiles.data.MIGRATION_1_2
import ru.barser.tiles.data.PlayResultEntity
import ru.barser.tiles.data.PlayResultStatus
import java.time.OffsetDateTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class GameDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var db: GameDatabase
    private lateinit var viewModel: GameDetailViewModel

    private lateinit var game: GameEntity

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java)
            .allowMainThreadQueries()
            .addMigrations(MIGRATION_1_2)
            .build()

        GameDatabase::class.java.getDeclaredField("INSTANCE").apply {
            isAccessible = true
            set(null, db)
        }

        // Создаём тестовую игру
        game = GameEntity(gameTitle = "Chess")
        val gameId = runBlocking { db.gameDao().insert(game).toInt() }
        game = game.copy(id = gameId)

        viewModel = GameDetailViewModel(
            ApplicationProvider.getApplicationContext<Application>(),
            gameId
        )
    }

    @After
    fun tearDown() {
        db.close()
        GameDatabase::class.java.getDeclaredField("INSTANCE").apply {
            isAccessible = true
            set(null, null)
        }
        Dispatchers.resetMain()
    }

    @Test
    fun game_loadsGameById() = runTest {
        viewModel.game.test {
            val loadedGame = awaitItem()
            assertNotNull(loadedGame)
            assertEquals("Chess", loadedGame!!.gameTitle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun results_initiallyEmpty() = runTest {
        viewModel.results.test {
            assertEquals(emptyList<PlayResultEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addResult_addsPlayResult() = runTest {
        viewModel.addResult(
            playedAt = OffsetDateTime.parse("2024-03-15T14:30:00+03:00"),
            result = PlayResultStatus.WIN,
            duration = null,
            comment = "Great game!"
        )

        viewModel.results.test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals(PlayResultStatus.WIN, results[0].result)
            assertEquals("Great game!", results[0].comment)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteResult_removesPlayResult() = runTest {
        val result = PlayResultEntity(
            gameId = game.id,
            playedAt = "2024-01-01T10:00:00Z",
            result = PlayResultStatus.WIN,
            durationMinutes = null,
            comment = null
        )
        db.playResultDao().insert(result)

        viewModel.deleteResult(result)

        viewModel.results.test {
            val results = awaitItem()
            assertEquals(0, results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteGame_removesGame() = runTest {
        viewModel.deleteGame(game)

        viewModel.game.test {
            // Game stays as previously loaded value
            cancelAndIgnoreRemainingEvents()
        }

        // Verify from DB
        val deletedGame = db.gameDao().getById(game.id)
        assertNull(deletedGame)
    }

    @Test
    fun game_returnsNull_forNonExistentGameId() = runTest {
        val nonExistentViewModel = GameDetailViewModel(
            ApplicationProvider.getApplicationContext<Application>(),
            999
        )

        nonExistentViewModel.game.test {
            val loadedGame = awaitItem()
            assertNull(loadedGame)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
