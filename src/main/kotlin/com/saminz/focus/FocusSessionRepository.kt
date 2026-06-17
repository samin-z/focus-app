package com.saminz.focus

import org.springframework.data.jpa.repository.JpaRepository

interface FocusSessionRepository : JpaRepository<FocusSessionEntity, Long>
