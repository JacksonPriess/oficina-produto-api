package com.dinoco.oficina.catalogoproduto.infrastructure.events;

import com.dinoco.oficina.shared.events.ProdutoCadastradoEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.*;

class ProdutoEventPublisherImplTest {
    @Test
    void registraIdentificadorEPayloadCompletoNaOutbox() {
        var jdbc = mock(JdbcTemplate.class);
        var json = JsonMapper.builder().build();
        var evento = new ProdutoCadastradoEvent(UUID.randomUUID(), 42L, new BigDecimal("3.50"));
        new ProdutoEventPublisherImpl(jdbc, json).publicar(evento);
        var payload = ArgumentCaptor.forClass(String.class);
        verify(jdbc).update(eq("INSERT INTO produto_outbox(event_id, payload) VALUES (?, ?)"), eq(evento.eventId()), payload.capture());
        assertThat(json.readValue(payload.getValue(), ProdutoCadastradoEvent.class)).isEqualTo(evento);
    }

    @Test
    void falhaNoInsertEhPropagadaParaTransacaoExterna() {
        var jdbc = mock(JdbcTemplate.class);
        var erro = new org.springframework.dao.DataAccessResourceFailureException("offline");
        when(jdbc.update(anyString(), any(UUID.class), anyString())).thenThrow(erro);
        var publisher = new ProdutoEventPublisherImpl(jdbc, JsonMapper.builder().build());
        assertThatThrownBy(() -> publisher.publicar(new ProdutoCadastradoEvent(UUID.randomUUID(), 42L, null)))
                .isSameAs(erro);
    }
}
