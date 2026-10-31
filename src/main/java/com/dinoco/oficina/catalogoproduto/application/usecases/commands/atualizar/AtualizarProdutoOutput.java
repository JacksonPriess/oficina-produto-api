package com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar;

import com.dinoco.oficina.catalogoproduto.domain.TipoProduto;

import java.math.BigDecimal;

public record AtualizarProdutoOutput(
        Long id,
        Long versao,
        String nome,
        TipoProduto tipo,
        String marca,
        String codigoFabricante,
        String aplicacao,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        Boolean ativo
) {}