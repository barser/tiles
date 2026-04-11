package ru.barser.tiles.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GameRepositoryTest {

    @Test
    fun getTodoFlow_returnsTodoGames() = runTest {
        val dao = mock<GameDao>()
        val expected = listOf(GameEntity(1, "Chess"))
        whenever(dao.getTodoGames()).thenReturn(flowOf(expected))

        val repository = GameRepository(dao)
        val result = repository.getTodoFlow().first()

        assertEquals(expected, result)
    }

    @Test
    fun getHistoryFlow_returnsHistoryGames() = runTest {
        val dao = mock<GameDao>()
        val expected = listOf(GameEntity(2, "Monopoly"))
        whenever(dao.getHistoryGames()).thenReturn(flowOf(expected))

        val repository = GameRepository(dao)
        val result = repository.getHistoryFlow().first()

        assertEquals(expected, result)
    }

    @Test
    fun getById_returnsGameEntity() = runTest {
        val dao = mock<GameDao>()
        val expected = GameEntity(1, "Chess")
        whenever(dao.getById(1)).thenReturn(expected)

        val repository = GameRepository(dao)
        val result = repository.getById(1)

        assertEquals(expected, result)
    }

    @Test
    fun getById_returnsNull_whenGameNotFound() = runTest {
        val dao = mock<GameDao>()
        whenever(dao.getById(99)).thenReturn(null)

        val repository = GameRepository(dao)
        val result = repository.getById(99)

        assertEquals(null, result)
    }

    @Test
    fun createGame_insertsGameAndReturnsId() = runTest {
        val dao = mock<GameDao>()
        whenever(dao.insert(mock())).thenReturn(42L)

        val repository = GameRepository(dao)
        val result = repository.createGame("New Game")

        assertEquals(42L, result)
    }

    @Test
    fun updateTitle_updatesExistingGame() = runTest {
        val dao = mock<GameDao>()
        val existingGame = GameEntity(1, "Old Title")
        whenever(dao.getById(1)).thenReturn(existingGame)

        val repository = GameRepository(dao)
        repository.updateTitle(1, "New Title")

        assertEquals("New Title", existingGame.gameTitle)
        verify(dao).update(existingGame)
    }

    @Test
    fun updateTitle_doesNothing_whenGameNotFound() = runTest {
        val dao = mock<GameDao>()
        whenever(dao.getById(99)).thenReturn(null)

        val repository = GameRepository(dao)
        repository.updateTitle(99, "New Title")

        verify(dao, org.mockito.kotlin.never()).update(mock())
    }

    @Test
    fun deleteGame_deletesGame() = runTest {
        val dao = mock<GameDao>()
        val game = GameEntity(1, "Chess")

        val repository = GameRepository(dao)
        repository.delete(game)

        verify(dao).delete(game)
    }

    @Test
    fun isTitleExists_returnsTrue_whenGameExists() = runTest {
        val dao = mock<GameDao>()
        whenever(dao.existsByTitle("Chess")).thenReturn(1)

        val repository = GameRepository(dao)
        val result = repository.isTitleExists("Chess")

        assertTrue(result)
    }

    @Test
    fun isTitleExists_returnsFalse_whenGameDoesNotExist() = runTest {
        val dao = mock<GameDao>()
        whenever(dao.existsByTitle("Chess")).thenReturn(0)

        val repository = GameRepository(dao)
        val result = repository.isTitleExists("Chess")

        assertFalse(result)
    }

    @Test
    fun isTitleInHistory_returnsTrue_whenGameInHistory() = runTest {
        val dao = mock<GameDao>()
        whenever(dao.existsInHistoryByTitle("Monopoly")).thenReturn(2)

        val repository = GameRepository(dao)
        val result = repository.isTitleInHistory("Monopoly")

        assertTrue(result)
    }

    @Test
    fun isTitleInHistory_returnsFalse_whenGameNotInHistory() = runTest {
        val dao = mock<GameDao>()
        whenever(dao.existsInHistoryByTitle("Monopoly")).thenReturn(0)

        val repository = GameRepository(dao)
        val result = repository.isTitleInHistory("Monopoly")

        assertFalse(result)
    }
}
