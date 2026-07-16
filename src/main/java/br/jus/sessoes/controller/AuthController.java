package br.jus.sessoes.controller;

import br.jus.sessoes.domain.Usuario;
import br.jus.sessoes.dto.AuthRequest;
import br.jus.sessoes.dto.AuthResponse;
import br.jus.sessoes.security.JwtService;
import br.jus.sessoes.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticacaoService autenticacaoService;
    private final JwtService jwtService;
    private final long expirationMs;

    public AuthController(
            AutenticacaoService autenticacaoService,
            JwtService jwtService,
            @Value("${sessoes.jwt.expiration-ms}") long expirationMs
    ) {
        this.autenticacaoService = autenticacaoService;
        this.jwtService = jwtService;
        this.expirationMs = expirationMs;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        try {
            Usuario usuario = autenticacaoService.autenticar(request.usuario(), request.senha());
            String token = jwtService.gerarToken(usuario);
            return new AuthResponse(true, usuario.getLogin(), usuario.getPerfil(), token, "Bearer", expirationMs);
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario ou senha invalidos.",
                    exception
            );
        }
    }
}
