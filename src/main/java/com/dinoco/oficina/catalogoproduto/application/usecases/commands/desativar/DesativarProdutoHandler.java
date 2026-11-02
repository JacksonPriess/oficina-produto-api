package com.dinoco.oficina.catalogoproduto.application.usecases.commands.desativar;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoCommandGateway;
import com.dinoco.oficina.catalogoproduto.domain.Produto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DesativarProdutoHandler implements DesativarProdutoUseCase {

    private final ProdutoCommandGateway produtoCommandGateway;

    public DesativarProdutoHandler(ProdutoCommandGateway produtoCommandGateway) {
        this.produtoCommandGateway = produtoCommandGateway;
    }

    @Override
    public void executar(DesativarProdutoCommand command) {

        log.debug(
                "evento=produto_desativacao_iniciado produtoId={} versao={}",
                command.id()
        );

        Produto produto = produtoCommandGateway.buscarParaAlteracao(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        produto.desativar();
        produtoCommandGateway.salvar(produto);

        log.info(
                "evento=produto_desativacao_sucesso produtoId={} versao={}",
                produto.getId(),
                produto.getVersao()
        );
    }
}
