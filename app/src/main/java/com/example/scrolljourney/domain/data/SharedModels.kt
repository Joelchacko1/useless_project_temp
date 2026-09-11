package com.example.scrolljourney.domain.data

enum class ScrollDirection { UP, DOWN, LEFT, RIGHT, UNKNOWN }
enum class EstimationMethod { ACTUAL_DELTA, CALIBRATED_ESTIMATE, FALLBACK_ESTIMATE }

data class ProcessedScroll(
    val id: String,
    val timestampEpochMs: Long,
    val packageName: String,
    val direction: ScrollDirection,
    val distanceMeters: Double,
    val confidence: Float,
    val estimationMethod: EstimationMethod
)

data class CalibrationProfile(
    val id: String,
    val createdAtEpochMs: Long,
    val sampleCount: Int,
    val medianDistanceMeters: Double,
    val meanDistanceMeters: Double,
    val lastUpdatedEpochMs: Long
)

data class AppDistanceStat(
    val packageName: String,
    val distanceMeters: Double
)

data class AggregatedStats(
    val dateKey: String,
    val totalScrolls: Long,
    val totalDistanceMeters: Double,
    val activeTrackingMs: Long,
    val topApps: List<AppDistanceStat>
)
