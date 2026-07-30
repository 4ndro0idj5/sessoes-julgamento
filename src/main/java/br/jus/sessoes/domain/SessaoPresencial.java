package br.jus.sessoes.domain;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class SessaoPresencial extends Sessao {

    @NotNull
    private LocalDate data;

    @NotNull
    private LocalTime horario;

    @NotBlank
    private String local;

    @NotBlank
    private String procurador;

    @Override
    public LocalDate getDataInicial() { return data; }

    @Override
    public LocalDate getDataFinal() { return data; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getProcurador() { return procurador; }
    public void setProcurador(String procurador) { this.procurador = procurador; }
}