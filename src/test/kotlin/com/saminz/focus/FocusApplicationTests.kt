package com.saminz.focus

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
class FocusApplicationTests {
	companion object {
		@JvmStatic
		@DynamicPropertySource
		fun postgresProperties(registry: DynamicPropertyRegistry) {
			PostgresTestContainer.register(registry)
		}
	}

	@Test
	fun contextLoads() {
	}

}
