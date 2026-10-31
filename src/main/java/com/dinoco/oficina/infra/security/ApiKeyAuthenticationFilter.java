package com.dinoco.oficina.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-API-Key";

    private final byte[] expectedApiKey;

    public ApiKeyAuthenticationFilter(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "A variável CATALOGO_API_KEY deve ser configurada."
            );
        }

        this.expectedApiKey = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String receivedApiKey = request.getHeader(HEADER_NAME);

        if (!isValid(receivedApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            response.getWriter().write(
                    "{\"mensagem\":\"API key ausente ou inválida.\"}"
            );
            return;
        }

        var authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "catalogo-api-client",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_CATALOGO_CLIENT")
                        )
                );

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }

    private boolean isValid(String receivedApiKey) {
        if (receivedApiKey == null || receivedApiKey.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                expectedApiKey,
                receivedApiKey.getBytes(StandardCharsets.UTF_8)
        );
    }
}
