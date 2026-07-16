package br.jus.sessoes.dto;

import br.jus.sessoes.domain.PerfilUsuario;
import br.jus.sessoes.domain.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String login,
        PerfilUsuario perfil,
        boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm()
        );
    }
}
