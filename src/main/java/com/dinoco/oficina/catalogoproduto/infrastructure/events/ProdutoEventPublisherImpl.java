package com.dinoco.oficina.catalogoproduto.infrastructure.events;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoEventPublisher;
import com.dinoco.oficina.shared.events.ProdutoCadastradoEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ProdutoEventPublisherImpl implements ProdutoEventPublisher {

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    public ProdutoEventPublisherImpl(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publicar(ProdutoCadastradoEvent event) {
        jdbc.update("INSERT INTO produto_outbox(event_id, payload) VALUES (?, ?)", event.eventId(), json.writeValueAsString(event));
    }
}