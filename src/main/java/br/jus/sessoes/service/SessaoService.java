package br.jus.sessoes.service;

import br.jus.sessoes.domain.Documento;
import br.jus.sessoes.domain.Sessao;
import br.jus.sessoes.repository.SessaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class SessaoService {

    private final SessaoRepository repository;
    private final Path uploadDir;

    public SessaoService(SessaoRepository repository, @Value("${sessoes.upload-dir}") String uploadDir) {
        this.repository = repository;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public List<Sessao> listar(String busca, LocalDate dataInicial, LocalDate dataFinal) {
        return repository.findAll().stream()
                .filter(sessao -> dentroDoPeriodo(sessao, dataInicial, dataFinal))
                .filter(sessao -> contemBusca(sessao, busca))
                .sorted(Comparator.comparing(Sessao::getData).thenComparing(Sessao::getHorario))
                .toList();
    }

    public Sessao buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sessao nao encontrada."));
    }

    public Sessao salvar(Sessao sessao) {
        if (sessao.getDocumentos() == null) {
            sessao.setDocumentos(new Documento());
        }
        if (sessao.getStatus() == null || sessao.getStatus().isBlank()) {
            sessao.setStatus("ATIVA");
        }
        if (sessao.getMotivoCancelamento() == null) {
            sessao.setMotivoCancelamento("");
        }
        return repository.save(sessao);
    }

    public Sessao atualizar(Long id, Sessao dados) {
        Sessao sessao = buscar(id);
        sessao.setTurma(dados.getTurma());
        sessao.setData(dados.getData());
        sessao.setHorario(dados.getHorario());
        sessao.setSala(dados.getSala());
        sessao.setProcurador(dados.getProcurador());
        return repository.save(sessao);
    }

    public Sessao cancelar(Long id, String motivo) {
        Sessao sessao = buscar(id);
        sessao.setStatus("CANCELADA");
        sessao.setMotivoCancelamento(motivo == null || motivo.isBlank() ? "Motivo nao informado." : motivo.trim());
        return repository.save(sessao);
    }

    public Sessao reativar(Long id) {
        Sessao sessao = buscar(id);
        sessao.setStatus("ATIVA");
        sessao.setMotivoCancelamento("");
        return repository.save(sessao);
    }

    public Sessao salvarDocumentos(Long id, MultipartFile pautaOrdinaria, MultipartFile aditamentos,
                                   MultipartFile pautaMesa, MultipartFile preferencias) throws IOException {
        Sessao sessao = buscar(id);
        Files.createDirectories(uploadDir);

        Documento documentos = sessao.getDocumentos() == null ? new Documento() : sessao.getDocumentos();
        if (temArquivo(pautaOrdinaria)) {
            documentos.setPautaOrdinaria(salvarArquivo(pautaOrdinaria));
        }
        if (temArquivo(aditamentos)) {
            documentos.setAditamentos(salvarArquivo(aditamentos));
        }
        if (temArquivo(pautaMesa)) {
            documentos.setPautaMesa(salvarArquivo(pautaMesa));
        }
        if (temArquivo(preferencias)) {
            documentos.setPreferencias(salvarArquivo(preferencias));
        }

        sessao.setDocumentos(documentos);
        return repository.save(sessao);
    }

    private boolean dentroDoPeriodo(Sessao sessao, LocalDate dataInicial, LocalDate dataFinal) {
        return (dataInicial == null || !sessao.getData().isBefore(dataInicial))
                && (dataFinal == null || !sessao.getData().isAfter(dataFinal));
    }

    private boolean contemBusca(Sessao sessao, String busca) {
        if (busca == null || busca.isBlank()) {
            return true;
        }

        String termo = normalizar(busca);
        String texto = normalizar(sessao.getTurma() + " " + sessao.getProcurador() + " " + sessao.getSala());
        return texto.contains(termo);
    }

    private boolean temArquivo(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String salvarArquivo(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() == null ? "documento.pdf" : file.getOriginalFilename();
        String nomeSeguro = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String nomeFinal = UUID.randomUUID() + "-" + nomeSeguro;
        Path destino = uploadDir.resolve(nomeFinal).normalize();
        file.transferTo(destino);
        return "/uploads/" + nomeFinal;
    }

    private String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT);
    }
}
