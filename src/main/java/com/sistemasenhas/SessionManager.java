package com.sistemasenhas;

public class SessionManager {
    private static SessionManager instance;

    private String usuarioLogado;
    private String tipoUsuario;
    private String nomeUsuario;
    private int idUsuario;

    // Construtor privado para Singleton
    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Getters e Setters
    public String getUsuarioLogado() {
        return usuarioLogado;
    }

    public void setUsuarioLogado(String usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void clearSession() {
        this.usuarioLogado = null;
        this.tipoUsuario = null;
        this.nomeUsuario = null;
        this.idUsuario = 0;
    }

    // Método para logout (alias para clearSession)
    public void logout() {
        clearSession();
    }

    // Método auxiliar para verificar se usuário está logado
    public boolean isLoggedIn() {
        return usuarioLogado != null && !usuarioLogado.isEmpty();
    }

    // Método para obter tipo de usuário como inteiro com segurança
    public int getTipoUsuarioInt() {
        try {
            return (tipoUsuario != null) ? Integer.parseInt(tipoUsuario) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}