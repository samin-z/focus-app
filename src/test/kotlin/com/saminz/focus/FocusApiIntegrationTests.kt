package com.saminz.focus

import com.saminz.focus.FocusSessionRepository
import com.saminz.focus.StartFocusSessionRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
@AutoConfigureWebTestClient
class FocusApiIntegrationTests {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun postgresProperties(registry: DynamicPropertyRegistry) {
            PostgresTestContainer.register(registry)
        }
    }


    @Autowired private lateinit var webTestClient: WebTestClient

    @Autowired private lateinit var focusSessionRepository: FocusSessionRepository
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun resetService() {
        FocusSessionDatabaseTestSupport.reset(focusSessionRepository, jdbcTemplate)
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
    fun `POST start empty subject returns 400 with ApiErrorResponse`() {
        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(StartFocusSessionRequest(""))

            .exchange()

            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.error").isEqualTo("subject can not be empty")
    }

    @Test
    fun `GET history returns active session`() {
        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(StartFocusSessionRequest("reading"))

            .exchange()

            .expectStatus().isCreated

        webTestClient
            .get()
            .uri("/focus/history")

            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content[0].subject").isEqualTo("reading")
            .jsonPath("$.content[0].status").isEqualTo("ACTIVE")
            .jsonPath("$.content[1]").doesNotExist()
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.totalElements").isEqualTo(1)
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
            .jsonPath("$.content[0].subject").isEqualTo("work")
            .jsonPath("$.content[0].status").isEqualTo("STOPPED")
            .jsonPath("$.content[1]").doesNotExist()
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.totalElements").isEqualTo(1)
    }

    @Test
    fun `GET history supports page and size query params`() {
        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(StartFocusSessionRequest("first"))
            .exchange()
            .expectStatus().isCreated

        webTestClient
            .post()
            .uri("/focus/start")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(StartFocusSessionRequest("second"))
            .exchange()
            .expectStatus().isCreated

        webTestClient
            .get()
            .uri("/focus/history?page=0&size=1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content.length()").isEqualTo(1)
            .jsonPath("$.content[0].subject").isEqualTo("second")
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.size").isEqualTo(1)
            .jsonPath("$.totalElements").isEqualTo(2)
            .jsonPath("$.totalPages").isEqualTo(2)

        webTestClient
            .get()
            .uri("/focus/history?page=1&size=1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content.length()").isEqualTo(1)
            .jsonPath("$.content[0].subject").isEqualTo("first")
            .jsonPath("$.page").isEqualTo(1)
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
