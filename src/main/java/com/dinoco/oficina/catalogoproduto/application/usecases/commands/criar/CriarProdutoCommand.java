package com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar;

import com.dinoco.oficina.catalogoproduto.domain.TipoProduto;
import java.math.BigDecimal;

public record CriarProdutoCommand(
        String nome,
        TipoProduto tipo,
        String marca,
        String codigoFabricante,
        String aplicacao,
        BigDecimal quantidade,
        BigDecimal precoCusto,
        BigDecimal precoVenda
) {}
