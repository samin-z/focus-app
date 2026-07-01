package com.saminz.focus

interface FocusSessionService {
    fun startSession(subject: String): FocusSession
    fun stopSession(id: Long): FocusSession
    fun getHistory(): List<FocusSession>
}
