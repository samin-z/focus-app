package com.saminz.focus

import com.saminz.focus.FocusSessionRepository
import com.saminz.focus.FocusSessionService
import com.saminz.focus.FocusSessionStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import kotlin.test.assertFailsWith

@SpringBootTest
class FocusSessionServiceTest {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun postgresProperties(registry: DynamicPropertyRegistry) {
            PostgresTestContainer.register(registry)
        }
    }


    @Autowired
    private lateinit var service: FocusSessionService
    @Autowired
    private lateinit var focusSessionRepository: FocusSessionRepository
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun resetState() {
        FocusSessionDatabaseTestSupport.reset(focusSessionRepository, jdbcTemplate)
    }

    @Test
    // this function name is better in test report
    fun `startSession trims subject`() {
        val session = service.startSession("  reading  ")
        assertEquals("reading", session.subject)
        assertEquals(FocusSessionStatus.ACTIVE, session.status)
        assertEquals(1L, session.id)
    }

    @Test
    fun `startSession rejects blank subject`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            service.startSession("   ")
        }
        assertEquals("subject can not be empty", ex.message)
    }

    @Test
    fun `stopSession updates stored session and history`() {
        val started = service.startSession("work")
        val stopped = service.stopSession(started.id)

        assertEquals(FocusSessionStatus.STOPPED, stopped.status)
        assertTrue(stopped.endTime != null)
        assertTrue(stopped.durationSeconds != null)

        val history = service.getHistory()
        assertEquals(1, history.size)
        assertEquals(stopped.id, history[0].id)
        assertEquals("work", history[0].subject)
    }

    @Test
    fun `getHistory includes active and stopped sessions`() {
        val active = service.startSession("active")
        val toStop = service.startSession("done")
        Thread.sleep(20)
        service.stopSession(toStop.id)

        val history = service.getHistory()
        assertEquals(2, history.size)
        assertEquals(toStop.id, history[0].id)
        assertEquals(FocusSessionStatus.STOPPED, history[0].status)
        assertEquals(active.id, history[1].id)
        assertEquals(FocusSessionStatus.ACTIVE, history[1].status)
    }

    @Test
    fun `getHistory orders stopped sessions by end time descending`() {
        val first = service.startSession("first")
        val second = service.startSession("second")
        Thread.sleep(20)
        service.stopSession(first.id)
        Thread.sleep(20)
        service.stopSession(second.id)

        val history = service.getHistory()
        assertEquals(2, history.size)
        assertEquals(second.id, history[0].id)
        assertEquals(first.id, history[1].id)
        assertEquals("second", history[0].subject)
    }

    @Test
    fun `stopSession fails when id missing`() {
        val ex = assertFailsWith<NoSuchElementException> {
            service.stopSession(999)
        }
        assertEquals("focus session not found", ex.message)
    }

    @Test
    fun `stopSession fails when already stopped`() {
        val started = service.startSession("x")
        service.stopSession(started.id)
        assertFailsWith<IllegalStateException> {
            service.stopSession(started.id)
        }
    }
}
