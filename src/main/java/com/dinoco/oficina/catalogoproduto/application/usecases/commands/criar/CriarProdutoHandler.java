package com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoCommandGateway;
import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoEventPublisher;
import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoQueryGateway;
import com.dinoco.oficina.catalogoproduto.domain.Produto;
import com.dinoco.oficina.shared.events.ProdutoCadastradoEvent;
import java.util.UUID;


public class CriarProdutoHandler implements CriarProdutoUseCase {

    private final ProdutoCommandGateway produtoCommandGateway;
    private final ProdutoQueryGateway produtoQueryGateway;
    private final ProdutoEventPublisher eventPublisher;

    public CriarProdutoHandler(ProdutoCommandGateway produtoCommandGateway, ProdutoQueryGateway produtoQueryGateway, ProdutoEventPublisher eventPublisher) {
        this.produtoCommandGateway = produtoCommandGateway;
        this.produtoQueryGateway = produtoQueryGateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CriarProdutoOutput executar(CriarProdutoCommand command) {

        if (command.quantidade() != null && command.quantidade().signum() < 0) {
            throw new IllegalArgumentException("Quantidade inicial não pode ser negativa.");
        }

        if (produtoQueryGateway.existePorNome(command.nome())) {
            throw new IllegalArgumentException("Produto já cadastrado com este nome.");
        }

        // A quantidade informada pelo usuário não será utilizada em Produto, será usada apenas no SaldoEstoque.
        Produto novoProduto = new Produto(
                command.nome(),
                command.tipo(),
                command.marca(),
                command.codigoFabricante(),
                command.aplicacao(),
                command.precoCusto(),
                command.precoVenda()
        );

        Produto produtoSalvo = produtoCommandGateway.salvar(novoProduto);

        // O gateway registra a intenção de publicação na mesma transação do produto.
        ProdutoCadastradoEvent evento = new ProdutoCadastradoEvent(UUID.randomUUID(), produtoSalvo.getId(), command.quantidade());
        eventPublisher.publicar(evento);

        return mapearParaOutput(produtoSalvo);
    }

    private CriarProdutoOutput mapearParaOutput(Produto produto) {

        return new CriarProdutoOutput(
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