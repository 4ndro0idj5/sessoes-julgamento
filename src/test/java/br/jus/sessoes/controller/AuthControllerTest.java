package br.jus.sessoes.controller;

import br.jus.sessoes.domain.PerfilUsuario;
import br.jus.sessoes.domain.Usuario;
import br.jus.sessoes.dto.AuthRequest;
import br.jus.sessoes.dto.AuthResponse;
import br.jus.sessoes.security.JwtService;
import br.jus.sessoes.service.AutenticacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final AutenticacaoService autenticacaoService = mock(AutenticacaoService.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthController controller = new AuthController(autenticacaoService, jwtService, 3_600_000);

    @Test
    void deveEmitirTokenParaUsuarioAutenticado() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setLogin("gestor");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        when(autenticacaoService.autenticar("gestor", "senha")).thenReturn(usuario);
        when(jwtService.gerarToken(usuario)).thenReturn("jwt-assinado");

        AuthResponse response = controller.login(new AuthRequest("gestor", "senha"));

        assertEquals(true, response.autenticado());
        assertEquals("gestor", response.usuario());
        assertEquals("jwt-assinado", response.token());
        assertEquals("Bearer", response.tipo());
        assertEquals(3_600_000, response.expiraEm());
    }

    @Test
    void deveResponderUnauthorizedParaCredenciaisInvalidas() {
        when(autenticacaoService.autenticar("gestor", "incorreta"))
                .thenThrow(new BadCredentialsException("Credenciais invalidas."));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.login(new AuthRequest("gestor", "incorreta"))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Usuario ou senha invalidos.", exception.getReason());
    }
}
