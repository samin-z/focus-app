package com.saminz.focus

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Request body to start a new focus session")
data class StartFocusSessionRequest(
    @field:NotBlank(message = SubjectValidation.EMPTY_MESSAGE)
    @field:Size(
        max = SubjectValidation.MAX_LENGTH,
        message = "subject must be at most ${SubjectValidation.MAX_LENGTH} characters",
    )
    @field:Schema(description = "What you are focusing on", example = "Reading")
    val subject: String,
)
