package com.saminz.focus

import java.time.Duration
import java.time.Instant

data class FocusSession(
    val id: Long,
    val subject: String,
    val startTime: Instant,
    val endTime: Instant?,
    val duration: Duration?,
    val status: FocusSessionStatus,
)
