package br.jus.sessoes.repository;

import br.jus.sessoes.domain.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {

    List<Sessao> findByDataBetweenOrderByDataAscHorarioAsc(LocalDate inicio, LocalDate fim);
}
