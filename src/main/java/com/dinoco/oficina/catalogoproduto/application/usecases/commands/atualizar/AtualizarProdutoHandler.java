package com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoCommandGateway;
import com.dinoco.oficina.catalogoproduto.domain.Produto;
import com.dinoco.oficina.exception.RecursoNaoEncontradoException;

public class AtualizarProdutoHandler implements AtualizarProdutoUseCase {

    private final ProdutoCommandGateway produtoCommandGateway;

    public AtualizarProdutoHandler(ProdutoCommandGateway produtoCommandGateway) {
        this.produtoCommandGateway = produtoCommandGateway;
    }

    @Override
    public AtualizarProdutoOutput executar(AtualizarProdutoCommand command) {

        Produto produto = produtoCommandGateway.buscarParaAlteracao(command.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));

        produto.atualizar(
                command.versao(),
                command.nome(),
                command.tipo(),
                command.marca(),
                command.codigoFabricante(),
                command.aplicacao(),
                command.precoCusto(),
                command.precoVenda()
        );

        Produto produtoSalvo = produtoCommandGateway.salvar(produto);
        return mapearParaOutput(produtoSalvo);
    }

    private AtualizarProdutoOutput mapearParaOutput(Produto produto) {
        return new AtualizarProdutoOutput(
                produto.getId(),
                produto.getVersao(),
                produto.getNome(),
                produto.getTipo(),
                produto.getMarca(),
                produto.getCodigoFabricante(),
                produto.getAplicacao(),
                produto.getPrecoCusto(),
                produto.getPrecoVenda(),
                produto.getAtivo()
        );
    }
}