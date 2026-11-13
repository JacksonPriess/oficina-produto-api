package com.dinoco.oficina.catalogoproduto.infrastructure.web;

import com.dinoco.oficina.catalogoproduto.domain.TipoProduto;
import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoEventPublisher;
import com.dinoco.oficina.catalogoproduto.infrastructure.config.ProdutoConfig;
import com.dinoco.oficina.catalogoproduto.infrastructure.gateways.*;
import com.dinoco.oficina.catalogoproduto.infrastructure.persistence.*;
import com.dinoco.oficina.catalogoproduto.infrastructure.web.mapper.ProdutoWebMapper;
import com.dinoco.oficina.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.*;
import java.math.BigDecimal;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Testa o contrato HTTP com mapper, controllers, handlers e gateways reais.
 * Somente repositório, publicação e gerenciador de transação são simulados. */
class ProdutoHttpTest {
    ProdutoJpaRepository repository = mock(ProdutoJpaRepository.class);
    ProdutoEventPublisher eventos = mock(ProdutoEventPublisher.class);
    PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    TransactionStatus transaction = mock(TransactionStatus.class);
    MockMvc mvc;
    String body = """
            {"nome":"Filtro","tipo":"PECA","marca":"Marca A","codigoFabricante":"ABC",
             "aplicacao":"Motor","quantidade":3,"precoCusto":20.50,"precoVenda":35.90,"versao":2}
            """;

