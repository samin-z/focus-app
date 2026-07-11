package com.saminz.focus

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "focus")
@Validated
data class FocusProperties(
    val session: Session = Session(),
    val history: History = History(),
) {
    data class Session(
        @field:Min(1)
        @field:Max(500)
        val subjectMaxLength: Int = 255,
    )

    data class History(
        @field:Min(1)
        val maxResults: Int = 1000,
    )
}
