package br.jus.sessoes.repository;

import br.jus.sessoes.domain.PerfilUsuario;
import br.jus.sessoes.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLoginIgnoreCase(String login);

    boolean existsByLoginIgnoreCase(String login);

    List<Usuario> findAllByOrderByNomeAsc();

    long countByPerfilAndAtivo(PerfilUsuario perfil, boolean ativo);
}
