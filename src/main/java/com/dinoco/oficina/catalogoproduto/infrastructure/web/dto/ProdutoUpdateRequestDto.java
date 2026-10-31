package com.dinoco.oficina.catalogoproduto.infrastructure.web.dto;

import com.dinoco.oficina.catalogoproduto.domain.TipoProduto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ProdutoUpdateRequestDto(

        @NotBlank(message = "O nome do produto é obrigatório.")
        String nome,

        @NotNull(message = "O tipo do produto é obrigatório.")
        TipoProduto tipo,

        String marca,
        String codigoFabricante,
        String aplicacao,

        @PositiveOrZero
        BigDecimal precoCusto,

        @PositiveOrZero
        BigDecimal precoVenda,

        @NotNull(message = "A versão é obrigatória.")
        Long versao
) {}