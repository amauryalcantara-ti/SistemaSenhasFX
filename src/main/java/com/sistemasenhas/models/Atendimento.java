package com.sistemasenhas.models;

import java.time.LocalDateTime;

public class Atendimento {
    private int id;
    private String senhaCodigo;
    private Integer tipoId;
    private Integer guicheId;
    private Integer operadorId;
    private LocalDateTime horaEmissao;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFim;
    private String status; // AGUARDANDO, CHAMADA, ATENDENDO, FINALIZADO, CANCELADO
    private Integer tempoEspera;
    private Integer tempoAtendimento;
    
    // Construtor
    public Atendimento() {}
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getSenhaCodigo() { return senhaCodigo; }
    public void setSenhaCodigo(String senhaCodigo) { this.senhaCodigo = senhaCodigo; }
    
    public Integer getTipoId() { return tipoId; }
    public void setTipoId(Integer tipoId) { this.tipoId = tipoId; }
    
    public Integer getGuicheId() { return guicheId; }
    public void setGuicheId(Integer guicheId) { this.guicheId = guicheId; }
    
    public Integer getOperadorId() { return operadorId; }
    public void setOperadorId(Integer operadorId) { this.operadorId = operadorId; }
    
    public LocalDateTime getHoraEmissao() { return horaEmissao; }
    public void setHoraEmissao(LocalDateTime horaEmissao) { this.horaEmissao = horaEmissao; }
    
    public LocalDateTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalDateTime horaInicio) { this.horaInicio = horaInicio; }
    
    public LocalDateTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalDateTime horaFim) { this.horaFim = horaFim; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getTempoEspera() { return tempoEspera; }
    public void setTempoEspera(Integer tempoEspera) { this.tempoEspera = tempoEspera; }
    
    public Integer getTempoAtendimento() { return tempoAtendimento; }
    public void setTempoAtendimento(Integer tempoAtendimento) { this.tempoAtendimento = tempoAtendimento; }
    
    @Override
    public String toString() {
        return senhaCodigo + " - " + status + " (Guichê: " + guicheId + ")";
    }
}