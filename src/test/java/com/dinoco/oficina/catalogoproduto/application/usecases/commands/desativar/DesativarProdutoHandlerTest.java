package com.dinoco.oficina.catalogoproduto.application.usecases.commands.desativar;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoCommandGateway;
import com.dinoco.oficina.catalogoproduto.domain.Produto;
import com.dinoco.oficina.catalogoproduto.domain.TipoProduto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesativarProdutoHandlerTest {

    @Mock
    private ProdutoCommandGateway produtoCommandGateway;

    @InjectMocks
    private DesativarProdutoHandler handler;

    @Captor
    private ArgumentCaptor<Produto> produtoCaptor;

    @Test
    @DisplayName("Deve desativar produto com sucesso")
    void deveDesativarProdutoComSucesso() {
        // Arrange
        Long idProduto = 1L;

        var command = new DesativarProdutoCommand(idProduto);

        var produtoExistente = new Produto(
                idProduto, 0L, "Pastilha de Freio", TipoProduto.PECA, "Cobreq", "C123",
                "Freio dianteiro", new BigDecimal("50.00"), new BigDecimal("120.00"), true
        );

        // Produto como esperamos que ele saia do gateway após salvar
        var produtoMockSalvo = new Produto(
                idProduto, 0L, "Pastilha de Freio", TipoProduto.PECA, "Cobreq", "C123",
                "Freio dianteiro", new BigDecimal("50.00"), new BigDecimal("120.00"), false
        );

        when(produtoCommandGateway.buscarParaAlteracao(idProduto)).thenReturn(Optional.of(produtoExistente));
        when(produtoCommandGateway.salvar(any(Produto.class))).thenReturn(produtoMockSalvo);

        // Act
        handler.executar(command);

        // Assert
        verify(produtoCommandGateway, times(1)).salvar(produtoCaptor.capture());

        Produto produtoSalvo = produtoCaptor.getValue();
        assertThat(produtoSalvo.getAtivo()).isFalse();
    }

    @Test
    @DisplayName("Deve lançar exceção quando o produto não for encontrado para desativar")
    void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
        // Arrange
        Long idProduto = 99L;
        var command = new DesativarProdutoCommand(idProduto);

        when(produtoCommandGateway.buscarParaAlteracao(idProduto)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> handler.executar(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Produto não encontrado.");

        // Garante que o método salvar nunca foi chamado
        verify(produtoCommandGateway, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve manter o produto desativado após múltiplas tentativas de desativação")
    void deveManterprodutoDesativadoAposMúltiplasDesativacoes() {
        // Arrange
        Long idProduto = 1L;

        var command = new DesativarProdutoCommand(idProduto);

        var produtoJaDesativado = new Produto(
                idProduto, 0L, "Pastilha de Freio", TipoProduto.PECA, "Cobreq", "C123",
                "Freio dianteiro", new BigDecimal("50.00"), new BigDecimal("120.00"), false
        );

        when(produtoCommandGateway.buscarParaAlteracao(idProduto)).thenReturn(Optional.of(produtoJaDesativado));
        when(produtoCommandGateway.salvar(any(Produto.class))).thenReturn(produtoJaDesativado);

        // Act
        handler.executar(command);

        // Assert
        verify(produtoCommandGateway, times(1)).salvar(produtoCaptor.capture());
        Produto produtoSalvo = produtoCaptor.getValue();
        assertThat(produtoSalvo.getAtivo()).isFalse();
    }
}
