package com.example.carappointmentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient garageWebClient(WebClient.Builder builder,
                                     @Value("${garage.service.base-url}") String garageServiceBaseUrl) {
        return builder.baseUrl(garageServiceBaseUrl).build();
    }
}
