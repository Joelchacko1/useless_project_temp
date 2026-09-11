package com.example.scrolljourney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrolljourney.domain.ui.AchievementCard
import com.example.scrolljourney.domain.ui.AchievementsUiState
import com.example.scrolljourney.ui.components.NeoBadge
import com.example.scrolljourney.ui.components.NeoDimens
import com.example.scrolljourney.ui.components.NeoPanel
import com.example.scrolljourney.ui.components.NeoTopBar
import com.example.scrolljourney.ui.theme.LocalScrollJourneyColors
import com.example.scrolljourney.ui.theme.ScrollJourneyTheme

@Composable
fun AchievementsScreen(
    state: AchievementsUiState,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeoTopBar(
            title = "Achievements",
            fillColor = extra.reward,
            contentColor = extra.onReward,
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                AchievementsProgressCard(state)
            }

            itemsIndexed(state.achievements) { index, achievement ->
                AchievementCardComponent(achievement, index)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AchievementsProgressCard(
    state: AchievementsUiState,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Achievements Unlocked",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "${state.totalAchievementsUnlocked} / ${state.achievements.size}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LinearProgressIndicator(
                progress = { state.unlockedPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = extra.reward,
                trackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f)
            )

            Text(
                text = "%.0f%% Complete".format(state.unlockedPercentage),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AchievementCardComponent(
    achievement: AchievementCard,
    index: Int,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    val fills = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        extra.reward,
        MaterialTheme.colorScheme.background,
    )
    val inks = listOf(
        MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.onSecondary,
        MaterialTheme.colorScheme.onTertiary,
        extra.onReward,
        MaterialTheme.colorScheme.onBackground,
    )
    val slot = index % fills.size
    val fill = fills[slot]
    val ink = inks[slot]

    NeoPanel(
        fillColor = fill,
        contentColor = ink,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            val badgeShape = RoundedCornerShape(NeoDimens.CornerRadius)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (achievement.isUnlocked) extra.reward else MaterialTheme.colorScheme.background,
                        shape = badgeShape
                    )
                    .border(NeoDimens.BorderWidthNested, extra.borderInk, badgeShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (achievement.isUnlocked) "✓" else "?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.isUnlocked) extra.onReward else extra.borderInk,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = achievement.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ink
                        )

                        Text(
                            text = achievement.description,
                            fontSize = 12.sp,
                            color = ink.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (achievement.isUnlocked) {
                        NeoBadge(
                            text = "+${achievement.xpReward}",
                            fillColor = extra.reward,
                            contentColor = extra.onReward
                        )
                    }
                }

                if (achievement.progressMax > 0 && !achievement.isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { achievement.progress.toFloat() / achievement.progressMax },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = extra.reward,
                        trackColor = extra.borderInk.copy(alpha = 0.15f)
                    )

                    Text(
                        text = "${achievement.progress}/${achievement.progressMax}",
                        fontSize = 11.sp,
                        color = ink.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (achievement.isUnlocked && achievement.unlockedAtEpochMs != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unlocked",
                        fontSize = 11.sp,
                        color = ink.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AchievementsScreenPreview() {
    ScrollJourneyTheme {
        AchievementsScreen(
            state = AchievementsUiState(
                achievements = listOf(
                    AchievementCard(
                        id = "first_scroll",
                        title = "First Scroll",
                        description = "Record your first scroll",
                        isUnlocked = true,
                        xpReward = 10
                    ),
                    AchievementCard(
                        id = "one_km",
                        title = "One Kilometer",
                        description = "Scroll 1 km in total",
                        isUnlocked = true,
                        xpReward = 50
                    ),
                    AchievementCard(
                        id = "seven_day_streak",
                        title = "Week Warrior",
                        description = "7 day streak",
                        progress = 3,
                        progressMax = 7,
                        isUnlocked = false,
                        xpReward = 100
                    ),
                    AchievementCard(
                        id = "ten_km",
                        title = "Ten Kilometers",
                        description = "Scroll 10 km in total",
                        progress = 5,
                        progressMax = 10,
                        isUnlocked = false,
                        xpReward = 200
                    )
                ),
                totalAchievementsUnlocked = 2,
                unlockedPercentage = 50f
            )
        )
    }
}
