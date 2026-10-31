package com.dinoco.oficina.catalogoproduto.infrastructure.web.dto;

import com.dinoco.oficina.catalogoproduto.domain.TipoProduto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ProdutoRequestDto(

    @NotBlank(message = "O nome do produto é obrigatório.")
    String nome,

    @NotNull(message = "O tipo do produto é obrigatório PECA/INSUMO.")
    TipoProduto tipo,

    String marca,
    String codigoFabricante,
    String aplicacao,

    @PositiveOrZero
    BigDecimal quantidade,

    @PositiveOrZero
    BigDecimal precoCusto,

    @PositiveOrZero
    BigDecimal precoVenda
) {}
