package com.saminz.focus

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant

@Entity
@Table(name = "focus_sessions")
class FocusSessionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val subject: String,
    val startTime: Instant,
    var endTime: Instant? = null,
    var durationSeconds: Long? = null,
    @Enumerated(EnumType.STRING)
    var status: FocusSessionStatus = FocusSessionStatus.ACTIVE,
    @Version
    var version: Long = 0,
)
