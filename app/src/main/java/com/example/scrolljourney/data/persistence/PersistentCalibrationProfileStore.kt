package com.example.scrolljourney.data.persistence

import com.example.scrolljourney.domain.distance.CalibrationProfile
import com.example.scrolljourney.domain.distance.CalibrationProfileProvider
import com.example.scrolljourney.domain.distance.CalibrationProfileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Reads/writes the single active [CalibrationProfile] as one small JSON file in app-private
 * storage. Unlike [ScrollDataStore], this holds no in-memory cache — reads and writes are rare
 * (once per completed calibration run), so each call just goes straight to disk, and multiple
 * instances pointed at the same file behave correctly with no singleton needed.
 */
class PersistentCalibrationProfileStore(
    private val file: File,
) : CalibrationProfileStore, CalibrationProfileProvider {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun activeProfile(): CalibrationProfile? = withContext(Dispatchers.IO) {
        runCatching { json.decodeFromString<CalibrationProfile>(file.readText()) }.getOrNull()
    }

    override suspend fun saveProfile(profile: CalibrationProfile) = withContext(Dispatchers.IO) {
        // Write to a temp file then atomically move it into place, so a process death mid-write
        // can't leave a half-written, unparseable file behind. Files.move with REPLACE_EXISTING
        // is used instead of File.renameTo, which silently fails on Windows when the destination
        // already exists (true for every save after the first).
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(json.encodeToString(profile))
        Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        Unit
    }
}
