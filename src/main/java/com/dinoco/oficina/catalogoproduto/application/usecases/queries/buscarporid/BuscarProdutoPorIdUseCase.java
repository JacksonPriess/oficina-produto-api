package com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarporid;

import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;

public interface BuscarProdutoPorIdUseCase {
    ProdutoQueryOutput executar(BuscarProdutoPorIdQuery query);
}