package br.jus.sessoes.dto;

import br.jus.sessoes.domain.PerfilUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizacaoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 80) String login,
        @Size(min = 8, max = 72) String senha,
        @NotNull PerfilUsuario perfil,
        @NotNull Boolean ativo
) {
}
