package com.ahmadisyraf39.sportsbooking.booking_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient venueServiceRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
    }
}