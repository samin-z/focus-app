package com.saminz.focus

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Request body to start a new focus session")
data class StartFocusSessionRequest(
    @field:NotBlank(message = "subject can not be empty")
    @field:Schema(description = "What you are focusing on", example = "Reading")
    val subject: String,
)
