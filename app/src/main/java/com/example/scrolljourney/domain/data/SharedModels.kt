package com.example.scrolljourney.domain.data

typealias ProcessedScroll = com.example.scrolljourney.domain.tracking.ProcessedScroll
typealias ScrollDirection = com.example.scrolljourney.domain.tracking.ScrollDirection
typealias EstimationMethod = com.example.scrolljourney.domain.distance.EstimationMethod
typealias CalibrationProfile = com.example.scrolljourney.domain.distance.CalibrationProfile

data class AppDistanceStat(
    val packageName: String,
    val distanceMeters: Double,
    val scrollCount: Long,
)

data class AggregatedStats(
    val dateKey: String,
    val totalScrolls: Long,
    val totalDistanceMeters: Double,
    val activeTrackingMs: Long,
    val topApps: List<AppDistanceStat>,
)
