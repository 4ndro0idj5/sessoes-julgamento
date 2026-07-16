package br.jus.sessoes.security;

import br.jus.sessoes.domain.PerfilUsuario;

import java.security.Principal;

public record UsuarioAutenticado(Long id, String login, PerfilUsuario perfil) implements Principal {

    @Override
    public String getName() {
        return login;
    }
}
