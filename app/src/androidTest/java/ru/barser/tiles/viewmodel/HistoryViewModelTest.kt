package ru.barser.tiles.viewmodel

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
class HistoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var db: GameDatabase
    private lateinit var viewModel: HistoryViewModel

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

        viewModel = HistoryViewModel(ApplicationProvider.getApplicationContext<Application>())
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
    fun historyList_initiallyEmpty() = runTest {
        viewModel.historyList.test {
            assertEquals(emptyList<GameEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun historyList_showsGamesWithPlayResults() = runTest {
        // Добавляем игру и создаём play result
        val game = GameEntity(gameTitle = "Chess")
        db.gameDao().insert(game)

        db.playResultDao().insert(
            PlayResultEntity(
                gameId = game.id,
                playedAt = "2024-01-01T10:00:00Z",
                result = PlayResultStatus.WIN,
                durationMinutes = 60,
                comment = null
            )
        )

        viewModel.historyList.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Chess", items[0].gameTitle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteGame_removesGameFromHistory() = runTest {
        val game = GameEntity(gameTitle = "Chess")
        db.gameDao().insert(game)

        db.playResultDao().insert(
            PlayResultEntity(
                gameId = game.id,
                playedAt = "2024-01-01T10:00:00Z",
                result = PlayResultStatus.WIN,
                durationMinutes = null,
                comment = null
            )
        )

        viewModel.deleteGame(game)

        viewModel.historyList.test {
            val items = awaitItem()
            assertEquals(0, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addResult_addsPlayResultToGame() = runTest {
        val game = GameEntity(gameTitle = "Chess")
        db.gameDao().insert(game)

        // Сначала без play results — не в History
        // Добавляем result
        viewModel.addResult(
            gameId = game.id,
            playedAt = OffsetDateTime.parse("2024-03-15T14:30:00+03:00"),
            result = PlayResultStatus.LOSS,
            duration = null,
            comment = "Better luck next time"
        )

        // Теперь игра должна появиться в History
        viewModel.historyList.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
