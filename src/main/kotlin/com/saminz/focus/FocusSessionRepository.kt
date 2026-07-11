package com.saminz.focus

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FocusSessionRepository : JpaRepository<FocusSessionEntity, Long> {
    @Query(
        """
        SELECT f FROM FocusSessionEntity f
        ORDER BY COALESCE(f.endTime, f.startTime) DESC
        """,
    )
    fun findAllOrderByRecentActivity(pageable: Pageable): Page<FocusSessionEntity>
}
