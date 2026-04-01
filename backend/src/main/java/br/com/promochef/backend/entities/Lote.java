package br.com.promochef.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_lote")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantidade;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal custoUnitario;

    @Column(nullable = false)
    private LocalDate dataValidade;

    @Column(nullable = false)
    private LocalDate dataEntrada;

    @Column(length = 50)
    private String numeroLote;

    @Column(columnDefinition = "TEXT")
    private String observacao;
}