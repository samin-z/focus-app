package com.saminz.focus.focus

import java.time.Instant

enum class FocusSessionStatus {
    ACTIVE,
    STOPPED,
}

data class FocusSession(
    val id: Long,
    val subject: String,
    val startTime: Instant,
    val endTime: Instant?,
    val durationSeconds: Long?,
    val status: FocusSessionStatus,
)
