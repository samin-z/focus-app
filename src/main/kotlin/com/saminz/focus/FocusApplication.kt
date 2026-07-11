package com.saminz.focus

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(FocusProperties::class)
class FocusApplication

fun main(args: Array<String>) {
	runApplication<FocusApplication>(*args)
}
