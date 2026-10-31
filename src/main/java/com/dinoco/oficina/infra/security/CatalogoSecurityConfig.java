package com.dinoco.oficina.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class CatalogoSecurityConfig {

    @Bean
    public SecurityFilterChain catalogoSecurityFilterChain(
            HttpSecurity http,
            @Value("${catalogo.security.api-key}") String apiKey
    ) throws Exception {

        var apiKeyFilter = new ApiKeyAuthenticationFilter(apiKey);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        apiKeyFilter,
                        AnonymousAuthenticationFilter.class
                )

                .build();
    }
}