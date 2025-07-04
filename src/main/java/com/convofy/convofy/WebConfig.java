package com.convofy.convofy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply CORS to all endpoints
                        // Remove allowedOriginPatterns("*"). List specific origins directly.
                        .allowedOrigins(
                                "https://convofy-frontend-bdk8o1bqx-naishal-doshis-projects.vercel.app",
                                "https://convofy-frontend-weld.vercel.app",
                                // If you need localhost for local frontend development, include it.
                                // Otherwise, for production build, keep only your production Vercel origins.
                                 "http://localhost:5173",
                                "https://www.convofy.fun"
                                // "http://192.168.29.132:5173"
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Allowed HTTP methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true) // <--- ADD THIS LINE IF YOUR FRONTEND SENDS COOKIES/AUTH HEADERS
                        .maxAge(3600); // <--- ADD THIS LINE (optional, but good practice for caching preflight)
            }
        };
    }
}