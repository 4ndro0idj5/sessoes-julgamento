package br.jus.sessoes.service;

import br.jus.sessoes.domain.PerfilUsuario;
import br.jus.sessoes.domain.Usuario;
import br.jus.sessoes.dto.UsuarioAtualizacaoRequest;
import br.jus.sessoes.dto.UsuarioCriacaoRequest;
import br.jus.sessoes.dto.UsuarioResponse;
import br.jus.sessoes.repository.UsuarioRepository;
import br.jus.sessoes.security.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private UsuarioRepository repository;
    private PasswordEncoder passwordEncoder;
    private UsuarioService service;

    @BeforeEach
    void configurar() {
        repository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UsuarioService(repository, passwordEncoder);
    }

    @Test
    void deveCriarGestorComSenhaCodificada() {
        when(repository.findByLoginIgnoreCase("gestor@orgao.br")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha-segura")).thenReturn("hash-bcrypt");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(2L);
            return usuario;
        });

        UsuarioResponse response = service.criar(new UsuarioCriacaoRequest(
                "Gestor de Pautas",
                " GESTOR@ORGAO.BR ",
                "senha-segura",
                PerfilUsuario.GESTOR
        ));

        assertEquals("gestor@orgao.br", response.login());
        assertEquals(PerfilUsuario.GESTOR, response.perfil());
        assertEquals(true, response.ativo());
        verify(passwordEncoder).encode("senha-segura");
    }

    @Test
    void deveImpedirAlteracaoDoProprioPerfil() {
        Usuario admin = admin(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        when(repository.findByLoginIgnoreCase(admin.getLogin())).thenReturn(Optional.of(admin));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.atualizar(
                        1L,
                        new UsuarioAtualizacaoRequest(
                                admin.getNome(), admin.getLogin(), null, PerfilUsuario.GESTOR, true
                        ),
                        new UsuarioAutenticado(1L, admin.getLogin(), PerfilUsuario.ADMIN)
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deveManterPeloMenosUmAdministradorAtivo() {
        Usuario admin = admin(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        when(repository.findByLoginIgnoreCase(admin.getLogin())).thenReturn(Optional.of(admin));
        when(repository.countByPerfilAndAtivo(PerfilUsuario.ADMIN, true)).thenReturn(1L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.atualizar(
                        1L,
                        new UsuarioAtualizacaoRequest(
                                admin.getNome(), admin.getLogin(), null, PerfilUsuario.ADMIN, false
                        ),
                        new UsuarioAutenticado(99L, "outro-admin", PerfilUsuario.ADMIN)
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deveRejeitarLoginDuplicado() {
        Usuario existente = admin(1L);
        when(repository.findByLoginIgnoreCase(existente.getLogin())).thenReturn(Optional.of(existente));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.criar(new UsuarioCriacaoRequest(
                        "Outro", existente.getLogin(), "senha-segura", PerfilUsuario.GESTOR
                ))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    private Usuario admin(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Administrador");
        usuario.setLogin("admin@orgao.br");
        usuario.setSenhaHash("hash");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);
        return usuario;
    }
}
