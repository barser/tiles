package ru.barser.tiles.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1) Создаём новую таблицу games без колонки status
        database.execSQL(
            """
            CREATE TABLE games_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                gameTitle TEXT NOT NULL
            )
            """.trimIndent()
        )
        // 2) Копируем данные (id, gameTitle) из старой таблицы
        database.execSQL(
            "INSERT INTO games_new (id, gameTitle) SELECT id, gameTitle FROM games"
        )
        // 3) Удаляем старую таблицу
        database.execSQL("DROP TABLE games")
        // 4) Переименовываем
        database.execSQL("ALTER TABLE games_new RENAME TO games")

        // 5) Создаём таблицу play_results
        database.execSQL(
            """
            CREATE TABLE play_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                gameId INTEGER NOT NULL,
                playedAt TEXT NOT NULL,
                result TEXT NOT NULL,
                durationMinutes INTEGER,
                comment TEXT,
                FOREIGN KEY (gameId) REFERENCES games(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL("CREATE INDEX index_play_results_gameId ON play_results(gameId)")
    }
}

@Database(
    entities = [GameEntity::class, PlayResultEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(PlayResultConverters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun playResultDao(): PlayResultDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
