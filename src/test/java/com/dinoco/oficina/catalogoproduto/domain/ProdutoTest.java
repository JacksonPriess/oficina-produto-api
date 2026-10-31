package com.dinoco.oficina.catalogoproduto.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {

    @Test
    void criarProdutoComSucesso() {
        Produto produto = new Produto("Óleo de Motor",
                TipoProduto.INSUMO, "MarcaX", "12345",
                "Aplicação Y", new BigDecimal("50.00"), new BigDecimal("80.00"));

        assertNotNull(produto);
        assertEquals("Óleo de Motor", produto.getNome());
        assertEquals(TipoProduto.INSUMO, produto.getTipo());
        assertEquals("MarcaX", produto.getMarca());
        assertEquals("12345", produto.getCodigoFabricante());
        assertEquals("Aplicação Y", produto.getAplicacao());
        assertEquals(new BigDecimal("50.00"), produto.getPrecoCusto());
        assertEquals(new BigDecimal("80.00"), produto.getPrecoVenda());
        assertTrue(produto.getAtivo());
    }

    @Test
    void criarProdutoSemNomeDeveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Produto("", TipoProduto.INSUMO, "MarcaX", "12345", "Aplicação Y",
                    new BigDecimal("50.00"), new BigDecimal("80.00"));
        });
        assertEquals("O nome do produto é obrigatório.", exception.getMessage());
    }

    @Test
    void criarProdutoSemTipoDeveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Produto("Óleo de Motor", null, "MarcaX", "12345", "Aplicação Y",
                    new BigDecimal("50.00"), new BigDecimal("80.00"));
        });
        assertEquals("O tipo do produto é obrigatório.", exception.getMessage());
    }

}