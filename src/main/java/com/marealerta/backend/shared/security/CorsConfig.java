package com.marealerta.backend.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origens permitidas (ajuste as portas conforme seu frontend, ex: 3000, 5173, 8080)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        // Para liberar qualquer origem em ambiente de dev local, pode usar:
        // configuration.setAllowedOriginPatterns(List.of("*"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos na requisição (inclusive Authorization para JWT)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Permite envio de cookies/credenciais se necessário
        configuration.setAllowCredentials(true);

        // Tempo de cache da resposta prévia (preflight / OPTIONS) em segundos
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}