package com.saminz.focus

object SubjectValidation {
    const val MAX_LENGTH = 255
    const val EMPTY_MESSAGE = "subject can not be empty"

    fun tooLongMessage(maxLength: Int): String =
        "subject must be at most $maxLength characters"

    fun clean(subject: String, maxLength: Int): String {
        val cleaned = subject.trim()
        require(cleaned.isNotEmpty()) { EMPTY_MESSAGE }
        require(cleaned.length <= maxLength) { tooLongMessage(maxLength) }
        return cleaned
    }
}
