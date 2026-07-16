package com.saminz.focus

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
@Transactional
class FocusSessionServiceImpl(
    private val focusSessionRepository: FocusSessionRepository,
    private val focusProperties: FocusProperties,
) : FocusSessionService {

    override fun startSession(subject: String): FocusSession {
        val cleanedSubject = subject.trim()
        require(cleanedSubject.isNotEmpty()) { "subject can not be empty" }
        require(cleanedSubject.length <= focusProperties.session.subjectMaxLength) {
            "subject must be at most ${focusProperties.session.subjectMaxLength} characters"
        }

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
        val duration = Duration.between(existingSession.startTime, endTime).coerceAtLeast(Duration.ZERO)

        val stoppedSession = existingSession.copy(
            endTime = endTime,
            durationSeconds = duration.toSeconds(),
            status = FocusSessionStatus.STOPPED,
        )

        return focusSessionRepository.save(stoppedSession).toModel()
    }

    @Transactional(readOnly = true)
    override fun getHistory(page: Int, size: Int?): Page<FocusSession> {
        val pageSize = (size ?: focusProperties.history.defaultPageSize)
            .coerceIn(1, focusProperties.history.maxPageSize)
        val pageable = PageRequest.of(page.coerceAtLeast(0), pageSize)
        return focusSessionRepository.findAllOrderByRecentActivity(pageable).map { it.toModel() }
    }

    private fun FocusSessionEntity.toModel(): FocusSession {
        return FocusSession(
            id = requireNotNull(id),
            subject = subject,
            startTime = startTime,
            endTime = endTime,
            duration = durationSeconds?.let { Duration.ofSeconds(it) },
            status = status,
        )
    }
}
