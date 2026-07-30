package br.jus.sessoes.service;

import br.jus.sessoes.domain.Documento;
import br.jus.sessoes.domain.Sessao;
import br.jus.sessoes.domain.SessaoPresencial;
import br.jus.sessoes.domain.SessaoVirtual;
import br.jus.sessoes.repository.SessaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
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

    public List<Sessao> listar(String busca, LocalDate dataInicial, LocalDate dataFinal, String tipo) {
        return repository.findAll().stream()
                .filter(sessao -> dentroDoPeriodo(sessao, dataInicial, dataFinal))
                .filter(sessao -> contemBusca(sessao, busca))
                .filter(sessao -> correspondeAoTipo(sessao, tipo))
                .sorted(Comparator.comparing(Sessao::getDataInicial)
                        .thenComparing(this::horarioParaOrdenacao))
                .toList();
    }

    public List<Sessao> listar(String busca, LocalDate dataInicial, LocalDate dataFinal) {
        return listar(busca, dataInicial, dataFinal, null);
    }

    public Sessao buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sessao nao encontrada."));
    }

    public Sessao salvar(Sessao sessao) {
        prepararCamposComuns(sessao);
        return repository.save(sessao);
    }

    public Sessao atualizar(Long id, Sessao dados) {
        Sessao sessao = buscar(id);
        if (!sessao.getClass().equals(dados.getClass())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A modalidade da sessao nao pode ser alterada.");
        }

        sessao.setTurma(dados.getTurma());
        if (sessao instanceof SessaoPresencial atual && dados instanceof SessaoPresencial novosDados) {
            atual.setData(novosDados.getData());
            atual.setHorario(novosDados.getHorario());
            atual.setLocal(novosDados.getLocal());
            atual.setProcurador(novosDados.getProcurador());
        } else if (sessao instanceof SessaoVirtual atual && dados instanceof SessaoVirtual novosDados) {
            atual.setDataInicial(novosDados.getDataInicial());
            atual.setDataFinal(novosDados.getDataFinal());
        }
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
        Documento documentos = sessao.getDocumentos();
        if (temArquivo(pautaOrdinaria)) documentos.setPautaOrdinaria(salvarArquivo(pautaOrdinaria));
        if (temArquivo(aditamentos)) documentos.setAditamentos(salvarArquivo(aditamentos));
        if (temArquivo(pautaMesa)) documentos.setPautaMesa(salvarArquivo(pautaMesa));
        if (temArquivo(preferencias)) documentos.setPreferencias(salvarArquivo(preferencias));
        sessao.setDocumentos(documentos);
        return repository.save(sessao);
    }

    private void prepararCamposComuns(Sessao sessao) {
        if (sessao.getDocumentos() == null) sessao.setDocumentos(new Documento());
        if (sessao.getStatus() == null || sessao.getStatus().isBlank()) sessao.setStatus("ATIVA");
        if (sessao.getMotivoCancelamento() == null) sessao.setMotivoCancelamento("");
    }

    private boolean dentroDoPeriodo(Sessao sessao, LocalDate inicio, LocalDate fim) {
        return (inicio == null || !sessao.getDataFinal().isBefore(inicio))
                && (fim == null || !sessao.getDataInicial().isAfter(fim));
    }

    private boolean contemBusca(Sessao sessao, String busca) {
        if (busca == null || busca.isBlank()) return true;
        StringBuilder texto = new StringBuilder(sessao.getTurma());
        if (sessao instanceof SessaoPresencial presencial) {
            texto.append(' ').append(presencial.getProcurador()).append(' ').append(presencial.getLocal());
        }
        return normalizar(texto.toString()).contains(normalizar(busca));
    }

    private boolean correspondeAoTipo(Sessao sessao, String tipo) {
        if (tipo == null || tipo.isBlank() || "TODAS".equalsIgnoreCase(tipo)) return true;
        return ("PRESENCIAL".equalsIgnoreCase(tipo) && sessao instanceof SessaoPresencial)
                || ("VIRTUAL".equalsIgnoreCase(tipo) && sessao instanceof SessaoVirtual);
    }

    private LocalTime horarioParaOrdenacao(Sessao sessao) {
        return sessao instanceof SessaoPresencial presencial ? presencial.getHorario() : LocalTime.MIN;
    }

    private boolean temArquivo(MultipartFile file) { return file != null && !file.isEmpty(); }

    private String salvarArquivo(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() == null ? "documento.pdf" : file.getOriginalFilename();
        String nomeSeguro = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String nomeFinal = UUID.randomUUID() + "-" + nomeSeguro;
        Path destino = uploadDir.resolve(nomeFinal).normalize();
        file.transferTo(destino);
        return "/uploads/" + nomeFinal;
    }

    private String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT)
                .replaceAll("\\b(\\d+)\\s*[aªº]?\\s*(?:turma|tesp)\\b", "$1 tesp")
                .replaceAll("\\s+", " ").trim();
    }
}