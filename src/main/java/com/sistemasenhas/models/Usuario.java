package com.sistemasenhas.models;

import java.time.LocalDateTime;

public class Usuario {
    private int id;
    private String nomeCompleto;
    private String usuario;
    private String senhaTexto;
    private String senhaHash;
    private String senhaTemporaria;
    private boolean primeiroLogin;
    private int tipoUsuarioId;
    private String email;
    private boolean ativo;
    private LocalDateTime dataCriacao;
    private Integer criadoPor;
    private LocalDateTime dataUltimoLogin;
    private LocalDateTime dataExpiracaoSenha;
    private boolean bloqueado;
    private int tentativasLogin;
    private LocalDateTime ultimaTrocaSenha;
    private String historicoSenhas;
    private int nivelPermissao;
    private boolean permitirEscolhaArea; // Campo da linha 308 do SQL

    public Usuario() {
    }

    // Método necessário para o ComboBox exibir o login (usuario) na tela
    @Override
    public String toString() {
        return this.usuario != null ? this.usuario : "";
    }

    public int getPrimeiroLogin() {
        return primeiroLogin ? 1 : 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenhaTexto() {
        return senhaTexto;
    }

    public void setSenhaTexto(String senhaTexto) {
        this.senhaTexto = senhaTexto;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getSenhaTemporaria() {
        return senhaTemporaria;
    }

    public void setSenhaTemporaria(String senhaTemporaria) {
        this.senhaTemporaria = senhaTemporaria;
    }

    public boolean isPrimeiroLoginBoolean() {
        return primeiroLogin;
    }

    public void setPrimeiroLogin(boolean primeiroLogin) {
        this.primeiroLogin = primeiroLogin;
    }

    public void setPrimeiroLogin(int primeiroLogin) {
        this.primeiroLogin = primeiroLogin == 1;
    }

    public int getTipoUsuarioId() {
        return tipoUsuarioId;
    }

    public void setTipoUsuarioId(int tipoUsuarioId) {
        this.tipoUsuarioId = tipoUsuarioId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public void setAtivo(int ativo) {
        this.ativo = ativo == 1;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Integer getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(Integer criadoPor) {
        this.criadoPor = criadoPor;
    }

    public LocalDateTime getDataUltimoLogin() {
        return dataUltimoLogin;
    }

    public void setDataUltimoLogin(LocalDateTime dataUltimoLogin) {
        this.dataUltimoLogin = dataUltimoLogin;
    }

    public LocalDateTime getDataExpiracaoSenha() {
        return dataExpiracaoSenha;
    }

    public void setDataExpiracaoSenha(LocalDateTime dataExpiracaoSenha) {
        this.dataExpiracaoSenha = dataExpiracaoSenha;
    }

    public void setDataExpiracaoSenha(String dataExpiracaoSenha) {
        if (dataExpiracaoSenha != null && !dataExpiracaoSenha.isEmpty()) {
            this.dataExpiracaoSenha = LocalDateTime.parse(dataExpiracaoSenha.replace(" ", "T"));
        }
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public void setBloqueado(int bloqueado) {
        this.bloqueado = bloqueado == 1;
    }

    public int getTentativasLogin() {
        return tentativasLogin;
    }

    public void setTentativasLogin(int tentativasLogin) {
        this.tentativasLogin = tentativasLogin;
    }

    public LocalDateTime getUltimaTrocaSenha() {
        return ultimaTrocaSenha;
    }

    public void setUltimaTrocaSenha(LocalDateTime ultimaTrocaSenha) {
        this.ultimaTrocaSenha = ultimaTrocaSenha;
    }

    public String getHistoricoSenhas() {
        return historicoSenhas;
    }

    public void setHistoricoSenhas(String historicoSenhas) {
        this.historicoSenhas = historicoSenhas;
    }

    public int getNivelPermissao() {
        return nivelPermissao;
    }

    public void setNivelPermissao(int nivelPermissao) {
        this.nivelPermissao = nivelPermissao;
    }

    // Novos métodos para o Checkbox (permitir_escolha_area)
    public boolean isPermitirEscolhaArea() {
        return permitirEscolhaArea;
    }

    public void setPermitirEscolhaArea(boolean permitirEscolhaArea) {
        this.permitirEscolhaArea = permitirEscolhaArea;
    }

    public void setPermitirEscolhaArea(int permitirEscolhaArea) {
        this.permitirEscolhaArea = permitirEscolhaArea == 1;
    }

    public int getPermitirEscolhaAreaInt() {
        return permitirEscolhaArea ? 1 : 0;
    }
}