package com.dinoco.oficina.catalogoproduto.domain;

public enum TipoProduto {

    PECA("Peça de Reposição"),
    INSUMO("Insumo de Uso Interno (Óleo, Graxa, Estopa)");

    private final String descricao;

    TipoProduto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
