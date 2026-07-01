package com.saminz.focus

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
@Transactional
class FocusSessionServiceImpl(
    private val focusSessionRepository: FocusSessionRepository,
) : FocusSessionService {

    override fun startSession(subject: String): FocusSession {
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

    override fun stopSession(id: Long): FocusSession {
        val existingSession = focusSessionRepository.findById(id).orElseThrow {
            NoSuchElementException("focus session not found")
        }
        check(existingSession.status != FocusSessionStatus.STOPPED) {
            "focus session already stopped"
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
    override fun getHistory(): List<FocusSession> {
        return focusSessionRepository.findAll()
            .map { it.toModel() }
            .sortedByDescending { it.endTime ?: it.startTime }
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
