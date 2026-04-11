package com.saminz.focus.focus

import java.time.Instant

data class FocusSessionResponse(
    val id: Long,
    val subject: String,
    val startTime: Instant,
    val endTime: Instant?,
    val durationSeconds: Long?,
    val status: String,
)
