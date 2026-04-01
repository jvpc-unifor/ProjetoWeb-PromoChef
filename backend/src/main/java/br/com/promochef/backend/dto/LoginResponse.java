package br.com.promochef.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Long expiresIn;
    private String nome;
    private String email;
    private String tipo;
}
