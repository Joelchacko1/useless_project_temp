package com.example.scrolljourney.data.persistence

import com.example.scrolljourney.domain.distance.CalibrationProfile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class PersistentCalibrationProfileStoreTest {

    private val tempFiles = mutableListOf<File>()

    @After
    fun cleanUp() {
        tempFiles.forEach { it.delete() }
    }

    private fun newTempFile(): File =
        File.createTempFile("calibration_profile_test", ".json").also { tempFiles.add(it) }

    @Test
    fun testSaveThenLoadRoundTrips() = runBlocking {
        val store = PersistentCalibrationProfileStore(newTempFile())
        val profile = CalibrationProfile(
            id = "calibration-1",
            createdAtEpochMs = 1_000L,
            sampleCount = 20,
            medianDistanceMeters = 0.18,
            meanDistanceMeters = 0.19,
            lastUpdatedEpochMs = 1_000L,
        )

        store.saveProfile(profile)

        assertEquals(profile, store.activeProfile())
    }

    @Test
    fun testLaterSaveReplacesEarlierProfile() = runBlocking {
        val file = newTempFile()
        val store = PersistentCalibrationProfileStore(file)

        store.saveProfile(
            CalibrationProfile(
                id = "first",
                createdAtEpochMs = 1_000L,
                sampleCount = 20,
                medianDistanceMeters = 0.10,
                meanDistanceMeters = 0.10,
                lastUpdatedEpochMs = 1_000L,
            ),
        )
        store.saveProfile(
            CalibrationProfile(
                id = "second",
                createdAtEpochMs = 2_000L,
                sampleCount = 20,
                medianDistanceMeters = 0.25,
                meanDistanceMeters = 0.26,
                lastUpdatedEpochMs = 2_000L,
            ),
        )

        // A second PersistentCalibrationProfileStore instance pointed at the same file must see
        // the replacement, not the first save — this store holds no in-memory cache.
        val reopened = PersistentCalibrationProfileStore(file)
        assertEquals("second", reopened.activeProfile()?.id)
    }

    @Test
    fun testMissingFileLoadsNull() = runBlocking {
        val missingFile = File.createTempFile("calibration_profile_test_missing", ".json")
        tempFiles.add(missingFile)
        missingFile.delete()

        assertNull(PersistentCalibrationProfileStore(missingFile).activeProfile())
    }
}
