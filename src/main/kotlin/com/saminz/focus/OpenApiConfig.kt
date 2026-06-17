package com.saminz.focus

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
// its a Spring configuration class that sets the top-level info for your API documentation
class OpenApiConfig {

    @Bean
    fun focusOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Focus API")
                    .description("Manage focus sessions: start, stop, and list sessions.")
                    .version("v1"),
            )
}
