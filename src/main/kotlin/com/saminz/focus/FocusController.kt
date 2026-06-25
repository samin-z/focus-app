package com.saminz.focus

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Focus", description = "Start, stop, and list focus sessions")
@RestController
@RequestMapping("/focus")
class FocusController(
    private val focusSessionService: FocusSessionService,
) {

    @Operation(summary = "Start a focus session", description = "Creates a new ACTIVE session with the given subject.")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Session created",
            content = [Content(schema = Schema(implementation = FocusSessionResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid subject",
            content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
        ),
    )
    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    fun startFocusSession(@Valid @RequestBody request: StartFocusSessionRequest): FocusSessionResponse {
        val session = focusSessionService.startSession(request.subject)
        return session.toResponse()
    }

    @Operation(summary = "Stop a focus session", description = "Stops an ACTIVE session by id and returns the stopped session.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Session stopped",
            content = [Content(schema = Schema(implementation = FocusSessionResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Session not found",
            content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "Session already stopped",
            content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
        ),
    )
    @PostMapping("/{id}/stop")
    // PathVariable takes a value from url path and put it in the method parameter
    fun stopFocusSession(@PathVariable id: Long): FocusSessionResponse {
        val session = focusSessionService.stopSession(id)
        // convert internal FocusSession model to API response DTO
        return session.toResponse()
    }

    @Operation(
        summary = "List focus sessions",
        description = "Returns active and stopped sessions, ordered by most recent activity (end time for stopped, start time for active).",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "All sessions",
            content = [Content(array = ArraySchema(schema = Schema(implementation = FocusSessionResponse::class)))],
        ),
    )
    @GetMapping("/history")
    fun getFocusHistory(): List<FocusSessionResponse> {
        return focusSessionService.getHistory().map { it.toResponse() }
    }
}
