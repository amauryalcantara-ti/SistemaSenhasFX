package com.sistemasenhas.models;

import java.time.LocalDate;

public class Alocacao {
    private int id;
    private int usuarioId;
    private String nomeUsuario;
    private int guicheId;
    private String infoGuiche; // Numero + Localização
    private String turno;
    private LocalDate dataAlocacao;
    private boolean ativo;

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public int getGuicheId() {
        return guicheId;
    }

    public void setGuicheId(int guicheId) {
        this.guicheId = guicheId;
    }

    public String getInfoGuiche() {
        return infoGuiche;
    }

    public void setInfoGuiche(String infoGuiche) {
        this.infoGuiche = infoGuiche;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public LocalDate getDataAlocacao() {
        return dataAlocacao;
    }

    public void setDataAlocacao(LocalDate dataAlocacao) {
        this.dataAlocacao = dataAlocacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}