    @BeforeEach
    void configurar() {
        when(manager.getTransaction(any())).thenReturn(transaction);
        var config = new ProdutoConfig();
        var command = new ProdutoCommandGatewayImpl(repository);
        var query = new ProdutoQueryGatewayImpl(repository);
        var controller = config.produtoControllerClean(
                config.criarProdutoUseCase(command, query, eventos, manager),
                config.atualizarProdutoUseCase(command), config.desativarProdutoUseCase(command),
                config.buscarProdutoPorIdUseCase(query), config.buscarProdutoPorTermoUseCase(query));
        mvc = MockMvcBuilders.standaloneSetup(new ProdutoSpringWebController(controller,
                Mappers.getMapper(ProdutoWebMapper.class))).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    ProdutoEntity entity() {
        return new ProdutoEntity(7L, "Filtro", TipoProduto.PECA, "Marca A", "ABC", "Motor",
                new BigDecimal("20.50"), new BigDecimal("35.90"), true, 2L);
    }

    @Test
    void cadastrarPersisteCamposPublicaQuantidadeERetornaLocation() throws Exception {
        when(repository.saveAndFlush(any())).thenAnswer(inv -> {
            ProdutoEntity e = inv.getArgument(0);
            e.setId(7L); e.setVersion(0L);
            return e;
        });
        mvc.perform(post("/api/produtos").contentType("application/json").content(body))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/api/produtos/7"))
                .andExpect(jsonPath("$.id").value(7)).andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.precoVenda").value(35.90)).andExpect(jsonPath("$.precoCusto").value(20.50))
                .andExpect(jsonPath("$.ativo").value(true));
        var captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        assertThat(captor.getValue()).usingRecursiveComparison().ignoringFields("version").isEqualTo(entity());
        var ordem = inOrder(repository, eventos, manager);
        ordem.verify(repository).saveAndFlush(any());
        ordem.verify(eventos).publicar(argThat(e -> e.produtoId().equals(7L) && e.quantidade().compareTo(new BigDecimal("3")) == 0));
        ordem.verify(manager).commit(transaction);
    }

    @Test
    void atualizarUsaVersaoDoClienteERetornaVersaoPersistida() throws Exception {
        when(repository.findById(7L)).thenReturn(Optional.of(entity()));
        when(repository.saveAndFlush(any())).thenAnswer(inv -> {
            ProdutoEntity e = inv.getArgument(0); e.setVersion(3L); return e;
        });
        mvc.perform(put("/api/produtos/7").contentType("application/json").content(body.replace("Filtro", "Filtro novo")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.versao").value(3))
                .andExpect(jsonPath("$.nome").value("Filtro novo"))
                .andExpect(jsonPath("$.precoCusto").value(20.50)).andExpect(jsonPath("$.precoVenda").value(35.90));
        verifyNoInteractions(eventos);
    }

    @Test
    void versaoDoClienteEhEncaminhadaAoRepositorioParaControleOtimista() throws Exception {
        when(repository.findById(7L)).thenReturn(Optional.of(entity()));
        when(repository.saveAndFlush(any())).thenAnswer(inv -> {
            ProdutoEntity recebido = inv.getArgument(0);
            assertThat(recebido.getVersion()).isEqualTo(1L);
            return recebido;
        });
        mvc.perform(put("/api/produtos/7").contentType("application/json").content(body.replace("\"versao\":2", "\"versao\":1")))
                .andExpect(status().isOk());
        verify(repository).saveAndFlush(any());
    }

    @Test
    void desativarPreservaDadosEDesabilitaProduto() throws Exception {
        when(repository.findById(7L)).thenReturn(Optional.of(entity()));
        when(repository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        mvc.perform(delete("/api/produtos/7")).andExpect(status().isNoContent());
        var c = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repository).saveAndFlush(c.capture());
        assertThat(c.getValue().getAtivo()).isFalse();
        assertThat(c.getValue()).usingRecursiveComparison().ignoringFields("ativo").isEqualTo(entity());
    }

    @Test
    void consultarPorIdEPorTermoPreservaDados() throws Exception {
        when(repository.findById(7L)).thenReturn(Optional.of(entity()));
        when(repository.findByNome("Filt")).thenReturn(List.of(entity()));
        mvc.perform(get("/api/produtos/7")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Filtro")).andExpect(jsonPath("$.precoVenda").value(35.90))
                .andExpect(jsonPath("$.precoCusto").value(20.50)).andExpect(jsonPath("$.marca").value("Marca A"))
                .andExpect(jsonPath("$.codigoFabricante").value("ABC")).andExpect(jsonPath("$.aplicacao").value("Motor"));
        mvc.perform(get("/api/produtos").param("busca", "Filt")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));
        verify(repository).findByNome("Filt");
    }

    @Test
    void consultaSemTermoRetornaListaVazia() throws Exception {
        when(repository.findByNome(null)).thenReturn(List.of());
        mvc.perform(get("/api/produtos")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void nomeVazioQuantidadeNegativaEVersaoAusenteSaoRejeitados() throws Exception {
        mvc.perform(post("/api/produtos").contentType("application/json")
                .content("{\"nome\":\"\",\"tipo\":\"PECA\",\"quantidade\":-1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Erro de validação de campos"));
        mvc.perform(put("/api/produtos/7").contentType("application/json")
                .content("{\"nome\":\"Filtro\",\"tipo\":\"PECA\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository, eventos);
    }

    @Test
    void recursosAusentesRespeitamTratamentoAtualDosHandlers() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        mvc.perform(get("/api/produtos/99")).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/produtos/99")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/produtos/99").contentType("application/json").content(body)).andExpect(status().isNotFound());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void nomeDuplicadoRetorna400SemSalvar() throws Exception {
        when(repository.existsByNome("Filtro")).thenReturn(true);
        mvc.perform(post("/api/produtos").contentType("application/json").content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Regra de negócio violada"));
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(eventos);
        verify(manager).rollback(transaction);
    }

    @Test
    void falhaNaOutboxReverteCadastroENaoExpoeDetalhesInternos() throws Exception {
        when(repository.saveAndFlush(any())).thenReturn(entity());
        doThrow(new RuntimeException("senha-banco-interna")).when(eventos).publicar(any());
        mvc.perform(post("/api/produtos").contentType("application/json").content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("senha-banco-interna"))));
        verify(manager).rollback(transaction);
        verify(manager, never()).commit(any());
    }

    @Test
    void idInvalidoEJsonMalformadoRetornam400() throws Exception {
        mvc.perform(get("/api/produtos/abc")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/produtos").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
}
