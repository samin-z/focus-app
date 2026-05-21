package com.saminz.focus

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Focus session returned by the API")
data class FocusSessionResponse(
    @field:Schema(description = "Session id", example = "1")
    val id: Long,
    @field:Schema(description = "Session subject", example = "Reading")
    val subject: String,
    @field:Schema(description = "When the session started", example = "2026-05-16T10:00:00Z")
    val startTime: Instant,
    @field:Schema(description = "When the session ended; null while active")
    val endTime: Instant?,
    @field:Schema(description = "Duration in seconds; null while active")
    val durationSeconds: Long?,
    @field:Schema(description = "Session status", example = "ACTIVE", allowableValues = ["ACTIVE", "STOPPED"])
    val status: String,
)
