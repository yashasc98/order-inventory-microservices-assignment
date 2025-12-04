package com.yashas.assignment.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for Spring beans
 */
@Configuration
public class AppConfiguration {

    /**
     * Create RestTemplate bean for HTTP communication
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

