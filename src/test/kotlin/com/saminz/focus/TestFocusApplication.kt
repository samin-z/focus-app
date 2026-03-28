package com.saminz.focus

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<FocusApplication>().with(TestcontainersConfiguration::class).run(*args)
}
