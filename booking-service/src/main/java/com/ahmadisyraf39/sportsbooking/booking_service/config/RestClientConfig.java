package com.ahmadisyraf39.sportsbooking.booking_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${venue-service.base-url:http://localhost:8082}")
    private String venueServiceBaseUrl;

    @Bean
    public RestClient venueServiceRestClient() {
        return RestClient.builder()
                .baseUrl(venueServiceBaseUrl)
                .build();
    }
}