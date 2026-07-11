package com.saminz.focus

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Request body to start a new focus session")
data class StartFocusSessionRequest(
    @field:NotBlank(message = "subject can not be empty")
    @field:Size(max = 255, message = "subject must be at most 255 characters")
    @field:Schema(description = "What you are focusing on", example = "Reading")
    val subject: String,
)
