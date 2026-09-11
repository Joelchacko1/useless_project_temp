# Handoff: Agent 3 (Data, Analytics, Gamification)

## Status
- **Domain Layer:** Shared contracts (`ScrollEventSink`, `ScrollStatsRepository`, models) have been created in `com.example.scrolljourney.domain.data`.
- **Gamification Layer:** `GamificationEngine` and `GamificationModels` created with complete unit test coverage.
- **Data Layer:** An `InMemoryScrollRepository` has been implemented to keep the branch fully buildable and testable without the missing Room dependencies.

## Instructions for Agent 4 (Integration)

As per the master specification, Agent 3 cannot modify `build.gradle.kts`. You must wire the actual Room persistence layer during integration.

### 1. Add Room Dependencies
Add these to `gradle/libs.versions.toml`:
```toml
[versions]
room = "2.6.1"
ksp = "2.0.0-1.0.24" # Match your Kotlin version

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

Apply in `app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.ksp)
}
dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
```

### 2. Room Implementation

Create the following Room implementation to replace `InMemoryScrollRepository`.

#### `ScrollEventEntity.kt`
```kotlin
@Entity(tableName = "scroll_events")
data class ScrollEventEntity(
    @PrimaryKey val id: String,
    val timestampEpochMs: Long,
    val packageName: String,
    val distanceMeters: Double
)
```

#### `ScrollEventDao.kt`
```kotlin
@Dao
interface ScrollEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ScrollEventEntity)

    @Query("SELECT * FROM scroll_events")
    fun observeAll(): Flow<List<ScrollEventEntity>>
}
```

#### `ScrollDatabase.kt`
```kotlin
@Database(entities = [ScrollEventEntity::class], version = 1, exportSchema = false)
abstract class ScrollDatabase : RoomDatabase() {
    abstract fun scrollEventDao(): ScrollEventDao
}
```

#### Replace the In-Memory Repository
Update the DI graph/wiring in `MainActivity` or Application class to inject a Room-backed repository instead of `InMemoryScrollRepository`. The Room-backed repository should use the DAOs and apply the exact same gamification update logic provided in `InMemoryScrollRepository.updateGamification`.

### 3. Gamification State Persistence
The current `InMemoryScrollRepository` holds `gamificationState` in memory. In your final Room implementation, persist this state using `DataStore<Preferences>` or `SharedPreferences`, since it's a simple key-value model.
