package br.com.promochef.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_produto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false, length = 80)
    private String categoria;

    @Column(nullable = false)
    private Boolean ativo = true;
}