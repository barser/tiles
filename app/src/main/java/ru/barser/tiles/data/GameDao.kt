package ru.barser.tiles.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM games WHERE id NOT IN (SELECT DISTINCT gameId FROM play_results) ORDER BY id DESC")
    fun getTodoGames(): Flow<List<GameEntity>>

    @Query("SELECT DISTINCT g.* FROM games g INNER JOIN play_results pr ON g.id = pr.gameId ORDER BY g.id DESC")
    fun getHistoryGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: Int): GameEntity?

    @Query("SELECT COUNT(*) FROM games WHERE gameTitle = :title")
    suspend fun existsByTitle(title: String): Int

    @Insert
    suspend fun insert(entity: GameEntity): Long

    @Update
    suspend fun update(entity: GameEntity)

    @Delete
    suspend fun delete(entity: GameEntity)
}
