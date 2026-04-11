package com.saminz.focus.focus

import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class FocusSessionService {

    private val idGenerator = AtomicLong(1)
    private val sessions = ConcurrentHashMap<Long, FocusSession>()

    fun startSession(subject: String): FocusSession {
        val cleanedSubject = subject.trim()
        require(cleanedSubject.isNotEmpty()) { "subject can not be empty" }

        val session = FocusSession(
            id = idGenerator.getAndIncrement(),
            subject = cleanedSubject,
            startTime = Instant.now(),
            endTime = null,
            durationSeconds = null,
            status = FocusSessionStatus.ACTIVE,
        )

        sessions[session.id] = session
        return session
    }

    fun stopSession(id: Long): FocusSession {
        val existingSession = sessions[id] ?: throw NoSuchElementException("focus session not found")
        if (existingSession.status == FocusSessionStatus.STOPPED) {
            throw IllegalStateException("focus session already stopped")
        }

        val endTime = Instant.now()
        val durationSeconds = ChronoUnit.SECONDS.between(existingSession.startTime, endTime).coerceAtLeast(0)

        val stoppedSession = existingSession.copy(
            endTime = endTime,
            durationSeconds = durationSeconds,
            status = FocusSessionStatus.STOPPED,
        )

        sessions[id] = stoppedSession
        return stoppedSession
    }

    fun getHistory(): List<FocusSession> {
        return sessions.values
            .filter { it.status == FocusSessionStatus.STOPPED }
            .sortedByDescending { it.endTime }
    }
}
