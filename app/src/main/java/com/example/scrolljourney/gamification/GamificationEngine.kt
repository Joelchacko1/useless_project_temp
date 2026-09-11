package com.example.scrolljourney.gamification

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

object GamificationEngine {
    const val XP_PER_METER = 0.1 // 100 XP per 1 km
    
    val allAchievements = listOf(
        Achievement("ACH_0_2KM", "🏆 First Roast", "Thondi thondi irikkathe vello panikkum pokkude? 😂", 200.0, false),
        Achievement("ACH_0_5KM", "🏆 Thumb Starter", "Enthada mone, thumb kond marathon aano?", 500.0, false),
        Achievement("ACH_1KM", "🏆 Thumb Walker", "1 KM aayi... nee nadannirunnenkil fitness aayene! 💀", 1000.0, false),
        Achievement("ACH_2KM", "🏆 Professional Scroller", "2 KM! Phone alla, treadmill aanu use cheyyunne ennu thonnunnu.", 2000.0, false),
        Achievement("ACH_5KM", "🏆 Thumb Athlete", "5 KM scroll cheythu... ninte thumb aanu veetile main athlete. 😂", 5000.0, false),
        Achievement("ACH_10KM", "🏆 Scroll Monster", "10 KM! Purathottu nokkeda mone, lokam avideyum undu. 💀", 10000.0, false),
        Achievement("ACH_25KM", "🏆 Touch Grass", "25 KM!!! Phone vechittu purathottu onnu poda. ☠️", 25000.0, false),
        Achievement("ACH_50KM", "🏆 Thumb Legend", "50 KM... ninte thumbinu ippo retirement venam.", 50000.0, false)
    )

    fun calculateXp(distanceMeters: Double): Long {
        return (distanceMeters * XP_PER_METER).toLong()
    }

    fun calculateLevel(totalXp: Long): Int {
        // Level 1: 0-99 XP
        // Level 2: 100-299 XP
        // Level n: (n-1)^2 * 100 XP
        if (totalXp <= 0) return 1
        return (Math.sqrt((totalXp / 100.0)) + 1).toInt()
    }

    fun evaluateAchievements(totalDistanceMeters: Double, unlockedIds: List<String>): List<Achievement> {
        return allAchievements.map { achievement ->
            val previouslyUnlocked = unlockedIds.contains(achievement.id)
            val newlyUnlocked = totalDistanceMeters >= achievement.targetDistanceMeters
            achievement.copy(isUnlocked = previouslyUnlocked || newlyUnlocked)
        }
    }

    fun updateStreak(currentStreak: Int, lastActiveDateKey: String?, todayDateKey: String): Int {
        if (lastActiveDateKey == null) return 1
        
        return try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val lastActiveDate = LocalDate.parse(lastActiveDateKey, formatter)
            val todayDate = LocalDate.parse(todayDateKey, formatter)

            val daysBetween = ChronoUnit.DAYS.between(lastActiveDate, todayDate)

            when {
                daysBetween == 0L -> currentStreak // Same day, streak doesn't change
                daysBetween == 1L -> currentStreak + 1 // Next day, increment streak
                daysBetween > 1L -> 1 // Missed a day, reset streak
                else -> currentStreak // Negative days (time travel?), keep current streak
            }
        } catch (e: DateTimeParseException) {
            1 // Reset streak if date key is invalid
        }
    }
}
