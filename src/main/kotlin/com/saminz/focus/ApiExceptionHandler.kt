package com.saminz.focus

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

// this handler is applied to all @RestControllers; maps common exceptions to HTTP status + JSON body.
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // ex is the exception
    // Map<String, String> is because we return a small map and spring turns it into JSON, so a readable message is returned
    fun handleIllegalArgument(ex: IllegalArgumentException): ApiErrorResponse =
        ApiErrorResponse(ex.message ?: "invalid request")

    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    // = means that this function returns the result of what follows
    fun handleNotFound(ex: NoSuchElementException): ApiErrorResponse =
        ApiErrorResponse(ex.message ?: "not found")

    @ExceptionHandler(IllegalStateException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleIllegalState(ex: IllegalStateException): ApiErrorResponse =
        ApiErrorResponse(ex.message ?: "conflict")
}
