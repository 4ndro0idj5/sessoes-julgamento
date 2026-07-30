package br.jus.sessoes.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SessaoVirtualTest {

    @Test
    void deveAceitarPeriodoSemCalcularOuArmazenarDuracao() {
        SessaoVirtual sessao = new SessaoVirtual();
        sessao.setDataInicial(LocalDate.of(2026, 8, 13));
        sessao.setDataFinal(LocalDate.of(2026, 8, 20));

        assertThat(sessao.isPeriodoValido()).isTrue();
    }

    @Test
    void deveRejeitarDataFinalAnteriorAInicial() {
        SessaoVirtual sessao = new SessaoVirtual();
        sessao.setDataInicial(LocalDate.of(2026, 8, 20));
        sessao.setDataFinal(LocalDate.of(2026, 8, 13));

        assertThat(sessao.isPeriodoValido()).isFalse();
    }
}