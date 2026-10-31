package com.dinoco.oficina.catalogoproduto.infrastructure.gateways;

import com.dinoco.oficina.catalogoproduto.application.gateways.ProdutoCommandGateway;
import com.dinoco.oficina.catalogoproduto.domain.Produto;
import com.dinoco.oficina.catalogoproduto.infrastructure.persistence.ProdutoEntity;
import com.dinoco.oficina.catalogoproduto.infrastructure.persistence.ProdutoJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ProdutoCommandGatewayImpl implements ProdutoCommandGateway {

    private final ProdutoJpaRepository jpaRepository;

    public ProdutoCommandGatewayImpl(ProdutoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Produto salvar(Produto produtoDominio) {
        ProdutoEntity entity = mapearParaEntity(produtoDominio);
        // saveAndFlush obriga o JPA a rodar o UPDATE imediatamente e retorna a versaõ atual.
        ProdutoEntity salvo = jpaRepository.saveAndFlush(entity);
        return mapearParaDominio(salvo);
    }

    @Override
    public Optional<Produto> buscarParaAlteracao(Long id) {
        return jpaRepository.findById(id).map(this::mapearParaDominio);
    }

    private ProdutoEntity mapearParaEntity(Produto dominio) {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(dominio.getId());
        entity.setVersion(dominio.getVersao());
        entity.setNome(dominio.getNome());
        entity.setTipo(dominio.getTipo());
        entity.setMarca(dominio.getMarca());
        entity.setCodigoFabricante(dominio.getCodigoFabricante());
        entity.setAplicacao(dominio.getAplicacao());
        entity.setPrecoCusto(dominio.getPrecoCusto());
        entity.setPrecoVenda(dominio.getPrecoVenda());
        entity.setAtivo(dominio.getAtivo());
        return entity;
    }

    private Produto mapearParaDominio(ProdutoEntity entity) {
        return new Produto(
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