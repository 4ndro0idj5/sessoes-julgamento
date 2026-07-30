package br.jus.sessoes.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SessaoPresencial.class, name = "PRESENCIAL"),
        @JsonSubTypes.Type(value = SessaoVirtual.class, name = "VIRTUAL")
})
public abstract class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String turma;

    @NotBlank
    private String status = "ATIVA";

    private String motivoCancelamento = "";

    @Embedded
    private Documento documentos = new Documento();

    public abstract LocalDate getDataInicial();
    public abstract LocalDate getDataFinal();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTurma() { return turma; }
    public void setTurma(String turma) { this.turma = turma; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    public Documento getDocumentos() {
        if (documentos == null) documentos = new Documento();
        return documentos;
    }

    public void setDocumentos(Documento documentos) { this.documentos = documentos; }
}