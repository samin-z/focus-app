package com.saminz.focus

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration
import java.time.Instant

@WebFluxTest(controllers = [FocusController::class])
class FocusControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var focusSessionService: FocusSessionService

    private val startTime: Instant = Instant.parse("2026-07-16T10:00:00Z")

    @Test
    fun `POST start returns 201 and maps session to response`() {
        given(focusSessionService.startSession("Reading")).willReturn(
            FocusSession(
                id = 1L,
                subject = "Reading",
                startTime = startTime,
                endTime = null,
                duration = null,
                status = FocusSessionStatus.ACTIVE,
            ),
        )

        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(StartFocusSessionRequest("Reading"))
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.subject").isEqualTo("Reading")
            .jsonPath("$.status").isEqualTo("ACTIVE")
            .jsonPath("$.durationSeconds").isEmpty
    }

    @Test
    fun `POST start with blank subject returns 400`() {
        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"subject":"   "}""")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.error").isEqualTo("subject can not be empty")
    }

    @Test
    fun `POST stop returns 200 and stopped session`() {
        given(focusSessionService.stopSession(1L)).willReturn(
            FocusSession(
                id = 1L,
                subject = "Reading",
                startTime = startTime,
                endTime = startTime.plusSeconds(30),
                duration = Duration.ofSeconds(30),
                status = FocusSessionStatus.STOPPED,
            ),
        )

        webTestClient
            .post()
            .uri("/focus/1/stop")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.status").isEqualTo("STOPPED")
            .jsonPath("$.durationSeconds").isEqualTo(30)
    }

    @Test
    fun `POST stop returns 404 when session missing`() {
        given(focusSessionService.stopSession(999L))
            .willThrow(NoSuchElementException("focus session not found"))

        webTestClient
            .post()
            .uri("/focus/999/stop")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.error").isEqualTo("focus session not found")
    }

    @Test
    fun `POST stop returns 409 when already stopped`() {
        given(focusSessionService.stopSession(1L))
            .willThrow(IllegalStateException("focus session already stopped"))

        webTestClient
            .post()
            .uri("/focus/1/stop")
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.error").isEqualTo("focus session already stopped")
    }

    @Test
    fun `GET history returns paged response`() {
        val session = FocusSession(
            id = 1L,
            subject = "Reading",
            startTime = startTime,
            endTime = null,
            duration = null,
            status = FocusSessionStatus.ACTIVE,
        )
        given(focusSessionService.getHistory(1, 5)).willReturn(
            PageImpl(listOf(session), PageRequest.of(1, 5), 6),
        )

        webTestClient
            .get()
            .uri("/focus/history?page=1&size=5")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content[0].subject").isEqualTo("Reading")
            .jsonPath("$.page").isEqualTo(1)
            .jsonPath("$.size").isEqualTo(5)
            .jsonPath("$.totalElements").isEqualTo(6)
            .jsonPath("$.totalPages").isEqualTo(2)

        verify(focusSessionService).getHistory(1, 5)
    }

    @Test
    fun `GET history uses default page when params omitted`() {
        given(focusSessionService.getHistory(0, null)).willReturn(
            PageImpl(emptyList()),
        )

        webTestClient
            .get()
            .uri("/focus/history")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content").isArray
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.totalElements").isEqualTo(0)

        verify(focusSessionService).getHistory(0, null)
    }
}
