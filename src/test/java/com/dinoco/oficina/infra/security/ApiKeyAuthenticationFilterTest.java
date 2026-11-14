package com.dinoco.oficina.infra.security;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.FilterChain;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class ApiKeyAuthenticationFilterTest {
    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void configuracaoSemChaveEhRejeitada(String chave) {
        assertThatIllegalArgumentException().isThrownBy(() -> new ApiKeyAuthenticationFilter(chave));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "invalida"})
    void chaveInvalidaBloqueiaCadeia(String chave) throws Exception {
        var request = new MockHttpServletRequest();
        if (chave != null) request.addHeader("X-API-Key", chave);
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        new ApiKeyAuthenticationFilter("segredo-teste").doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("API key ausente ou inválida");
        verifyNoInteractions(chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void chaveValidaAutenticaClienteComAutoridadeCorreta() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "segredo-teste");
        var response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth.isAuthenticated()).isTrue();
            assertThat(auth.getName()).isEqualTo("catalogo-api-client");
            assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_CATALOGO_CLIENT");
            res.getWriter().write("autorizado");
        };
        new ApiKeyAuthenticationFilter("segredo-teste").doFilter(request, response, chain);
        assertThat(response.getContentAsString()).isEqualTo("autorizado");
    }
}
