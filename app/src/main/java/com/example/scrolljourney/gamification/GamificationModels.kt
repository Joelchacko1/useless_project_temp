package com.example.scrolljourney.gamification

data class GamificationState(
    val totalXp: Long,
    val currentLevel: Int,
    val currentStreakDays: Int,
    val lastActiveDateKey: String?,
    val unlockedAchievements: List<String>
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val targetDistanceMeters: Double,
    val isUnlocked: Boolean
)
