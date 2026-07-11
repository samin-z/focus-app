package com.saminz.focus

import org.springframework.data.domain.Page

interface FocusSessionService {
    fun startSession(subject: String): FocusSession
    fun stopSession(id: Long): FocusSession
    fun getHistory(page: Int = 0, size: Int? = null): Page<FocusSession>
}
