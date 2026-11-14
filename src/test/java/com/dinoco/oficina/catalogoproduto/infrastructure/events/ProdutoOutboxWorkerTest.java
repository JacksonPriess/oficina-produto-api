package com.dinoco.oficina.catalogoproduto.infrastructure.events;

import com.dinoco.oficina.catalogoproduto.infrastructure.config.ProdutoRabbitConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.*;
import org.springframework.transaction.*;
import java.sql.ResultSet;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.*;

class ProdutoOutboxWorkerTest {
    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    RabbitTemplate rabbit = mock(RabbitTemplate.class);
    PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    TransactionStatus status = mock(TransactionStatus.class);
    UUID id = UUID.randomUUID();
    String payload = "{\"produtoId\":42,\"quantidade\":3}";
    ProdutoOutboxWorker worker;

    @BeforeEach
    void configurar() {
        when(manager.getTransaction(any())).thenReturn(status);
        worker = new ProdutoOutboxWorker(jdbc, rabbit, manager);
    }

    void pendente() {
        when(jdbc.query(anyString(), any(RowMapper.class))).thenAnswer(inv -> {
            RowMapper<?> mapper = inv.getArgument(1);
            var rs = mock(ResultSet.class);
            when(rs.getObject(1, UUID.class)).thenReturn(id);
            when(rs.getString(2)).thenReturn(payload);
            return List.of(mapper.mapRow(rs, 0));
        });
    }

    @Test
    void filaVaziaNaoPublicaNemAtualiza() {
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of());
        worker.publicarPendente();
        verifyNoInteractions(rabbit);
        verify(jdbc, never()).update(anyString(), any(Object[].class));
        verify(manager).commit(status);
    }

    @Test
    void confirmaPublicacaoSomenteAposAckComMetadadosPersistentes() {
        pendente();
        doAnswer(inv -> {
            Message m = inv.getArgument(2);
            assertThat(new String(m.getBody(), StandardCharsets.UTF_8)).isEqualTo(payload);
            assertThat(m.getMessageProperties().getMessageId()).isEqualTo(id.toString());
            assertThat(m.getMessageProperties().getContentType()).isEqualTo("application/json");
            assertThat(m.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
            verify(jdbc, never()).update(contains("SET publicado_em"), any(UUID.class));
            CorrelationData c = inv.getArgument(3);
            c.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbit).send(eq(ProdutoRabbitConfig.EXCHANGE), eq(ProdutoRabbitConfig.ROUTING_KEY), any(Message.class), any(CorrelationData.class));
        worker.publicarPendente();
        verify(jdbc).update(contains("SET publicado_em"), eq(id));
        verify(jdbc, never()).update(contains("tentativas=tentativas+1"), anyString(), any(UUID.class));
        verify(manager).commit(status);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nack", "devolvida", "conexao"})
    void falhaAgendaNovaTentativaSemMarcarComoPublicado(String falha) {
        pendente();
        doAnswer(inv -> {
            if (falha.equals("conexao")) throw new IllegalStateException("broker indisponivel");
            CorrelationData c = inv.getArgument(3);
            if (falha.equals("devolvida")) c.setReturned(new ReturnedMessage(inv.getArgument(2), 312,
                    "NO_ROUTE", ProdutoRabbitConfig.EXCHANGE, ProdutoRabbitConfig.ROUTING_KEY));
            c.getFuture().complete(new CorrelationData.Confirm(!falha.equals("nack"), "teste"));
            return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        worker.publicarPendente();
        verify(jdbc, never()).update(contains("SET publicado_em"), any(UUID.class));
        verify(jdbc).update(contains("tentativas=tentativas+1"), anyString(), eq(id));
        verify(manager).commit(status);
    }

    @Test
    void falhaNaConsultaReverteTransacaoSemPublicar() {
        when(jdbc.query(anyString(), any(RowMapper.class))).thenThrow(new IllegalStateException("banco offline"));
        assertThatCode(worker::publicarPendente).doesNotThrowAnyException();
        verify(manager).rollback(status);
        verifyNoInteractions(rabbit);
    }

    @Test
    void interrupcaoPreservaSinalEAgendaRecuperacao() {
        pendente();
        Thread.currentThread().interrupt();
        try {
            worker.publicarPendente();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            verify(jdbc).update(contains("tentativas=tentativas+1"), contains("InterruptedException"), eq(id));
        } finally {
            Thread.interrupted();
        }
    }
}
