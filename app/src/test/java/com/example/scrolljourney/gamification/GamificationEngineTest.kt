package com.example.scrolljourney.gamification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamificationEngineTest {

    @Test
    fun testCalculateXp() {
        assertEquals(0L, GamificationEngine.calculateXp(0.0))
        assertEquals(100L, GamificationEngine.calculateXp(1000.0))
        assertEquals(250L, GamificationEngine.calculateXp(2500.0))
    }

    @Test
    fun testCalculateLevel() {
        assertEquals(1, GamificationEngine.calculateLevel(0L))
        assertEquals(2, GamificationEngine.calculateLevel(100L))
        assertEquals(3, GamificationEngine.calculateLevel(400L))
        assertEquals(4, GamificationEngine.calculateLevel(900L))
    }

    @Test
    fun testEvaluateAchievements() {
        val initialAchievements = GamificationEngine.evaluateAchievements(500.0, emptyList())
        assertFalse(initialAchievements.first { it.id == "ACH_1KM" }.isUnlocked)

        val unlocked1km = GamificationEngine.evaluateAchievements(1500.0, emptyList())
        assertTrue(unlocked1km.first { it.id == "ACH_1KM" }.isUnlocked)
        assertFalse(unlocked1km.first { it.id == "ACH_5KM" }.isUnlocked)

        val preservedAchievements = GamificationEngine.evaluateAchievements(500.0, listOf("ACH_1KM"))
        assertTrue(preservedAchievements.first { it.id == "ACH_1KM" }.isUnlocked)

        val unlockedFirstRoast = GamificationEngine.evaluateAchievements(200.0, emptyList())
        assertTrue(unlockedFirstRoast.first { it.id == "ACH_0_2KM" }.isUnlocked)
        assertFalse(unlockedFirstRoast.first { it.id == "ACH_0_5KM" }.isUnlocked)
    }

    @Test
    fun testUpdateStreak() {
        assertEquals(1, GamificationEngine.updateStreak(0, null, "2023-10-27"))
        
        assertEquals(5, GamificationEngine.updateStreak(5, "2023-10-27", "2023-10-27"))
        
        assertEquals(6, GamificationEngine.updateStreak(5, "2023-10-26", "2023-10-27"))
        
        assertEquals(1, GamificationEngine.updateStreak(5, "2023-10-25", "2023-10-27"))
        
        // Month boundary
        assertEquals(6, GamificationEngine.updateStreak(5, "2023-10-31", "2023-11-01"))
        
        // Leap year
        assertEquals(6, GamificationEngine.updateStreak(5, "2024-02-29", "2024-03-01"))
    }
}
