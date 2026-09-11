package com.example.scrolljourney.domain.data

import kotlinx.coroutines.flow.Flow

interface ScrollStatsRepository {
    fun observeToday(): Flow<AggregatedStats>
    fun observeWeek(): Flow<AggregatedStats>
    fun observeMonth(): Flow<AggregatedStats>
    fun observeAppBreakdown(dateKey: String): Flow<List<AppDistanceStat>>
}
