package br.jus.sessoes.service;

import br.jus.sessoes.domain.SessaoPresencial;
import br.jus.sessoes.domain.SessaoVirtual;
import br.jus.sessoes.repository.SessaoRepository;
import org.junit.jupiter.api.Test;
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
        SessaoPresencial sessao = new SessaoPresencial();
        sessao.setTurma("5ª TESP");
        sessao.setData(LocalDate.of(2026, 7, 1));
        sessao.setHorario(LocalTime.of(14, 0));
        sessao.setProcurador("MAURÍCIO RIBEIRO MANSO");
        sessao.setLocal("5º andar, sala 2");
        when(repository.findAll()).thenReturn(List.of(sessao));

        assertThat(service.listar(busca, null, null)).containsExactly(sessao);
    }

    @Test
    void deveEncontrarVirtualQuandoPeriodosSeSobrepoem() {
        SessaoRepository repository = mock(SessaoRepository.class);
        SessaoService service = new SessaoService(repository, "uploads");
        SessaoVirtual sessao = new SessaoVirtual();
        sessao.setTurma("5ª TESP");
        sessao.setDataInicial(LocalDate.of(2026, 8, 13));
        sessao.setDataFinal(LocalDate.of(2026, 8, 20));
        when(repository.findAll()).thenReturn(List.of(sessao));

        assertThat(service.listar(null, LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 16), "VIRTUAL"))
                .containsExactly(sessao);
        assertThat(service.listar(null, null, null, "PRESENCIAL")).isEmpty();
    }
}