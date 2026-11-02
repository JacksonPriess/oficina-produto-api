package com.dinoco.oficina.catalogoproduto.infrastructure.events;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.dinoco.oficina.catalogoproduto.infrastructure.config.ProdutoRabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
public class ProdutoOutboxWorker {

    private final JdbcTemplate jdbc;
    private final RabbitTemplate rabbit;
    private final TransactionTemplate transaction;

    public ProdutoOutboxWorker(JdbcTemplate jdbc, RabbitTemplate rabbit, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.rabbit = rabbit;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    private record Pendente(UUID id, String payload) {}

    @Scheduled(fixedDelayString = "${oficina.outbox.intervalo-ms:2000}")
    public void publicarPendente() {
        try {
            transaction.executeWithoutResult(status -> {
                var pendentes = jdbc.query("""
                    SELECT event_id, payload FROM produto_outbox
                    WHERE publicado_em IS NULL AND proxima_tentativa <= CURRENT_TIMESTAMP
                    ORDER BY criado_em LIMIT 1 FOR UPDATE SKIP LOCKED
                    """, (rs, row) -> new Pendente(rs.getObject(1, UUID.class), rs.getString(2)));

                if (pendentes.isEmpty())
                    return;

                var evento = pendentes.getFirst();

                log.debug(
                        "evento=produto_evento_publicacao_inicio eventoId={} exchange={} routingKey={}",
                        evento.id(),
                        ProdutoRabbitConfig.EXCHANGE,
                        ProdutoRabbitConfig.ROUTING_KEY
                );

                try {
                    var properties = new MessageProperties();
                    properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                    properties.setContentEncoding("UTF-8");
                    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    properties.setMessageId(evento.id().toString());
                    properties.setType(ProdutoRabbitConfig.ROUTING_KEY);

                    var correlation = new CorrelationData(UUID.randomUUID().toString());
                    rabbit.send(ProdutoRabbitConfig.EXCHANGE, ProdutoRabbitConfig.ROUTING_KEY,
                            new Message(evento.payload().getBytes(StandardCharsets.UTF_8), properties), correlation);

                    var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);

                    if (!confirm.ack() || correlation.getReturned() != null)
                        throw new IllegalStateException("Broker rejeitou ou mensagem ficou sem fila de destino.");

                    log.info(
                            "evento=produto_evento_publicacao_confirmada eventoId={} exchange={} routingKey={}",
                            evento.id(),
                            ProdutoRabbitConfig.EXCHANGE,
                            ProdutoRabbitConfig.ROUTING_KEY
                    );

                    jdbc.update("UPDATE produto_outbox SET publicado_em=CURRENT_TIMESTAMP, ultimo_erro=NULL WHERE event_id=?", evento.id());
                } catch (Exception e) {

                    if (e instanceof InterruptedException)
                        Thread.currentThread().interrupt();

                    log.warn(
                            "evento=produto_evento_publicacao_falha eventoId={}",
                            evento.id(),
                            e
                    );

                    jdbc.update("""
                        UPDATE produto_outbox SET tentativas=tentativas+1,
                        proxima_tentativa=CURRENT_TIMESTAMP + INTERVAL '10 seconds', ultimo_erro=?
                        WHERE event_id=?
                        """, e.toString().substring(0, Math.min(1000, e.toString().length())), evento.id());
                }
            });
        } catch (Exception e) {
            log.error(
                    "evento=produto_outbox_transacao_falha",
                    e
            );
        }
    }
}

