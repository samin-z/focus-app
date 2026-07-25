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
import java.util.Collections
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
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
        assertTrue(stopped.duration != null)

        val history = service.getHistory()
        assertEquals(1, history.content.size)
        assertEquals(1, history.totalElements)
        assertEquals(stopped.id, history.content[0].id)
        assertEquals("work", history.content[0].subject)
    }

    @Test
    fun `getHistory includes active and stopped sessions`() {
        val active = service.startSession("active")
        val toStop = service.startSession("done")
        Thread.sleep(20)
        service.stopSession(toStop.id)

        val history = service.getHistory()
        assertEquals(2, history.content.size)
        assertEquals(2, history.totalElements)
        assertEquals(toStop.id, history.content[0].id)
        assertEquals(FocusSessionStatus.STOPPED, history.content[0].status)
        assertEquals(active.id, history.content[1].id)
        assertEquals(FocusSessionStatus.ACTIVE, history.content[1].status)
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
        assertEquals(2, history.content.size)
        assertEquals(second.id, history.content[0].id)
        assertEquals(first.id, history.content[1].id)
        assertEquals("second", history.content[0].subject)
    }

    @Test
    fun `getHistory paginates results`() {
        service.startSession("a")
        service.startSession("b")
        service.startSession("c")

        val page0 = service.getHistory(page = 0, size = 2)
        assertEquals(2, page0.content.size)
        assertEquals(3, page0.totalElements)
        assertEquals(2, page0.totalPages)
        assertEquals(0, page0.number)

        val page1 = service.getHistory(page = 1, size = 2)
        assertEquals(1, page1.content.size)
        assertEquals(1, page1.number)
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

    @Test
    fun `concurrent stopSession allows only one success`() {
        val started = service.startSession("race")
        val executor = Executors.newFixedThreadPool(2)
        val barrier = CyclicBarrier(2)
        val results = Collections.synchronizedList(mutableListOf<Result<FocusSession>>())

        try {
            val futures = List(2) {
                executor.submit {
                    barrier.await()
                    results += runCatching { service.stopSession(started.id) }
                }
            }
            futures.forEach { it.get(5, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
        }

        assertEquals(1, results.count { it.isSuccess })
        assertEquals(1, results.count { it.isFailure })
        assertTrue(results.single { it.isFailure }.exceptionOrNull() is IllegalStateException)

        val stored = focusSessionRepository.findById(started.id).orElseThrow()
        assertEquals(FocusSessionStatus.STOPPED, stored.status)
        assertTrue(stored.endTime != null)
    }
}
