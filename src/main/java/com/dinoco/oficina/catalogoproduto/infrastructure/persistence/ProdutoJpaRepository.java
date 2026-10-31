package com.dinoco.oficina.catalogoproduto.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoJpaRepository extends JpaRepository<ProdutoEntity, Long> {
    boolean existsByNome(String nome);
    List<ProdutoEntity> findByNome(String nome);
}