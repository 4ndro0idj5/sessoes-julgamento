package br.jus.sessoes.service;

import br.jus.sessoes.domain.Usuario;
import br.jus.sessoes.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private static final String CREDENCIAIS_INVALIDAS = "Usuario ou senha invalidos.";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario autenticar(String login, String senha) {
        if (login == null || login.isBlank() || senha == null || senha.isBlank()) {
            throw new BadCredentialsException(CREDENCIAIS_INVALIDAS);
        }

        Usuario usuario = usuarioRepository.findByLoginIgnoreCase(login.trim())
                .orElseThrow(() -> new BadCredentialsException(CREDENCIAIS_INVALIDAS));

        if (!usuario.isAtivo()) {
            throw new DisabledException("Usuario inativo.");
        }

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new BadCredentialsException(CREDENCIAIS_INVALIDAS);
        }

        return usuario;
    }
}
