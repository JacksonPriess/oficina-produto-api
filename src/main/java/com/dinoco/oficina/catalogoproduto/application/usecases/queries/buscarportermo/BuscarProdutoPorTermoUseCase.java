package com.dinoco.oficina.catalogoproduto.application.usecases.queries.buscarportermo;

import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;
import java.util.List;

public interface BuscarProdutoPorTermoUseCase {
    List<ProdutoQueryOutput> executar(BuscarProdutoPorTermoQuery query);
}