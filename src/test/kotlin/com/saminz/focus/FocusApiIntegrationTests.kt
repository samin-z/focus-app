package com.saminz.focus

import com.saminz.focus.focus.FocusSessionService
import com.saminz.focus.focus.StartFocusSessionRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@AutoConfigureWebTestClient
class FocusApiIntegrationTests {

    @Autowired private lateinit var webTestClient: WebTestClient

    // builds requests like a real caller would: choose method (GET/POST/…), path, headers, JSON body
    @Autowired private lateinit var focusSessionService: FocusSessionService

    @BeforeEach
    fun resetService() {
        focusSessionService.resetStateForTests()
    }

    // exchange actually fire the request and get a response
    @Test
    fun `POST start returns 201 and ACTIVE session`() {
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
            .jsonPath("$.endTime").isEmpty
            .jsonPath("$.durationSeconds").isEmpty
    }

    @Test
    fun `POST start blank subject returns 400 with ApiErrorResponse`() {
        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(StartFocusSessionRequest("   "))

            .exchange()

            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.error").isEqualTo("subject can not be empty")
    }

    @Test
    fun `POST stop then GET history returns stopped session`() {
        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(StartFocusSessionRequest("work"))

            .exchange()

            .expectStatus().isCreated

        webTestClient
            .post()
            .uri("/focus/1/stop")

            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("STOPPED")
            .jsonPath("$.endTime").exists()
            .jsonPath("$.durationSeconds").exists()

        webTestClient
            .get()
            .uri("/focus/history")
            
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].subject").isEqualTo("work")
            .jsonPath("$[0].status").isEqualTo("STOPPED")
            .jsonPath("$[1]").doesNotExist()
    }

    @Test
    fun `POST stop unknown id returns 404`() {
        webTestClient
            .post()
            .uri("/focus/404/stop")

            .exchange()

            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.error").isEqualTo("focus session not found")
    }

    @Test
    fun `POST stop when already stopped returns 409`() {
        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(StartFocusSessionRequest("x"))
            
            .exchange()
            
            .expectStatus().isCreated

        webTestClient
            .post()
            .uri("/focus/1/stop")
            
            .exchange()
            
            .expectStatus().isOk

        webTestClient
            .post()
            .uri("/focus/1/stop")
            
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody()
            .jsonPath("$.error").isEqualTo("focus session already stopped")
    }
}
