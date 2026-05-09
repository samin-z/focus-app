package com.saminz.focus

import com.saminz.focus.focus.FocusSessionRepository
import com.saminz.focus.focus.FocusSessionService
import com.saminz.focus.focus.FocusSessionStatus
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
        focusSessionRepository.deleteAll()
        jdbcTemplate.execute("ALTER SEQUENCE focus_sessions_id_seq RESTART WITH 1")
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
    fun `getHistory excludes active sessions and orders by end time descending`() {
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
