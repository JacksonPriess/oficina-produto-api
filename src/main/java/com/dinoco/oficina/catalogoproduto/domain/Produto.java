package com.dinoco.oficina.catalogoproduto.domain;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Produto {

    private Long id;
    private String nome;
    private TipoProduto tipo;
    private String marca;
    private String codigoFabricante;
    private String aplicacao;
    private BigDecimal precoCusto;
    private BigDecimal precoVenda;
    private Boolean ativo;
    private Long versao;

    public Produto(String nome, TipoProduto tipo, String marca, String codigoFabricante, String aplicacao,
                   BigDecimal precoCusto, BigDecimal precoVenda) {

        validarProduto(nome,tipo);
        this.nome = nome;
        this.tipo = tipo;
        this.marca = marca;
        this.codigoFabricante = codigoFabricante;
        this.aplicacao = aplicacao;
        this.precoCusto = precoCusto == null ? BigDecimal.ZERO : precoCusto;
        this.precoVenda = precoVenda == null ? BigDecimal.ZERO : precoVenda;
        this.ativo = true;
    }

    private void validarProduto(String nome, TipoProduto tipo) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo do produto é obrigatório.");
        }
    }

    public Produto(Long id, Long versao, String nome, TipoProduto tipo, String marca, String codigoFabricante, String aplicacao,
                   BigDecimal precoCusto, BigDecimal precoVenda, Boolean ativo ) {
        this.id = id;
        this.versao = versao;
        this.nome = nome;
        this.tipo = tipo;
        this.marca = marca;
        this.codigoFabricante = codigoFabricante;
        this.aplicacao = aplicacao;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.ativo = ativo;
    }

    public void atualizar(Long versao,
                          String nome,
                          TipoProduto tipo,
                          String marca,
                          String codigoFabricante,
                          String aplicacao,
                          BigDecimal precoCusto,
                          BigDecimal precoVenda) {
        this.versao = versao;
        this.nome = nome;
        this.tipo = tipo;
        this.marca = marca;
        this.codigoFabricante = codigoFabricante;
        this.aplicacao = aplicacao;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
    }

    public void desativar() {
        this.ativo = false;
    }

}