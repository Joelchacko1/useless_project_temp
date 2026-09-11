package com.example.scrolljourney.domain.data

interface ScrollEventSink {
    suspend fun recordScroll(event: ProcessedScroll)
}
