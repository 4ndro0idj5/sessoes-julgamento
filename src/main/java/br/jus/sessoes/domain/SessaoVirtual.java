package br.jus.sessoes.domain;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
public class SessaoVirtual extends Sessao {

    @NotNull
    private LocalDate dataInicial;

    @NotNull
    private LocalDate dataFinal;

    @Override
    public LocalDate getDataInicial() { return dataInicial; }
    public void setDataInicial(LocalDate dataInicial) { this.dataInicial = dataInicial; }
    @Override
    public LocalDate getDataFinal() { return dataFinal; }
    public void setDataFinal(LocalDate dataFinal) { this.dataFinal = dataFinal; }

    @AssertTrue(message = "A data final nao pode ser anterior a data inicial.")
    public boolean isPeriodoValido() {
        return dataInicial == null || dataFinal == null || !dataFinal.isBefore(dataInicial);
    }
}