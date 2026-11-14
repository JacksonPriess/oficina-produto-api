package com.dinoco.oficina.infra.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.*;
import org.slf4j.MDC;
import java.util.UUID;
import jakarta.servlet.ServletException;
import static org.assertj.core.api.Assertions.*;

class CorrelationIdFilterTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "rastreio-123"})
    void propagaOuGeraIdentificadorELimpaMdc(String id) throws Exception {
        var req = new MockHttpServletRequest();
        if (id != null)
            req.addHeader("X-Correlation-ID", id);

        var res = new MockHttpServletResponse();
        new CorrelationIdFilter().doFilter(req, res, (r, s) -> {
            assertThat(MDC.get("correlationId")).isEqualTo(res.getHeader("X-Correlation-ID"));
            if (id == null || id.isBlank()) UUID.fromString(MDC.get("correlationId"));
            else assertThat(MDC.get("correlationId")).isEqualTo(id);
        });
        assertThat(res.getHeader("X-Correlation-ID")).isNotBlank();
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void limpaMdcMesmoSeRequisicaoFalhar() {
        assertThatThrownBy(() -> new CorrelationIdFilter().doFilter(new MockHttpServletRequest(),
                new MockHttpServletResponse(), (r, s) -> { throw new ServletException("falha"); }))
                .isInstanceOf(ServletException.class);
        assertThat(MDC.get("correlationId")).isNull();
    }
}
