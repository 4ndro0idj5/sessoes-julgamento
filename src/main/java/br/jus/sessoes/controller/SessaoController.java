package br.jus.sessoes.controller;

import br.jus.sessoes.domain.Sessao;
import br.jus.sessoes.dto.CancelamentoRequest;
import br.jus.sessoes.service.SessaoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessoes")
public class SessaoController {

    private final SessaoService service;

    public SessaoController(SessaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Sessao> listar(
            String busca,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal
    ) {
        return service.listar(busca, dataInicial, dataFinal);
    }

    @GetMapping("/{id}")
    public Sessao buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sessao criar(@Valid @RequestBody Sessao sessao) {
        return service.salvar(sessao);
    }

    @PutMapping("/{id}")
    public Sessao atualizar(@PathVariable Long id, @Valid @RequestBody Sessao sessao) {
        return service.atualizar(id, sessao);
    }

    @PatchMapping("/{id}/cancelar")
    public Sessao cancelar(@PathVariable Long id, @RequestBody CancelamentoRequest request) {
        return service.cancelar(id, request.motivo());
    }

    @PatchMapping("/{id}/reativar")
    public Sessao reativar(@PathVariable Long id) {
        return service.reativar(id);
    }

    @PostMapping("/{id}/documentos")
    public Sessao salvarDocumentos(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile pautaOrdinaria,
            @RequestParam(required = false) MultipartFile aditamentos,
            @RequestParam(required = false) MultipartFile pautaMesa,
            @RequestParam(required = false) MultipartFile preferencias
    ) throws IOException {
        return service.salvarDocumentos(id, pautaOrdinaria, aditamentos, pautaMesa, preferencias);
    }
}
