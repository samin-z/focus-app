package com.saminz.focus

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

// Applied to all @RestControllers: maps common exceptions to HTTP status + JSON body.
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: WebExchangeBindException): ApiErrorResponse {
        val message = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
        return errorBody(message, "invalid request")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(ex: IllegalArgumentException): ApiErrorResponse =
        errorBody(ex.message, "invalid request")

    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: NoSuchElementException): ApiErrorResponse =
        errorBody(ex.message, "not found")

    @ExceptionHandler(IllegalStateException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleIllegalState(ex: IllegalStateException): ApiErrorResponse =
        errorBody(ex.message, "conflict")

    private fun errorBody(message: String?, fallback: String): ApiErrorResponse =
        ApiErrorResponse(message ?: fallback)
}
