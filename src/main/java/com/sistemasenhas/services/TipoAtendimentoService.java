package com.sistemasenhas.services;

import com.sistemasenhas.database.DatabaseConnection;
import com.sistemasenhas.models.TipoAtendimento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipoAtendimentoService {

    // 1. Busca todos para o CRUD de cadastro (Global - Sem campos de estilo)
    public List<TipoAtendimento> listarTodos() throws SQLException {
        List<TipoAtendimento> lista = new ArrayList<>();
        String sql = "SELECT id, prefixo, descricao, prioridade, ativo, tempo_medio_estimado FROM tipos_atendimento ORDER BY descricao ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        return lista;
    }

    // 2. Busca serviços de um guichê trazendo o estilo vinculado ao local dele
    public List<TipoAtendimento> buscarPorGuiche(int guicheId) throws SQLException {
        List<TipoAtendimento> lista = new ArrayList<>();
        String sql = "SELECT ta.id, ta.prefixo, ta.descricao, ta.prioridade, ta.ativo, ta.tempo_medio_estimado, " +
                "ls.cor_fundo, ls.cor_texto, ls.caminho_icone, ls.formato_botao, ls.estilo_fonte, ls.tamanho_fonte " +
                "FROM tipos_atendimento ta " +
                "JOIN local_servicos ls ON ta.id = ls.servico_id " +
                "JOIN guiche_servicos gs ON ls.id = gs.local_servico_id " +
                "WHERE gs.guiche_id = ? AND ta.ativo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, guicheId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSetCompleto(rs));
                }
            }
        }
        return lista;
    }

    // NOVO MÉTODO: Busca serviços vinculados a um LOCAL específico
    public List<TipoAtendimento> buscarPorLocal(String localNome) throws SQLException {
        List<TipoAtendimento> lista = new ArrayList<>();
        String sql = "SELECT ta.id, ta.prefixo, ta.descricao, ta.prioridade, ta.ativo, ta.tempo_medio_estimado, " +
                "ls.cor_fundo, ls.cor_texto, ls.caminho_icone, ls.formato_botao, ls.estilo_fonte, ls.tamanho_fonte " +
                "FROM tipos_atendimento ta " +
                "JOIN local_servicos ls ON ta.id = ls.servico_id " +
                "WHERE ls.localizacao = ? AND ls.ativo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, localNome);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSetCompleto(rs));
                }
            }
        }
        return lista;
    }

    // 3. Salvar ou Atualizar o cadastro base do serviço
    public void salvar(TipoAtendimento tipo) throws SQLException {
        String sql;
        if (tipo.getId() == 0) {
            sql = "INSERT INTO tipos_atendimento (prefixo, descricao, prioridade, ativo, tempo_medio_estimado) " +
                    "VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE tipos_atendimento SET prefixo=?, descricao=?, prioridade=?, ativo=?, tempo_medio_estimado=? WHERE id=?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipo.getPrefixo());
            stmt.setString(2, tipo.getDescricao());
            stmt.setInt(3, tipo.getPrioridade());
            stmt.setBoolean(4, tipo.isAtivo());
            stmt.setInt(5, tipo.getTempoMedioEstimado());

            if (tipo.getId() != 0) {
                stmt.setInt(6, tipo.getId());
            }

            stmt.executeUpdate();
        }
    }

    // Mapeamento básico (Tipos Atendimento Global)
    private TipoAtendimento mapearResultSet(ResultSet rs) throws SQLException {
        TipoAtendimento t = new TipoAtendimento();
        t.setId(rs.getInt("id"));
        t.setPrefixo(rs.getString("prefixo"));
        t.setDescricao(rs.getString("descricao"));
        t.setPrioridade(rs.getInt("prioridade"));
        t.setAtivo(rs.getBoolean("ativo"));
        t.setTempoMedioEstimado(rs.getInt("tempo_medio_estimado"));
        return t;
    }

    // Mapeamento completo (Incluindo estilo vindo da local_servicos)
    private TipoAtendimento mapearResultSetCompleto(ResultSet rs) throws SQLException {
        TipoAtendimento t = mapearResultSet(rs);
        t.setCorFundo(rs.getString("cor_fundo"));
        t.setCorTexto(rs.getString("cor_texto"));
        t.setCaminhoIcone(rs.getString("caminho_icone"));
        t.setFormatoBotao(rs.getString("formato_botao"));
        t.setEstiloFonte(rs.getString("estilo_fonte"));
        t.setTamanhoFonte(rs.getInt("tamanho_fonte"));
        return t;
    }
}