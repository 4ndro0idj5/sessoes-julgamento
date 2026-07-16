package br.jus.sessoes.service;

import br.jus.sessoes.domain.Usuario;
import br.jus.sessoes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutenticacaoServiceTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private AutenticacaoService service;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new AutenticacaoService(usuarioRepository, passwordEncoder);
    }

    @Test
    void deveAutenticarUsuarioAtivoComSenhaValida() {
        Usuario usuario = usuarioAtivo();
        when(usuarioRepository.findByLoginIgnoreCase("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha", usuario.getSenhaHash())).thenReturn(true);

        Usuario autenticado = service.autenticar(" admin ", "senha");

        assertSame(usuario, autenticado);
        verify(passwordEncoder).matches("senha", usuario.getSenhaHash());
    }

    @Test
    void deveRejeitarSenhaInvalida() {
        Usuario usuario = usuarioAtivo();
        when(usuarioRepository.findByLoginIgnoreCase("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorreta", usuario.getSenhaHash())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> service.autenticar("admin", "incorreta"));
    }

    @Test
    void deveRejeitarUsuarioInativoSemCompararSenha() {
        Usuario usuario = usuarioAtivo();
        usuario.setAtivo(false);
        when(usuarioRepository.findByLoginIgnoreCase("admin")).thenReturn(Optional.of(usuario));

        assertThrows(DisabledException.class, () -> service.autenticar("admin", "senha"));
    }

    private Usuario usuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setLogin("admin");
        usuario.setSenhaHash("$2a$10$hash");
        usuario.setAtivo(true);
        return usuario;
    }
}
