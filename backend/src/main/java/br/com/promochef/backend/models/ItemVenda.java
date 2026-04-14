package br.com.promochef.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;


@Entity
@Table(name = "tb_item_venda")
@Data
public class ItemVenda {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 8, scale = 2)
    private BigDecimal precoUnitario;
}
