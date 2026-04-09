package ru.barser.tiles.data

import androidx.room.TypeConverter
import java.time.Duration
import java.time.OffsetDateTime

class PlayResultConverters {

    // --- OffsetDateTime ---

    @TypeConverter
    fun fromOffsetDateTime(value: OffsetDateTime?): String? = value?.toString()

    @TypeConverter
    fun toOffsetDateTime(value: String?): OffsetDateTime? = value?.let { OffsetDateTime.parse(it) }

    // --- Duration (stored as minutes) ---

    @TypeConverter
    fun fromDuration(value: Duration?): Long? = value?.toMinutes()

    @TypeConverter
    fun toDuration(value: Long?): Duration? = value?.let { Duration.ofMinutes(it) }

    // --- PlayResultStatus ---

    @TypeConverter
    fun fromPlayResultStatus(status: PlayResultStatus): String = status.name

    @TypeConverter
    fun toPlayResultStatus(value: String): PlayResultStatus = PlayResultStatus.fromName(value)
}
