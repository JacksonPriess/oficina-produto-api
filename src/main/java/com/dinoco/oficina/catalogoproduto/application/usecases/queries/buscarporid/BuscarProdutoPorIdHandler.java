package com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarporid;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoQueryGateway;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BuscarProdutoPorIdHandler implements BuscarProdutoPorIdUseCase {

    private final ProdutoQueryGateway produtoQueryGateway;

    public BuscarProdutoPorIdHandler(ProdutoQueryGateway produtoQueryGateway) {
        this.produtoQueryGateway = produtoQueryGateway;
    }

    @Override
    public ProdutoQueryOutput executar(BuscarProdutoPorIdQuery query) {

        log.debug("evento=consulta_produto_iniciada produtoId={}", query.id());

        return produtoQueryGateway.buscarPorId(query.id())
                .map(produto -> {
                    log.info("evento=produto_encontrado produtoId={}", produto.id());
                    return produto;
                })
                .orElseThrow(() -> {
                    log.warn("evento=produto_nao_encontrado produtoId={}", query.id());
                    return new IllegalArgumentException("Produto não encontrado com ID: " + query.id());
                });
    }
}