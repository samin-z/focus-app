package com.saminz.focus.focus

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
@Transactional
class FocusSessionService(
    private val focusSessionRepository: FocusSessionRepository,
) {

    fun startSession(subject: String): FocusSession {
        val cleanedSubject = subject.trim()
        require(cleanedSubject.isNotEmpty()) { "subject can not be empty" }

        val session = FocusSessionEntity(
            subject = cleanedSubject,
            startTime = Instant.now(),
            endTime = null,
            durationSeconds = null,
            status = FocusSessionStatus.ACTIVE,
        )

        return focusSessionRepository.save(session).toModel()
    }

    fun stopSession(id: Long): FocusSession {
        val existingSession = focusSessionRepository.findById(id).orElseThrow {
            NoSuchElementException("focus session not found")
        }
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

        return focusSessionRepository.save(stoppedSession).toModel()
    }

    @Transactional(readOnly = true)
    fun getHistory(): List<FocusSession> {
        return focusSessionRepository.findAllByStatusOrderByEndTimeDesc(FocusSessionStatus.STOPPED)
            .map { it.toModel() }
    }

    private fun FocusSessionEntity.toModel(): FocusSession {
        return FocusSession(
            id = requireNotNull(id),
            subject = subject,
            startTime = startTime,
            endTime = endTime,
            durationSeconds = durationSeconds,
            status = status,
        )
    }
}
