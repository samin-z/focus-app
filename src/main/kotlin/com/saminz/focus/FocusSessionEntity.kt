package com.saminz.focus

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "focus_sessions")
data class FocusSessionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val subject: String,
    val startTime: Instant,
    val endTime: Instant?,
    val durationSeconds: Long?,
    @Enumerated(EnumType.STRING)
    val status: FocusSessionStatus,
)
