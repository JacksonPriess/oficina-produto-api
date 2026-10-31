package com.dinoco.oficina.catalogoproduto.application.usecases.commands.criar;

public interface CriarProdutoUseCase {
    CriarProdutoOutput executar(CriarProdutoCommand input);
}