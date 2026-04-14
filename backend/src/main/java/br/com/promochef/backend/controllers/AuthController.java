package br.com.promochef.backend.controllers;

import br.com.promochef.backend.dto.LoginRequest;
import br.com.promochef.backend.dto.LoginResponse;
import br.com.promochef.backend.models.Usuario;
import br.com.promochef.backend.security.JwtUtil;
import br.com.promochef.backend.services.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            Usuario usuario = (Usuario) userDetails;

            String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getTipo().name());

            LoginResponse response = new LoginResponse(
                    token,
                    28800L, // 8 horas em segundos
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getTipo().name()
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Email ou senha inválidos");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno no servidor");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = (Usuario) userDetails;

        return ResponseEntity.ok(new LoginResponse(
                null,
                null,
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipo().name()
        ));
    }
}
