package com.dinoco.oficina.shared.events;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoCadastradoEvent(
        UUID eventId,
        Long produtoId,
        BigDecimal quantidade) {

    public ProdutoCadastradoEvent {

        if (eventId == null || produtoId == null || produtoId <= 0)
            throw new IllegalArgumentException("Evento exige eventId e produtoId positivo.");

        if (quantidade != null && quantidade.signum() < 0)
            throw new IllegalArgumentException("Quantidade inicial não pode ser negativa.");
    }
}