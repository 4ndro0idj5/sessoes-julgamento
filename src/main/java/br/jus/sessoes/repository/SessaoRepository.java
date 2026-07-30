package br.jus.sessoes.repository;

import br.jus.sessoes.domain.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {
}