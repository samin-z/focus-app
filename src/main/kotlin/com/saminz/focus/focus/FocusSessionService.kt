package com.saminz.focus.focus

import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

@Service
class FocusSessionService {

    private val idGenerator = AtomicLong(1)

    fun startSession(subject: String): FocusSession {
        val cleanedSubject = subject.trim()
        require(cleanedSubject.isNotEmpty()) { "subject can not be empty" }

        return FocusSession(
            id = idGenerator.getAndIncrement(),
            subject = cleanedSubject,
            startTime = Instant.now(),
            status = FocusSessionStatus.ACTIVE,
        )
    }
}
