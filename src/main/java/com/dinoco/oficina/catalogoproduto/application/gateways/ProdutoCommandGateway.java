package com.dinoco.oficina.catalogoproduto.application.gateways;

import com.dinoco.oficina.catalogoproduto.domain.Produto;
import java.util.Optional;

public interface ProdutoCommandGateway {

    Produto salvar(Produto produto);

    Optional<Produto> buscarParaAlteracao(Long id);

}