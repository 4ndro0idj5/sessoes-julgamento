package br.jus.sessoes.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank String usuario,
        @NotBlank String senha
) {
}
