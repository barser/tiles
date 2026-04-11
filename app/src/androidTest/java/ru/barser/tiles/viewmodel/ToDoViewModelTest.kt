package ru.barser.tiles.viewmodel

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
import ru.barser.tiles.data.PlayResultDao
import ru.barser.tiles.data.PlayResultRepository
import ru.barser.tiles.data.PlayResultStatus
import java.time.OffsetDateTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ToDoViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var db: GameDatabase
    private lateinit var viewModel: ToDoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java)
            .allowMainThreadQueries()
            .addMigrations(MIGRATION_1_2)
            .build()

        // Override getDatabase singleton
        GameDatabase::class.java.getDeclaredField("INSTANCE").apply {
            isAccessible = true
            set(null, db)
        }

        viewModel = ToDoViewModel(ApplicationProvider.getApplicationContext<Application>())
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
    fun todoList_initiallyEmpty() = runTest {
        viewModel.todoList.test {
            assertEquals(emptyList<GameEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addGame_addsGameToTodoList() = runTest {
        viewModel.addGame("Chess") {}

        viewModel.todoList.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Chess", items[0].gameTitle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addGame_withDuplicate_returnsDuplicateResult() = runTest {
        viewModel.addGame("Chess") {}

        var result: AddGameResult? = null
        viewModel.addGame("Chess") { result = it }

        // Даём корутине завершиться
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AddGameResult.Duplicate.InTodo, result)
    }

    @Test
    fun addGame_withHistoryDuplicate_returnsHistoryDuplicateResult() = runTest {
        // Добавляем игру и делаем её сыгранной
        var gameId = 0
        viewModel.addGame("Chess") {
            // Получим ID позже
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val games = db.gameDao().getTodoGames().first()
        val game = games.find { it.gameTitle == "Chess" }!!
        gameId = game.id

        // Добавляем play result
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

        // Теперь пытаемся добавить ту же игру
        var result: AddGameResult? = null
        viewModel.addGame("Chess") { result = it }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AddGameResult.Duplicate.InHistory, result)
    }

    @Test
    fun deleteGame_removesGameFromList() = runTest {
        viewModel.addGame("Chess") {}
        testDispatcher.scheduler.advanceUntilIdle()

        val game = db.gameDao().getTodoGames().first().find { it.gameTitle == "Chess" }!!
        viewModel.deleteGame(game)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.todoList.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateTitle_changesGameTitle() = runTest {
        viewModel.addGame("Chess") {}
        testDispatcher.scheduler.advanceUntilIdle()

        val game = db.gameDao().getTodoGames().first().find { it.gameTitle == "Chess" }!!
        viewModel.updateTitle(game.id, "Checkers")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.todoList.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Checkers", items[0].gameTitle)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
