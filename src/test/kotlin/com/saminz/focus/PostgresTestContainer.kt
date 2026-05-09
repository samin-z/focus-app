package com.saminz.focus

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

object PostgresTestContainer {
    private val container =
        GenericContainer(DockerImageName.parse("postgres:16-alpine"))
            .withEnv("POSTGRES_DB", "focus")
            .withEnv("POSTGRES_USER", "focus")
            .withEnv("POSTGRES_PASSWORD", "focus")
            .withExposedPorts(5432)

    init {
        container.start()
    }

    fun register(registry: DynamicPropertyRegistry) {
        val jdbcUrl = "jdbc:postgresql://${container.host}:${container.getMappedPort(5432)}/focus"
        registry.add("spring.datasource.url") { jdbcUrl }
        registry.add("spring.datasource.username") { "focus" }
        registry.add("spring.datasource.password") { "focus" }
    }
}
