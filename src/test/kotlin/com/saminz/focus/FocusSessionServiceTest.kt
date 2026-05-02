package com.saminz.focus

import com.saminz.focus.focus.FocusSessionService
import com.saminz.focus.focus.FocusSessionStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class FocusSessionServiceTest {

    // Tests often use it because frameworks run setup methods (@BeforeEach) after the test object exists, so “create the service here” fits lateinit + assign in setup.
    private lateinit var service: FocusSessionService

    // its like setUp in phpunit
    @BeforeEach
    fun setup() {
        service = FocusSessionService()
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
