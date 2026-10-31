package com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoCommandGateway;
import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoEventPublisher;
import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoQueryGateway;
import com.dinoco.oficina.catalogoproduto.domain.Produto;
import com.dinoco.oficina.catalogoproduto.domain.TipoProduto;
import com.dinoco.oficina.shared.events.ProdutoCadastradoEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarProdutoHandlerTest {

    @Mock
    private ProdutoCommandGateway comandGateway;

    @Mock
    private ProdutoQueryGateway queryGateway;

    @Mock
    private ProdutoEventPublisher eventPublisher;

    @InjectMocks
    private CriarProdutoHandler handler;

    @Captor
    private ArgumentCaptor<Produto> produtoCaptor;

    @Captor
    private ArgumentCaptor<ProdutoCadastradoEvent> eventoCaptor;

    @Test
    @DisplayName("Deve criar produto com sucesso")
    void deveCriarProdutoComSucesso() {
        // Arrange
        var command = new CriarProdutoCommand(
                "Pastilha de Freio",
                TipoProduto.PECA,
                "Cobreq",
                "C123",
                "Freio dianteiro",
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("120.00")
        );

        var produtoMockSalvo = new Produto(
                1L, 0L, "Pastilha de Freio", TipoProduto.PECA, "Cobreq", "C123",
                "Freio dianteiro", new BigDecimal("50.00"), new BigDecimal("120.00"), true
        );

        // Simulamos o comportamento das portas de saída
        when(queryGateway.existePorNome(anyString())).thenReturn(false);
        when(comandGateway.salvar(any(Produto.class))).thenReturn(produtoMockSalvo);

        // Act
        CriarProdutoOutput response = handler.executar(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Pastilha de Freio");
        assertThat(response.ativo()).isTrue();

        // Verificamos se o gateway de escrita foi chamado capturando o objeto de domínio puro
        verify(comandGateway).salvar(produtoCaptor.capture());
        Produto produtoSalvo = produtoCaptor.getValue();
        assertThat(produtoSalvo.getNome()).isEqualTo("Pastilha de Freio");
        assertThat(produtoSalvo.getTipo()).isEqualTo(TipoProduto.PECA);
        assertThat(produtoSalvo.getMarca()).isEqualTo("Cobreq");
        assertThat(produtoSalvo.getCodigoFabricante()).isEqualTo("C123");
        assertThat(produtoSalvo.getPrecoCusto()).isEqualTo(new BigDecimal("50.00"));
        assertThat(produtoSalvo.getPrecoVenda()).isEqualTo(new BigDecimal("120.00"));

        // Verificamos se o evento foi publicado com a quantidade correta
        verify(eventPublisher).publicar(eventoCaptor.capture());
        ProdutoCadastradoEvent evento = eventoCaptor.getValue();
        assertThat(evento.produtoId()).isEqualTo(1L);
        assertThat(evento.quantidade()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o produto já existe")
    void deveLancarExcecaoQuandoProdutoJaExiste() {
        // Arrange
        var command = new CriarProdutoCommand(
                "Pastilha de Freio",
                TipoProduto.PECA,
                "Cobreq",
                "C123",
                "Freio dianteiro",
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("120.00")
        );

        when(queryGateway.existePorNome(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> handler.executar(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Produto já cadastrado com este nome.");
    }

    @Test
    @DisplayName("Deve criar produto com quantidade zero")
    void deveCriarProdutoComQuantidadeZero() {
        // Arrange
        var command = new CriarProdutoCommand(
                "Óleo de Motor",
                TipoProduto.INSUMO,
                "Mobil",
                "OM001",
                "Motor 1.6",
                new BigDecimal("0"),
                new BigDecimal("30.00"),
                new BigDecimal("85.00")
        );

        var produtoMockSalvo = new Produto(
                2L, 0L, "Óleo de Motor", TipoProduto.INSUMO, "Mobil", "OM001",
                "Motor 1.6", new BigDecimal("30.00"), new BigDecimal("85.00"), true
        );

        when(queryGateway.existePorNome(anyString())).thenReturn(false);
        when(comandGateway.salvar(any(Produto.class))).thenReturn(produtoMockSalvo);

        // Act
        CriarProdutoOutput response = handler.executar(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.nome()).isEqualTo("Óleo de Motor");

        //Evento foi publicado com quantidade zero
        verify(eventPublisher).publicar(eventoCaptor.capture());
        ProdutoCadastradoEvent evento = eventoCaptor.getValue();
        assertThat(evento.quantidade()).isEqualTo(new BigDecimal("0"));
    }
}
