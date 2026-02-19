package com.sistemasenhas.services;

import com.sistemasenhas.database.DatabaseConnection;
import com.sistemasenhas.models.Alocacao;
import com.sistemasenhas.models.Guiche;
import com.sistemasenhas.models.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlocacaoService {

    public List<Usuario> listarAtendentes() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, usuario FROM usuarios WHERE tipo_usuario_id <> 1 AND ativo = 1 ORDER BY usuario";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsuario(rs.getString("usuario"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Guiche> listarGuichesDisponiveis(String local) {
        List<Guiche> lista = new ArrayList<>();
        // CORREÇÃO: O filtro por local agora verifica APENAS se o guichê está ATIVO.
        // Isso permite listar todos os guichês aptos a receberem alocação naquele
        // local.
        StringBuilder sql = new StringBuilder(
                "SELECT id, numero, localizacao FROM guiches WHERE ativo = 1 ");

        if (local != null && !local.isEmpty()) {
            sql.append("AND localizacao = ? ");
        }
        sql.append("ORDER BY numero");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            if (local != null && !local.isEmpty()) {
                pstmt.setString(1, local);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Guiche g = new Guiche();
                    g.setId(rs.getInt("id"));
                    g.setNumero(rs.getString("numero"));
                    g.setLocalizacao(rs.getString("localizacao"));
                    lista.add(g);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Guiche> listarGuichesComOperador(String local) {
        List<Guiche> lista = new ArrayList<>();
        // Mantém a lógica de exibir apenas quem está efetivamente trabalhando (alocado
        // = 1)
        String sql = "SELECT g.*, u.usuario as nome_atendente " +
                "FROM guiches g " +
                "INNER JOIN usuarios u ON g.operador_id = u.id " +
                "WHERE g.localizacao = ? AND g.alocado = 1 ORDER BY g.numero";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, local);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Guiche g = new Guiche();
                    g.setId(rs.getInt("id"));
                    g.setNumero(rs.getString("numero"));
                    g.setLocalizacao(rs.getString("localizacao"));
                    g.setAtivo(rs.getBoolean("ativo"));
                    g.setAlocado(rs.getBoolean("alocado"));
                    g.setOperadorId(rs.getInt("operador_id"));
                    g.setNomeOperador(rs.getString("nome_atendente") != null ? rs.getString("nome_atendente") : "");
                    lista.add(g);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Alocacao> listarAlocacoesHoje() {
        List<Alocacao> lista = new ArrayList<>();
        // Removido o campo 'status' que não existe na tabela
        String sql = "SELECT a.*, u.usuario as nome_usuario, g.numero as num_guiche, g.localizacao " +
                "FROM alocacoes_diarias a " +
                "JOIN usuarios u ON a.usuario_id = u.id " +
                "JOIN guiches g ON a.guiche_id = g.id " +
                "WHERE a.data_alocacao = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Alocacao a = new Alocacao();
                a.setId(rs.getInt("id"));
                a.setUsuarioId(rs.getInt("usuario_id"));
                a.setGuicheId(rs.getInt("guiche_id"));
                a.setNomeUsuario(rs.getString("nome_usuario"));
                a.setInfoGuiche("Guichê " + rs.getString("num_guiche") + " - " + rs.getString("localizacao"));
                lista.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean salvarAlocacao(Alocacao a, int masterId) {
        // Removido o campo 'status' do INSERT
        String sqlAlocacao = "INSERT INTO alocacoes_diarias (usuario_id, guiche_id, data_alocacao, supervisor_id) VALUES (?, ?, ?, ?)";
        String sqlUpdateGuiche = "UPDATE guiches SET alocado = 1, alocado_por = ?, operador_id = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlAlocacao)) {
                pstmt.setInt(1, a.getUsuarioId());
                pstmt.setInt(2, a.getGuicheId());
                pstmt.setDate(3, Date.valueOf(a.getDataAlocacao()));
                pstmt.setInt(4, masterId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateGuiche)) {
                pstmt.setInt(1, masterId);
                pstmt.setInt(2, a.getUsuarioId());
                pstmt.setInt(3, a.getGuicheId());
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean desalocarGuiche(int guicheId) {
        // CORREÇÃO DEFINITIVA: Removida qualquer menção à coluna 'status'.
        // Foca apenas em resetar o estado do guichê na tabela 'guiches'.
        String sqlLimparGuiche = "UPDATE guiches SET alocado = 0, alocado_por = NULL, operador_id = NULL WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sqlLimparGuiche)) {

            pstmt.setInt(1, guicheId);
            int rows = pstmt.executeUpdate();

            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}