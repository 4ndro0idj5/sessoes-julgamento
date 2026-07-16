package br.jus.sessoes.service;

import br.jus.sessoes.domain.PerfilUsuario;
import br.jus.sessoes.domain.Usuario;
import br.jus.sessoes.dto.UsuarioAtualizacaoRequest;
import br.jus.sessoes.dto.UsuarioCriacaoRequest;
import br.jus.sessoes.dto.UsuarioResponse;
import br.jus.sessoes.repository.UsuarioRepository;
import br.jus.sessoes.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAllByOrderByNomeAsc().stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @Transactional
    public UsuarioResponse criar(UsuarioCriacaoRequest request) {
        String login = normalizarLogin(request.login());
        validarLoginDisponivel(login, null);

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setLogin(login);
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(true);

        return UsuarioResponse.de(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(
            Long id,
            UsuarioAtualizacaoRequest request,
            UsuarioAutenticado usuarioAtual
    ) {
        Usuario usuario = buscar(id);
        String login = normalizarLogin(request.login());
        validarLoginDisponivel(login, id);
        validarAlteracaoDeAcesso(usuario, request, usuarioAtual);

        usuario.setNome(request.nome().trim());
        usuario.setLogin(login);
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(request.ativo());

        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        }

        return UsuarioResponse.de(repository.save(usuario));
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));
    }

    private void validarLoginDisponivel(String login, Long idAtual) {
        repository.findByLoginIgnoreCase(login)
                .filter(usuario -> !usuario.getId().equals(idAtual))
                .ifPresent(usuario -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Login ja cadastrado.");
                });
    }

    private void validarAlteracaoDeAcesso(
            Usuario usuario,
            UsuarioAtualizacaoRequest request,
            UsuarioAutenticado usuarioAtual
    ) {
        boolean alterandoProprioAcesso = usuario.getId().equals(usuarioAtual.id())
                && (usuario.getPerfil() != request.perfil() || usuario.isAtivo() != request.ativo());

        if (alterandoProprioAcesso) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nao e permitido alterar o proprio perfil ou status."
            );
        }

        boolean deixaDeSerAdminAtivo = usuario.isAtivo()
                && usuario.getPerfil() == PerfilUsuario.ADMIN
                && (!request.ativo() || request.perfil() != PerfilUsuario.ADMIN);

        if (deixaDeSerAdminAtivo
                && repository.countByPerfilAndAtivo(PerfilUsuario.ADMIN, true) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O sistema deve manter pelo menos um administrador ativo."
            );
        }
    }

    private String normalizarLogin(String login) {
        return login.trim().toLowerCase(Locale.ROOT);
    }
}
