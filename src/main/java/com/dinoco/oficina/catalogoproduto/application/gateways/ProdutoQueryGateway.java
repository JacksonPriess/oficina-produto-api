package com.dinoco.oficina.catalogoproduto.application.gateways;

import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;
import java.util.List;
import java.util.Optional;

public interface ProdutoQueryGateway {

    boolean existePorNome(String nome);
    Optional<ProdutoQueryOutput> buscarPorId(Long id);
    List<ProdutoQueryOutput> buscarPorTermo(String termo);
}