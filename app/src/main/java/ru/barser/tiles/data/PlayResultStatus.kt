package ru.barser.tiles.data

enum class PlayResultStatus(val displayName: String) {
    WIN("Победа"),
    LOSS("Проигрыш"),
    DRAW("Ничья"),
    UNFINISHED("Не доиграли");

    companion object {
        fun fromName(name: String): PlayResultStatus =
            entries.find { it.name == name } ?: WIN
    }
}
