package com.dinoco.oficina.catalogoproduto.infrastructure.gateways;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoQueryGateway;
import com.dinoco.oficina.catalogoproduto.application.usecases.queries.ProdutoQueryOutput;
import com.dinoco.oficina.catalogoproduto.infrastructure.persistence.ProdutoEntity;
import com.dinoco.oficina.catalogoproduto.infrastructure.persistence.ProdutoJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class ProdutoQueryGatewayImpl implements ProdutoQueryGateway {

    private final ProdutoJpaRepository jpaRepository;

    public ProdutoQueryGatewayImpl(ProdutoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existePorNome(String nome) {
        return jpaRepository.existsByNome(nome);
    }

    @Override
    public Optional<ProdutoQueryOutput> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::mapearParaOutput);
    }

    @Override
    public List<ProdutoQueryOutput> buscarPorTermo(String nome) {
        return jpaRepository.findByNome(nome)
                .stream()
                .map(this::mapearParaOutput)
                .toList();

    }

    private ProdutoQueryOutput mapearParaOutput(ProdutoEntity entity) {
        return new ProdutoQueryOutput(
                entity.getId(),
                entity.getVersion(),
                entity.getNome(),
                entity.getTipo(),
                entity.getMarca(),
                entity.getCodigoFabricante(),
                entity.getAplicacao(),
                entity.getPrecoCusto(),
                entity.getPrecoVenda(),
                entity.getAtivo()
        );
    }
}