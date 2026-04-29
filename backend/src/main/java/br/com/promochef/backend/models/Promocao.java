package br.com.promochef.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "tb_promocao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promocao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer descontoPct;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPromocao status;

    @Column(nullable = false)
    private LocalDate dataSugestao;

    @Column
    private LocalDate dataAtivacao;
}
