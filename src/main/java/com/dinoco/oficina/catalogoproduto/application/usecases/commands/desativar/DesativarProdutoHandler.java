package com.dinoco.oficina.catalogoproduto.application.usecases.commands.desativar;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoCommandGateway;
import com.dinoco.oficina.catalogoproduto.domain.Produto;

public class DesativarProdutoHandler implements DesativarProdutoUseCase {

    private final ProdutoCommandGateway produtoCommandGateway;

    public DesativarProdutoHandler(ProdutoCommandGateway produtoCommandGateway) {
        this.produtoCommandGateway = produtoCommandGateway;
    }

    @Override
    public void executar(DesativarProdutoCommand command) {
        Produto produto = produtoCommandGateway.buscarParaAlteracao(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        produto.desativar();
        produtoCommandGateway.salvar(produto);
    }
}
