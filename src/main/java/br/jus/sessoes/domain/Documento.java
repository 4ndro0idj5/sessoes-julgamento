package br.jus.sessoes.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Documento {

    private String pautaOrdinaria;
    private String aditamentos;
    private String pautaMesa;
    private String preferencias;

    public String getPautaOrdinaria() {
        return pautaOrdinaria;
    }

    public void setPautaOrdinaria(String pautaOrdinaria) {
        this.pautaOrdinaria = pautaOrdinaria;
    }

    public String getAditamentos() {
        return aditamentos;
    }

    public void setAditamentos(String aditamentos) {
        this.aditamentos = aditamentos;
    }

    public String getPautaMesa() {
        return pautaMesa;
    }

    public void setPautaMesa(String pautaMesa) {
        this.pautaMesa = pautaMesa;
    }

    public String getPreferencias() {
        return preferencias;
    }

    public void setPreferencias(String preferencias) {
        this.preferencias = preferencias;
    }
}
