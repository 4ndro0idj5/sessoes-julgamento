package br.jus.sessoes.security;

import br.jus.sessoes.domain.PerfilUsuario;
import br.jus.sessoes.domain.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "segredo-de-teste-longo-e-exclusivo";

    @Test
    void devePreservarIdentidadeLoginEPerfil() {
        JwtService service = new JwtService(SECRET, 3_600_000, new ObjectMapper());
        Usuario usuario = usuario();

        String token = service.gerarToken(usuario);
        UsuarioAutenticado autenticado = service.validarToken(token).orElseThrow();

        assertEquals(42L, autenticado.id());
        assertEquals("gestor", autenticado.login());
        assertEquals(PerfilUsuario.ADMIN, autenticado.perfil());
        assertEquals("gestor", autenticado.getName());
    }

    @Test
    void deveRejeitarTokenAlterado() {
        JwtService service = new JwtService(SECRET, 3_600_000, new ObjectMapper());
        String token = service.gerarToken(usuario());
        String tokenAlterado = token + (char) 46 + token;

        assertTrue(service.validarToken(tokenAlterado).isEmpty());
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtService service = new JwtService(SECRET, -1_000, new ObjectMapper());

        Optional<UsuarioAutenticado> resultado = service.validarToken(service.gerarToken(usuario()));

        assertTrue(resultado.isEmpty());
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(42L);
        usuario.setLogin("gestor");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        return usuario;
    }
}
