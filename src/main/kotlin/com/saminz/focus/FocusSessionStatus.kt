package com.saminz.focus

enum class FocusSessionStatus {
    ACTIVE,
    STOPPED,
    ;

    fun requireActive() {
        check(this == ACTIVE) { "focus session already stopped" }
    }
}
