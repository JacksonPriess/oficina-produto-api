package com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoCommandGateway;
import com.dinoco.oficina.catalogoproduto.domain.Produto;
import com.dinoco.oficina.catalogoproduto.domain.TipoProduto;
import com.dinoco.oficina.exception.RecursoNaoEncontradoException;
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
class AtualizarProdutoHandlerTest {

    @Mock
    private ProdutoCommandGateway produtoCommandGateway;

    @InjectMocks
    private AtualizarProdutoHandler handler;

    @Captor
    private ArgumentCaptor<Produto> produtoCaptor;

    @Test
    @DisplayName("Deve atualizar produto com sucesso")
    void deveAtualizarProdutoComSucesso() {
        // Arrange
        Long idProduto = 1L;
        Long versao = 0L;

        var command = new AtualizarProdutoCommand(
                idProduto,
                versao,
                "Pastilha de Freio Atualizada",
                TipoProduto.PECA,
                "Cobreq",
                "C999",
                "Freio dianteiro e traseiro",
                new BigDecimal("55.00"),
                new BigDecimal("135.00")
        );

        // Produto como ele está no banco antes da atualização
        var produtoExistente = new Produto(
                idProduto, versao, "Pastilha de Freio", TipoProduto.PECA, "Cobreq", "C123",
                "Freio dianteiro", new BigDecimal("50.00"), new BigDecimal("120.00"), true
        );

        // Produto como esperamos que ele saia do gateway após salvar
        var produtoMockSalvo = new Produto(
                idProduto, versao, "Pastilha de Freio Atualizada", TipoProduto.PECA, "Cobreq", "C999",
                "Freio dianteiro e traseiro", new BigDecimal("55.00"), new BigDecimal("135.00"), true
        );

        when(produtoCommandGateway.buscarParaAlteracao(idProduto)).thenReturn(Optional.of(produtoExistente));
        when(produtoCommandGateway.salvar(any(Produto.class))).thenReturn(produtoMockSalvo);

        // Act
        AtualizarProdutoOutput response = handler.executar(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(idProduto);
        assertThat(response.nome()).isEqualTo("Pastilha de Freio Atualizada");
        assertThat(response.codigoFabricante()).isEqualTo("C999");
        assertThat(response.precoCusto()).isEqualTo(new BigDecimal("55.00"));
        assertThat(response.precoVenda()).isEqualTo(new BigDecimal("135.00"));

        // Verificamos o objeto de domínio enviado para o gateway de salvamento
        verify(produtoCommandGateway).salvar(produtoCaptor.capture());
        Produto produtoEnviadoParaSalvar = produtoCaptor.getValue();

        assertThat(produtoEnviadoParaSalvar.getNome()).isEqualTo("Pastilha de Freio Atualizada");
        assertThat(produtoEnviadoParaSalvar.getAplicacao()).isEqualTo("Freio dianteiro e traseiro");
        assertThat(produtoEnviadoParaSalvar.getPrecoCusto()).isEqualTo(new BigDecimal("55.00"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o produto não for encontrado")
    void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
        // Arrange
        var command = new AtualizarProdutoCommand(
                99L,
                0L,
                "Produto Inexistente",
                TipoProduto.PECA,
                "Marca",
                "COD",
                "Aplicacao",
                new BigDecimal("10.00"),
                new BigDecimal("20.00")
        );

        when(produtoCommandGateway.buscarParaAlteracao(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> handler.executar(command))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Produto não encontrado.");

        verify(produtoCommandGateway, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve atualizar apenas alguns campos do produto")
    void deveAtualizarApenasAlgunsCampos() {
        // Arrange
        Long idProduto = 1L;
        Long versao = 0L;

        var command = new AtualizarProdutoCommand(
                idProduto,
                versao,
                "Pastilha de Freio",
                TipoProduto.PECA,
                "Cobreq",
                "C123",
                "Freio dianteiro",
                new BigDecimal("45.00"),  // Preço custo alterado
                new BigDecimal("120.00")  // Preço venda mantido igual
        );

        var produtoExistente = new Produto(
                idProduto, versao, "Pastilha de Freio", TipoProduto.PECA, "Cobreq", "C123",
                "Freio dianteiro", new BigDecimal("50.00"), new BigDecimal("120.00"), true
        );

        var produtoMockSalvo = new Produto(
                idProduto, versao, "Pastilha de Freio", TipoProduto.PECA, "Cobreq", "C123",
                "Freio dianteiro", new BigDecimal("45.00"), new BigDecimal("120.00"), true
        );

        when(produtoCommandGateway.buscarParaAlteracao(idProduto)).thenReturn(Optional.of(produtoExistente));
        when(produtoCommandGateway.salvar(any(Produto.class))).thenReturn(produtoMockSalvo);

        // Act
        AtualizarProdutoOutput response = handler.executar(command);

        // Assert
        assertThat(response.precoCusto()).isEqualTo(new BigDecimal("45.00"));
        assertThat(response.precoVenda()).isEqualTo(new BigDecimal("120.00"));

        verify(produtoCommandGateway).salvar(produtoCaptor.capture());
        Produto produtoEnviadoParaSalvar = produtoCaptor.getValue();
        assertThat(produtoEnviadoParaSalvar.getPrecoCusto()).isEqualTo(new BigDecimal("45.00"));
    }
}
