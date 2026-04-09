package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: GameDao) {

    fun getTodoFlow(): Flow<List<GameEntity>> = dao.getTodoGames()

    fun getHistoryFlow(): Flow<List<GameEntity>> = dao.getHistoryGames()

    suspend fun createGame(gameTitle: String): Long {
        return dao.insert(GameEntity(gameTitle = gameTitle))
    }

    suspend fun updateTitle(id: Int, newTitle: String) {
        val entity = dao.getById(id) ?: return
        entity.gameTitle = newTitle
        dao.update(entity)
    }

    suspend fun delete(entity: GameEntity) {
        dao.delete(entity)
    }

    suspend fun getById(id: Int): GameEntity? = dao.getById(id)
}
