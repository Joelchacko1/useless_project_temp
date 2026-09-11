package com.example.scrolljourney.tracking

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PersistentTrackingStatusControllerTest {

    @Test
    fun testDefaultsToDisabledWhenNothingPersistedYet() {
        val controller = PersistentTrackingStatusController(FakeSharedPreferences())
        assertFalse(controller.trackingState.value.isTrackingEnabled)
    }

    @Test
    fun testTrackingEnabledSurvivesNewControllerAgainstSameBackingPrefs() {
        val prefs = FakeSharedPreferences()
        val controller = PersistentTrackingStatusController(prefs)

        controller.setTrackingEnabled(true)

        // Simulate the app being closed and reopened: a brand new controller instance backed
        // by the same underlying preferences file.
        val reopened = PersistentTrackingStatusController(prefs)
        assertEquals(true, reopened.trackingState.value.isTrackingEnabled)
    }

    @Test
    fun testTrackingDisabledAlsoPersists() {
        val prefs = FakeSharedPreferences()
        val controller = PersistentTrackingStatusController(prefs)
        controller.setTrackingEnabled(true)
        controller.setTrackingEnabled(false)

        val reopened = PersistentTrackingStatusController(prefs)
        assertFalse(reopened.trackingState.value.isTrackingEnabled)
    }

    /** Minimal in-memory fake covering only the members [PersistentTrackingStatusController] uses. */
    private class FakeSharedPreferences : SharedPreferences {
        private val values = mutableMapOf<String, Any?>()

        override fun getBoolean(key: String, defValue: Boolean): Boolean =
            values[key] as? Boolean ?: defValue

        override fun edit(): SharedPreferences.Editor = FakeEditor()

        override fun getAll(): MutableMap<String, *> = throw UnsupportedOperationException()
        override fun getString(key: String, defValue: String?): String? = throw UnsupportedOperationException()
        override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? =
            throw UnsupportedOperationException()
        override fun getInt(key: String, defValue: Int): Int = throw UnsupportedOperationException()
        override fun getLong(key: String, defValue: Long): Long = throw UnsupportedOperationException()
        override fun getFloat(key: String, defValue: Float): Float = throw UnsupportedOperationException()
        override fun contains(key: String): Boolean = values.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener,
        ) = throw UnsupportedOperationException()
        override fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener,
        ) = throw UnsupportedOperationException()

        private inner class FakeEditor : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()

            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor =
                apply { pending[key] = value }

            override fun apply() {
                values.putAll(pending)
            }

            override fun commit(): Boolean {
                values.putAll(pending)
                return true
            }

            override fun putString(key: String, value: String?): SharedPreferences.Editor =
                throw UnsupportedOperationException()
            override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor =
                throw UnsupportedOperationException()
            override fun putInt(key: String, value: Int): SharedPreferences.Editor =
                throw UnsupportedOperationException()
            override fun putLong(key: String, value: Long): SharedPreferences.Editor =
                throw UnsupportedOperationException()
            override fun putFloat(key: String, value: Float): SharedPreferences.Editor =
                throw UnsupportedOperationException()
            override fun remove(key: String): SharedPreferences.Editor = throw UnsupportedOperationException()
            override fun clear(): SharedPreferences.Editor = throw UnsupportedOperationException()
        }
    }
}
