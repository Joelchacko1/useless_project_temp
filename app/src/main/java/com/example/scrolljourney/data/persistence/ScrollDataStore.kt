package com.example.scrolljourney.data.persistence

import com.example.scrolljourney.domain.tracking.ProcessedScroll
import com.example.scrolljourney.gamification.GamificationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Serializable
data class PersistedScrollData(
    val events: List<ProcessedScroll> = emptyList(),
    val gamificationState: GamificationState = GamificationState(
        totalXp = 0L,
        currentLevel = 1,
        currentStreakDays = 0,
        lastActiveDateKey = null,
        unlockedAchievements = emptyList(),
    ),
)

/**
 * Reads/writes the app's entire tracked history as one JSON file in app-private storage.
 * Tolerant of a missing or corrupt file — callers get an empty [PersistedScrollData] rather
 * than an exception, since losing this file should never crash the app.
 */
class ScrollDataStore(private val file: File) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(): PersistedScrollData = withContext(Dispatchers.IO) {
        runCatching { json.decodeFromString<PersistedScrollData>(file.readText()) }
            .getOrDefault(PersistedScrollData())
    }

    suspend fun save(data: PersistedScrollData) = withContext(Dispatchers.IO) {
        // Write to a temp file then atomically move it into place, so a process death mid-write
        // can't leave a half-written, unparseable file behind. Files.move with REPLACE_EXISTING
        // is used instead of File.renameTo, which silently fails on Windows when the destination
        // already exists (true for every save after the first).
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(json.encodeToString(data))
        Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        Unit
    }
}
