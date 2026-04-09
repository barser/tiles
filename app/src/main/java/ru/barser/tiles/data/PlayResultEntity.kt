package ru.barser.tiles.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_results",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class PlayResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameId: Int,
    val playedAt: String,        // ISO-8601 OffsetDateTime
    val result: PlayResultStatus,
    val durationMinutes: Int?,   // nullable, in minutes
    val comment: String?
)
