package br.jus.sessoes.service;

import br.jus.sessoes.domain.Sessao;
import br.jus.sessoes.repository.SessaoRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessaoServiceTest {

    @ParameterizedTest
    @ValueSource(strings = {"5a turma", "5ª turma", "5 turma", "5a TESP", "5ª TESP"})
    void deveEncontrarTespPorVariacoesDoNomeDaTurma(String busca) {
        SessaoRepository repository = mock(SessaoRepository.class);
        SessaoService service = new SessaoService(repository, "uploads");

        Sessao sessao = new Sessao();
        sessao.setTurma("5ª TESP");
        sessao.setData(LocalDate.of(2026, 7, 1));
        sessao.setHorario(LocalTime.of(14, 0));
        sessao.setProcurador("MAURÍCIO RIBEIRO MANSO");
        sessao.setSala("5º andar, sala 2");

        when(repository.findAll()).thenReturn(List.of(sessao));

        assertThat(service.listar(busca, null, null)).containsExactly(sessao);
    }
}