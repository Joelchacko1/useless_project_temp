package com.example.scrolljourney.integration

import com.example.scrolljourney.data.repository.InMemoryScrollRepository
import com.example.scrolljourney.domain.repository.AchievementData
import com.example.scrolljourney.domain.repository.GamificationRepository
import com.example.scrolljourney.gamification.GamificationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.sqrt

class GamificationRepositoryBridge(
    private val repository: InMemoryScrollRepository,
) : GamificationRepository {

    override fun observeCurrentXpAndLevel(): Flow<Pair<Long, Int>> =
        repository.gamificationState.map { Pair(it.totalXp, it.currentLevel) }

    override fun observeCurrentStreak(): Flow<Int> =
        repository.gamificationState.map { it.currentStreakDays }

    override fun observeLongestStreak(): Flow<Int> =
        repository.gamificationState.map { it.currentStreakDays }

    override fun observeAchievements(): Flow<List<AchievementData>> =
        repository.gamificationState.map { state ->
            GamificationEngine.allAchievements.map { achievement ->
                val unlocked = state.unlockedAchievements.contains(achievement.id)
                AchievementData(
                    id = achievement.id,
                    title = achievement.title,
                    description = achievement.description,
                    isUnlocked = unlocked,
                    xpReward = GamificationEngine.calculateXp(achievement.targetDistanceMeters),
                    progress = if (unlocked) 100 else 0,
                    progressMax = 100,
                )
            }
        }

    override fun observeXpToNextLevel(): Flow<Long> =
        repository.gamificationState.map { state ->
            val currentLevel = state.currentLevel
            val nextLevelXp = (currentLevel.toLong() * currentLevel.toLong()) * 100L
            (nextLevelXp - state.totalXp).coerceAtLeast(0L)
        }
}
