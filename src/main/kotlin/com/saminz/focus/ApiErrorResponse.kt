package com.saminz.focus

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Error response body")
data class ApiErrorResponse(
    @field:Schema(description = "Human-readable error message", example = "subject can not be empty")
    val error: String,
)
