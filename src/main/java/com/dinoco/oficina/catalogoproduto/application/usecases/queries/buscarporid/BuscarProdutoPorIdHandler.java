package com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarporid;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoQueryGateway;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;

public class BuscarProdutoPorIdHandler implements BuscarProdutoPorIdUseCase {

    private final ProdutoQueryGateway produtoQueryGateway;

    public BuscarProdutoPorIdHandler(ProdutoQueryGateway produtoQueryGateway) {
        this.produtoQueryGateway = produtoQueryGateway;
    }

    @Override
    public ProdutoQueryOutput executar(BuscarProdutoPorIdQuery query) {
        return produtoQueryGateway.buscarPorId(query.id())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com ID: " + query.id()));
    }
}