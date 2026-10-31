package com.dinoco.oficina.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErroPadraoDto(
        LocalDateTime timestamp,
        Integer status,
        String erro,
        List<String> mensagens
) {}
