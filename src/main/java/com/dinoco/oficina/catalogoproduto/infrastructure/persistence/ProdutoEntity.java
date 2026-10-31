package com.dinoco.oficina.catalogoproduto.infrastructure.persistence;

import com.dinoco.oficina.catalogoproduto.domain.TipoProduto;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "produto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoProduto tipo;

    @Column(length = 100)
    private String marca;

    @Column(name = "codigo_fabricante", length = 100)
    private String codigoFabricante;

    @Column(columnDefinition = "TEXT")
    private String aplicacao;

    @Column(name = "preco_custo", nullable = false)
    private BigDecimal precoCusto = BigDecimal.ZERO;

    @Column(name = "preco_venda", nullable = false)
    private BigDecimal precoVenda = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Version
    private Long version;
}