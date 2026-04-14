package br.com.promochef.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FichaTecnicaId implements Serializable {
    @Column(name = "produto_id")
    private Long produtoId;
    
    @Column(name = "ingrediente_id")
    private Long ingredienteId;
}
