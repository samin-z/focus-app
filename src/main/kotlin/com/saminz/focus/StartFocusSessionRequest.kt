package com.saminz.focus

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request body to start a new focus session")
data class StartFocusSessionRequest(
    @field:Schema(description = "What you are focusing on", example = "Reading")
    val subject: String,
)
