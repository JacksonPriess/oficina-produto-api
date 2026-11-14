package com.dinoco.oficina.catalogoproduto.application;

import com.dinoco.oficina.catalogoproduto.application.gateways.*;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar.*;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar.*;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.desativar.*;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarporid.*;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarportermo.*;
import com.dinoco.oficina.catalogoproduto.domain.*;
import com.dinoco.oficina.shared.events.ProdutoCadastradoEvent;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProdutoCenariosComplementaresTest {

    private final ProdutoCommandGateway escrita = mock(ProdutoCommandGateway.class);
    private final ProdutoQueryGateway consulta = mock(ProdutoQueryGateway.class);
    private final ProdutoEventPublisher eventos = mock(ProdutoEventPublisher.class);
    private final CriarProdutoHandler criar = new CriarProdutoHandler(escrita, consulta, eventos);

    @Test
    void deveRejeitarQuantidadeNegativaAntesDeAcessarGateways() {
        assertThatThrownBy(() -> criar.executar(comando(BigDecimal.ONE.negate()))).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(escrita, consulta, eventos);
    }

    @Test
    void devePreservarQuantidadeNulaNoEventoEUsarIdPersistido() {
        var salvo = produto();
        when(escrita.salvar(any())).thenReturn(salvo);
        var output = criar.executar(comando(null));
        var captor = ArgumentCaptor.forClass(ProdutoCadastradoEvent.class);
        var ordem = inOrder(escrita, eventos);
        ordem.verify(escrita).salvar(any());
        ordem.verify(eventos).publicar(captor.capture());
        assertThat(captor.getValue().eventId()).isNotNull();
        assertThat(captor.getValue().produtoId()).isEqualTo(42L);
        assertThat(captor.getValue().quantidade()).isNull();
        assertThat(output.id()).isEqualTo(42L);
    }

    @Test
    void devePreservarQuantidadeFracionadaNoEvento() {
        when(escrita.salvar(any())).thenReturn(produto());
        criar.executar(comando(new BigDecimal("1.25")));
        var captor = ArgumentCaptor.forClass(ProdutoCadastradoEvent.class);
        verify(eventos).publicar(captor.capture());
        assertThat(captor.getValue().quantidade()).isEqualByComparingTo("1.25");
    }

    @Test
    void deveImpedirPersistenciaEEventoParaNomeDuplicado() {
        when(consulta.existePorNome("Filtro")).thenReturn(true);
        assertThatThrownBy(() -> criar.executar(comando(BigDecimal.ONE))).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(escrita, eventos);
    }

    @Test
    void deveImpedirEventoQuandoPersistenciaFalha() {
        var falha = new IllegalStateException("banco indisponível");
        when(escrita.salvar(any())).thenThrow(falha);
        assertThatThrownBy(() -> criar.executar(comando(BigDecimal.ONE))).isSameAs(falha);
        verifyNoInteractions(eventos);
    }

    @Test
    void devePropagarFalhaDoPublicadorParaTransacaoExterna() {
        when(escrita.salvar(any())).thenReturn(produto());
        var falha = new IllegalStateException("outbox indisponível");
        doThrow(falha).when(eventos).publicar(any());
        assertThatThrownBy(() -> criar.executar(comando(BigDecimal.ONE))).isSameAs(falha);
    }

    @Test
    void devePropagarFalhaAoAtualizar() {
        var p = produto();
        when(escrita.buscarParaAlteracao(42L)).thenReturn(Optional.of(p));
        var falha = new IllegalStateException("conflito de versão");
        when(escrita.salvar(p)).thenThrow(falha);
        assertThatThrownBy(() -> new AtualizarProdutoHandler(escrita).executar(
                new AtualizarProdutoCommand(42L, 0L, "Filtro novo", TipoProduto.PECA, "Marca", "A", "Motor", BigDecimal.ONE, BigDecimal.TEN)))
                .isSameAs(falha);
    }

    @Test
    void devePropagarFalhaAoDesativar() {
        var p = produto();
        when(escrita.buscarParaAlteracao(42L)).thenReturn(Optional.of(p));
        var falha = new IllegalStateException("falha");
        when(escrita.salvar(p)).thenThrow(falha);
        assertThatThrownBy(() -> new DesativarProdutoHandler(escrita).executar(new DesativarProdutoCommand(42L))).isSameAs(falha);
    }

    @Test
    void deveConsultarProdutoPorId() {
        var esperado = output();
        when(consulta.buscarPorId(42L)).thenReturn(Optional.of(esperado));
        assertThat(new BuscarProdutoPorIdHandler(consulta).executar(new BuscarProdutoPorIdQuery(42L))).isEqualTo(esperado);
    }

    @Test
    void deveRejeitarConsultaInexistente() {
        assertThatThrownBy(() -> new BuscarProdutoPorIdHandler(consulta).executar(new BuscarProdutoPorIdQuery(99L)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Produto não encontrado com ID: 99");
    }

    @Test
    void deveConsultarPorTermoSemAlterarResultados() {
        var esperado = List.of(output());
        when(consulta.buscarPorTermo("Filtro")).thenReturn(esperado);
        assertThat(new BuscarProdutoPorTermoHandler(consulta).executar(new BuscarProdutoPorTermoQuery("Filtro"))).isEqualTo(esperado);
        verify(consulta).buscarPorTermo("Filtro");
    }

    @Test
    void deveRetornarListaVaziaQuandoTermoNaoEncontrado() {
        when(consulta.buscarPorTermo("ausente")).thenReturn(List.of());
        assertThat(new BuscarProdutoPorTermoHandler(consulta).executar(new BuscarProdutoPorTermoQuery("ausente"))).isEmpty();
    }

    @Test
    void devePropagarFalhaNaConsulta() {
        var falha = new IllegalStateException("consulta indisponível");
        when(consulta.buscarPorId(42L)).thenThrow(falha);
        assertThatThrownBy(() -> new BuscarProdutoPorIdHandler(consulta).executar(new BuscarProdutoPorIdQuery(42L))).isSameAs(falha);
    }

    private CriarProdutoCommand comando(BigDecimal quantidade) {
        return new CriarProdutoCommand("Filtro", TipoProduto.PECA, "Marca", "A", "Motor", quantidade, BigDecimal.ONE, BigDecimal.TEN);
    }

    private Produto produto() {
        return new Produto(42L, 0L, "Filtro", TipoProduto.PECA, "Marca", "A", "Motor", BigDecimal.ONE, BigDecimal.TEN, true);
    }

    private ProdutoQueryOutput output() {
        return new ProdutoQueryOutput(42L, 0L, "Filtro", TipoProduto.PECA, "Marca", "A", "Motor", BigDecimal.ONE, BigDecimal.TEN, true);
    }
}
