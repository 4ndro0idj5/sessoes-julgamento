package br.jus.sessoes.controller;

import br.jus.sessoes.dto.UsuarioAtualizacaoRequest;
import br.jus.sessoes.dto.UsuarioCriacaoRequest;
import br.jus.sessoes.dto.UsuarioResponse;
import br.jus.sessoes.security.UsuarioAutenticado;
import br.jus.sessoes.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioCriacaoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAtualizacaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAtual
    ) {
        return service.atualizar(id, request, usuarioAtual);
    }
}
