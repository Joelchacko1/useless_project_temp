package com.example.scrolljourney.integration

import com.example.scrolljourney.domain.data.AggregatedStats as DataAggregatedStats
import com.example.scrolljourney.domain.data.AppDistanceStat
import com.example.scrolljourney.domain.data.ScrollStatsRepository as DataScrollStatsRepository
import com.example.scrolljourney.domain.repository.ScrollStatsRepository as UiScrollStatsRepository
import com.example.scrolljourney.domain.ui.AggregatedStats as UiAggregatedStats
import com.example.scrolljourney.domain.ui.AppMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScrollStatsRepositoryBridge(
    private val dataRepo: DataScrollStatsRepository,
) : UiScrollStatsRepository {

    override fun observeToday(): Flow<UiAggregatedStats> =
        dataRepo.observeToday().map { it.toUi() }

    override fun observeWeek(): Flow<UiAggregatedStats> =
        dataRepo.observeWeek().map { it.toUi() }

    override fun observeMonth(): Flow<UiAggregatedStats> =
        dataRepo.observeMonth().map { it.toUi() }

    override fun observeAppBreakdown(dateKey: String): Flow<List<AppMetric>> =
        dataRepo.observeAppBreakdown(dateKey).map { stats ->
            stats.map { it.toAppMetric() }
        }

    private fun DataAggregatedStats.toUi(): UiAggregatedStats = UiAggregatedStats(
        totalScrolls = totalScrolls,
        totalDistanceMeters = totalDistanceMeters,
        activeTrackingMs = activeTrackingMs,
        dateKey = dateKey,
        topApps = topApps.map { it.toAppMetric() },
    )

    private fun AppDistanceStat.toAppMetric(): AppMetric = AppMetric(
        packageName = packageName,
        appName = packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() },
        distanceMeters = distanceMeters,
        scrollCount = scrollCount,
        iconUrl = null,
    )
}
