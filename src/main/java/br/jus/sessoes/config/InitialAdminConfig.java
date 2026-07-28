package br.jus.sessoes.config;

import br.jus.sessoes.domain.PerfilUsuario;
import br.jus.sessoes.domain.Usuario;
import br.jus.sessoes.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@Profile("prod")
public class InitialAdminConfig implements ApplicationRunner {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String nome;
    private final String login;
    private final String senha;

    public InitialAdminConfig(UsuarioRepository repository, PasswordEncoder passwordEncoder,
            @Value("${INITIAL_ADMIN_NAME:}") String nome,
            @Value("${INITIAL_ADMIN_LOGIN:}") String login,
            @Value("${INITIAL_ADMIN_PASSWORD:}") String senha) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) return;
        if (nome.isBlank() || login.isBlank() || senha.isBlank()) {
            throw new IllegalStateException("Banco sem usuarios: defina INITIAL_ADMIN_NAME, INITIAL_ADMIN_LOGIN e INITIAL_ADMIN_PASSWORD.");
        }
        if (senha.length() < 12) {
            throw new IllegalStateException("INITIAL_ADMIN_PASSWORD deve ter pelo menos 12 caracteres.");
        }
        Usuario admin = new Usuario();
        admin.setNome(nome.trim());
        admin.setLogin(login.trim().toLowerCase(Locale.ROOT));
        admin.setSenhaHash(passwordEncoder.encode(senha));
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);
        repository.save(admin);
    }
}