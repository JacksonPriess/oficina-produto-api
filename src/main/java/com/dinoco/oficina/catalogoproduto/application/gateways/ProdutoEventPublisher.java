package com.dinoco.oficina.catalogoproduto.application.gateways;

import com.dinoco.oficina.shared.events.ProdutoCadastradoEvent;

public interface ProdutoEventPublisher {
    void publicar(ProdutoCadastradoEvent event);
}
