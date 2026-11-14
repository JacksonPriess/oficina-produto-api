package com.dinoco.oficina.catalogoproduto.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProdutoRegrasTest {

    @Test
    void deveRejeitarNomeNuloOuApenasEspacos() {
        for (var nome : new String[]{null, "   "}) {
            assertThatThrownBy(() -> new Produto(nome, TipoProduto.PECA, null, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void deveInicializarPrecosAusentesComoZero() {
        var p = new Produto("Filtro", TipoProduto.PECA, null, null, null, null, null);
        assertThat(p.getPrecoCusto()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(p.getPrecoVenda()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void deveAtualizarSemPerderIdentidadeOuReativarProduto() {
        var p = new Produto(1L, 0L, "Filtro", TipoProduto.PECA, "A", "B", "Motor", BigDecimal.ONE, BigDecimal.TEN, false);
        p.atualizar(2L, "Óleo", TipoProduto.INSUMO, "C", "D", "Câmbio", BigDecimal.TEN, new BigDecimal("20"));
        assertThat(p.getId()).isEqualTo(1L);
        assertThat(p.getVersao()).isEqualTo(2L);
        assertThat(p.getAtivo()).isFalse();
        assertThat(p.getNome()).isEqualTo("Óleo");
        assertThat(p.getTipo()).isEqualTo(TipoProduto.INSUMO);
        assertThat(p.getMarca()).isEqualTo("C");
        assertThat(p.getCodigoFabricante()).isEqualTo("D");
        assertThat(p.getAplicacao()).isEqualTo("Câmbio");
        assertThat(p.getPrecoCusto()).isEqualTo(BigDecimal.TEN);
        assertThat(p.getPrecoVenda()).isEqualByComparingTo("20");
    }

    @Test
    void devePermitirDesativacaoRepetidaSemAlterarPreco() {
        var p = new Produto("Filtro", TipoProduto.PECA, null, null, null, BigDecimal.ONE, BigDecimal.TEN);
        p.desativar();
        p.desativar();
        assertThat(p.getAtivo()).isFalse();
        assertThat(p.getPrecoVenda()).isEqualTo(BigDecimal.TEN);
    }
}
