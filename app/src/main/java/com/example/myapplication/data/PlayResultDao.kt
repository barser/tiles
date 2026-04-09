package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayResultDao {

    @Query("SELECT * FROM play_results WHERE gameId = :gameId ORDER BY playedAt DESC")
    fun getByGameId(gameId: Int): Flow<List<PlayResultEntity>>

    @Query("SELECT * FROM play_results WHERE gameId = :gameId ORDER BY playedAt DESC")
    suspend fun getByGameIdSync(gameId: Int): List<PlayResultEntity>

    @Insert
    suspend fun insert(entity: PlayResultEntity): Long

    @Delete
    suspend fun delete(entity: PlayResultEntity)
}
