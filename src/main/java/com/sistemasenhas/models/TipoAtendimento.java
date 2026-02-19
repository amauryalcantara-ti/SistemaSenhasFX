package com.sistemasenhas.models;

import java.time.LocalDateTime;

public class TipoAtendimento {
    private int id;
    private String prefixo;
    private String descricao;
    private int prioridade;
    private String corFundo;
    private String corTexto;
    private String caminhoIcone;

    // Novos campos para o estilo profissional do botão
    private String formatoBotao; // Ex: "REDONDO", "QUADRADO", "ARREDONDADO"
    private String estiloFonte; // Ex: "Segoe UI", "Arial"
    private int tamanhoFonte;

    private boolean ativo;
    private int tempoMedioEstimado;
    private Integer criadoPor;
    private LocalDateTime dataCriacao;

    // Construtor
    public TipoAtendimento() {
    }

    // Getters e Setters (Mantendo os seus e adicionando os novos)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPrefixo() {
        return prefixo;
    }

    public void setPrefixo(String prefixo) {
        this.prefixo = prefixo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    public String getCorFundo() {
        return corFundo;
    }

    public void setCorFundo(String corFundo) {
        this.corFundo = corFundo;
    }

    public String getCorTexto() {
        return corTexto;
    }

    public void setCorTexto(String corTexto) {
        this.corTexto = corTexto;
    }

    public String getCaminhoIcone() {
        return caminhoIcone;
    }

    public void setCaminhoIcone(String caminhoIcone) {
        this.caminhoIcone = caminhoIcone;
    }

    public String getFormatoBotao() {
        return formatoBotao;
    }

    public void setFormatoBotao(String formatoBotao) {
        this.formatoBotao = formatoBotao;
    }

    public String getEstiloFonte() {
        return estiloFonte;
    }

    public void setEstiloFonte(String estiloFonte) {
        this.estiloFonte = estiloFonte;
    }

    public int getTamanhoFonte() {
        return tamanhoFonte;
    }

    public void setTamanhoFonte(int tamanhoFonte) {
        this.tamanhoFonte = tamanhoFonte;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getTempoMedioEstimado() {
        return tempoMedioEstimado;
    }

    public void setTempoMedioEstimado(int tempoMedioEstimado) {
        this.tempoMedioEstimado = tempoMedioEstimado;
    }

    public Integer getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(Integer criadoPor) {
        this.criadoPor = criadoPor;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Override
    public String toString() {
        return prefixo + " - " + descricao;
    }
}