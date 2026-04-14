package br.com.promochef.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_ficha_tecnica")
@Data
public class FichaTecnica {

    @EmbeddedId
    private FichaTecnicaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("produtoId")
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredienteId")
    @JoinColumn(name = "ingrediente_id")
    private Ingrediente ingrediente;

    @Column(name = "quantidade_usada", nullable = false, precision = 10, scale = 4)
    private BigDecimal quantidadeUsada;

    @Column(nullable = false, length = 20)
    private String unidade;
}
