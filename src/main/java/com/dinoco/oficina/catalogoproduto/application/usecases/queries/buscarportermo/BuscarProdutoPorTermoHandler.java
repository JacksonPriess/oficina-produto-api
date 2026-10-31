package com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarportermo;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoQueryGateway;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;
import java.util.List;

public class BuscarProdutoPorTermoHandler implements BuscarProdutoPorTermoUseCase {

    private final ProdutoQueryGateway produtoQueryGateway;

    public BuscarProdutoPorTermoHandler(ProdutoQueryGateway produtoQueryGateway) {
        this.produtoQueryGateway = produtoQueryGateway;
    }

    @Override
    public List<ProdutoQueryOutput> executar(BuscarProdutoPorTermoQuery query) {
        return produtoQueryGateway.buscarPorTermo(query.termo());

    }
}