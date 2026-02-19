package com.sistemasenhas.models;

public class LocalAtendimento {
    private int id;
    private String nome;
    private String descricao;
    private boolean ativo;

    public LocalAtendimento() {}

    public LocalAtendimento(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    // O toString é essencial para o ComboBox exibir apenas o nome
    @Override
    public String toString() {
        return nome;
    }
}