package br.jus.sessoes.dto;

import br.jus.sessoes.domain.PerfilUsuario;

public record AuthResponse(
        boolean autenticado,
        String usuario,
        PerfilUsuario perfil,
        String token,
        String tipo,
        long expiraEm
) {
}
