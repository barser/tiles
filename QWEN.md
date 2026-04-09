# Tiles — Android Board Game List Management App

## Project Overview

This is an **Android board game management application** ("Tiles") built using **Kotlin**, **Jetpack Compose**, and **Room** for local data persistence. The app helps board game enthusiasts track their games, record play results, and manage their gaming history.

**Package Name:** `ru.barser.tiles`  
**Application ID:** `ru.barser.tiles`  
**Minimum SDK:** 26 (Android 7.0+)  
**Target SDK:** 36 (Android 16)  
**Compile SDK:** 36 (Android 16)  
**Java Compatibility:** Java 11  
**Repository:** https://github.com/barser/tiles

## Key Technologies & Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin 2.2.10 |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Database** | Room 2.7.0 with KSP 2.2.10-2.0.2 |
| **Architecture** | MVVM (Model-View-ViewModel) with StateFlow |
| **Build System** | Gradle 9.3.1 with Kotlin DSL |
| **Android Gradle Plugin** | 9.1.0 |
| **Dependency Injection** | Manual (ViewModels with SavedStateHandle) |

## Architecture

```
Room DAO → Repository → ViewModel (StateFlow) → Compose UI (collectAsStateWithLifecycle)
```

### Key Components:
- **GameEntity**: `id` (autoGenerate), `gameTitle`
- **PlayResultEntity**: `id` (autoGenerate), `gameId` (FK → games.id, CASCADE delete), `playedAt`, `result` (enum), `score`, `notes`
- **PlayResultStatus enum**: WIN(Победа), LOSS(Проигрыш), DRAW(Ничья), UNFINISHED(Не доиграли)
- **To Do** = games with NO play results; **History** = games with ≥1 play result

## Project Structure

```
MyApplication/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ru/barser/tiles/
│   │   │   │   ├── MainActivity.kt          # Main activity with Compose UI
│   │   │   │   ├── ToDoActivity.kt          # Games to play (no results)
│   │   │   │   ├── HistoryActivity.kt       # Games with play history
│   │   │   │   ├── GameDetailActivity.kt    # Game details with play results
│   │   │   │   ├── PlayResultDialog.kt      # Dialog for adding play results
│   │   │   │   ├── data/
│   │   │   │   │   ├── GameEntity.kt        # Game data model
│   │   │   │   │   ├── GameDao.kt           # Game database operations
│   │   │   │   │   ├── GameDatabase.kt      # Room database with migrations
│   │   │   │   │   ├── GameRepository.kt    # Game repository
│   │   │   │   │   ├── PlayResultEntity.kt  # Play result data model
│   │   │   │   │   ├── PlayResultDao.kt     # Play result DB operations
│   │   │   │   │   ├── PlayResultRepository.kt # Play result repository
│   │   │   │   │   ├── PlayResultConverters.kt # Type converters
│   │   │   │   │   └── PlayResultStatus.kt  # Result status enum
│   │   │   │   ├── viewmodel/
│   │   │   │   │   ├── ToDoViewModel.kt     # ViewModel for To Do list
│   │   │   │   │   ├── HistoryViewModel.kt  # ViewModel for History
│   │   │   │   │   └── GameDetailViewModel.kt # ViewModel for game details
│   │   │   │   └── ui/theme/
│   │   │   │       ├── Color.kt             # Orange palette (Orange40/80, etc.)
│   │   │   │       ├── Theme.kt             # Material 3 theme setup
│   │   │   │       └── Type.kt              # Typography definitions
│   │   │   ├── res/                         # Android resources
│   │   │   └── AndroidManifest.xml          # Application manifest
│   │   ├── test/                            # Unit tests
│   │   └── androidTest/                     # Instrumented tests
│   ├── build.gradle.kts                     # Module-level build config
│   └── proguard-rules.pro                   # ProGuard/R8 rules
├── gradle/
│   └── libs.versions.toml                   # Version catalog for dependencies
├── build.gradle.kts                         # Project-level build config
├── settings.gradle.kts                      # Project settings & repository config
├── gradle.properties                        # Gradle daemon settings
│   └── android.disallowKotlinSourceSets=false  # Required for KSP
├── local.properties                         # Local SDK path (excluded from git)
└── gradlew / gradlew.bat                    # Gradle wrapper scripts
```

## Building and Running

### Prerequisites
- **Android Studio** (latest recommended)
- **JDK 17+** (for Gradle 9.x)
- Android SDK with API level 36
- Environment variable: `ANDROID_HOME=/home/sb/Android/Sdk`

### Commands

```bash
# Build the project (debug)
./gradlew assembleDebug

# Build and install on connected device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease

# Run all tests
./gradlew test

# Run instrumented tests on connected device/emulator
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Install debug APK on connected device
./gradlew installDebug
```

### Opening in Android Studio
Simply open the project root directory in Android Studio. The IDE will automatically sync Gradle and download dependencies.

## Development Conventions

### Kotlin Code Style
- `kotlin.code.style=official` — follows official Kotlin coding conventions
- Compose-first architecture (no XML layouts)

### Dependency Management
- Uses **Gradle Version Catalog** (`gradle/libs.versions.toml`) for centralized dependency management
- All AndroidX libraries are referenced via the version catalog (e.g., `libs.androidx.core.ktx`)

### Compose Theme Architecture
- **Color.kt** — Orange palette: Orange40/80, AmberGrey40/80, Peach40/80
- **Theme.kt** — Material 3 theme with dynamic colors support (Android 12+)
- **Type.kt** — Typography definitions

### Testing
- **Unit tests:** JUnit 4 (`test/` directory)
- **Instrumented tests:** AndroidX Test + Compose Testing (`androidTest/` directory)
- Test runner: `androidx.test.runner.AndroidJUnitRunner`

### Build Configuration
- **Debug:** Minification disabled, Compose tooling enabled
- **Release:** Minification disabled by default (can be enabled), ProGuard rules included

### Room Database
- Room uses **camelCase** column names (e.g., `gameId`, not `game_id`)
- Migrations use `.addMigrations()` (plural) on Room.databaseBuilder
- KSP annotation processing for Room entities

## Current Application State

The **Tiles** app is a fully functional board game management application with:
- Game list management (add, edit title, delete games)
- Play result tracking (win/loss/draw/unfinished)
- "To Do" list showing games without play results
- History view showing games with play results
- Material 3 theming with orange color palette (theme name: `TilesTheme`)
- Edge-to-edge display with dynamic color support (Android 12+)
- Room database for local persistence
- MVVM architecture with StateFlow and Compose UI
- All UI strings externalized to `strings.xml` for proper localization support

## Git & Repository

- **Repository:** https://github.com/barser/tiles
- **Branch:** main
- **Authentication:** SSH (ed25519 key)
- **Git identity:** "Serg Bara" <leto82@gmail.com>

## Useful Links

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material 3 for Android](https://m3.material.io/)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Gradle Version Catalog](https://docs.gradle.org/current/userguide/platforms.html)
- [Android Developer Guides](https://developer.android.com/guide)
