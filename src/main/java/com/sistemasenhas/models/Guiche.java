package com.sistemasenhas.models;

import java.time.LocalDateTime;

public class Guiche {
    private int id;
    private String numero;
    private String descricao;
    private String localizacao;
    private boolean ativo; // Status do cadastro (Ativo/Inativo)
    private boolean alocado; // Status operacional (Alocado/Livre)
    private LocalDateTime dataCadastro;
    private int cadastradoPor;

    // --- NOVOS CAMPOS PARA VINCULAR O OPERADOR ---
    private int operadorId; // ID do usuário alocado (operador_id no BD) 👤
    private String nomeOperador; // Nome vindo da coluna 'usuario' da tabela 'usuarios' 🏷️

    public Guiche() {
    }

    // Getters e Setters Originais
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isAlocado() {
        return alocado;
    }

    public void setAlocado(boolean alocado) {
        this.alocado = alocado;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public int getCadastradoPor() {
        return cadastradoPor;
    }

    public void setCadastradoPor(int cadastradoPor) {
        this.cadastradoPor = cadastradoPor;
    }

    // --- NOVOS GETTERS E SETTERS ---
    public int getOperadorId() {
        return operadorId;
    }

    public void setOperadorId(int operadorId) {
        this.operadorId = operadorId;
    }

    public String getNomeOperador() {
        return nomeOperador;
    }

    public void setNomeOperador(String nomeOperador) {
        this.nomeOperador = nomeOperador;
    }

    @Override
    public String toString() {
        return "Guichê " + numero + (descricao != null ? " - " + descricao : "");
    }
}