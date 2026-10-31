package com.dinoco.oficina.catalogoproduto.adapters.controllers;

import com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar.AtualizarProdutoCommand;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar.AtualizarProdutoOutput;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.atualizar.AtualizarProdutoUseCase;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar.CriarProdutoCommand;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar.CriarProdutoOutput;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar.CriarProdutoUseCase;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.desativar.DesativarProdutoCommand;
import com.dinoco.oficina.catalogoproduto.application.usecases.commands.desativar.DesativarProdutoUseCase;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarporid.BuscarProdutoPorIdQuery;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarporid.BuscarProdutoPorIdUseCase;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarportermo.BuscarProdutoPorTermoQuery;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarportermo.BuscarProdutoPorTermoUseCase;
import java.util.List;

public class ProdutoControllerClean {

    private final CriarProdutoUseCase criarProdutoUseCase;
    private final AtualizarProdutoUseCase atualizarProdutoUseCase;
    private final DesativarProdutoUseCase desativarProdutoUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final BuscarProdutoPorTermoUseCase buscarProdutoPorTermoUseCase;

    public ProdutoControllerClean(CriarProdutoUseCase criarProdutoUseCase, AtualizarProdutoUseCase atualizarProdutoUseCase, DesativarProdutoUseCase desativarProdutoUseCase, BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase, BuscarProdutoPorTermoUseCase buscarProdutoPorTermoUseCase) {
        this.criarProdutoUseCase = criarProdutoUseCase;
        this.atualizarProdutoUseCase = atualizarProdutoUseCase;
        this.desativarProdutoUseCase = desativarProdutoUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
        this.buscarProdutoPorTermoUseCase = buscarProdutoPorTermoUseCase;
    }

    public CriarProdutoOutput criarProduto(CriarProdutoCommand command) {
        return criarProdutoUseCase.executar(command);
    }

    public AtualizarProdutoOutput atualizarProduto(AtualizarProdutoCommand command) {
        return atualizarProdutoUseCase.executar(command);
    }

    public void desativarProduto(DesativarProdutoCommand command) {
        desativarProdutoUseCase.executar(command);
    }

    public ProdutoQueryOutput buscarPorId(BuscarProdutoPorIdQuery query) {
        return buscarProdutoPorIdUseCase.executar(query);
    }

    public List<ProdutoQueryOutput> buscarPorTermo(BuscarProdutoPorTermoQuery termo) {
        return buscarProdutoPorTermoUseCase.executar(termo);
    }
}