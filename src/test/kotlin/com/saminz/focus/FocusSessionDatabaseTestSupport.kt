package com.saminz.focus

import org.springframework.jdbc.core.JdbcTemplate

// this helper is like a shared “wipe DB and reset ids” button the tests press before each run
object FocusSessionDatabaseTestSupport {
    fun reset(repository: FocusSessionRepository, jdbcTemplate: JdbcTemplate) {
        repository.deleteAll()
        jdbcTemplate.execute("ALTER SEQUENCE focus_sessions_id_seq RESTART WITH 1")
    }
}
