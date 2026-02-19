package com.sistemasenhas.services;

import com.sistemasenhas.database.DatabaseConnection;
import com.sistemasenhas.models.Guiche;
import com.sistemasenhas.models.LocalAtendimento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuicheService {

    // --- MÉTODOS DE LOCALIZAÇÃO ---

    public List<LocalAtendimento> listarLocais() {
        List<LocalAtendimento> locais = new ArrayList<>();
        String sql = "SELECT id, nome FROM local_atendimento ORDER BY nome";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                locais.add(new LocalAtendimento(rs.getInt("id"), rs.getString("nome")));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar locais: " + e.getMessage());
        }
        return locais;
    }

    public boolean adicionarLocal(String nome) {
        String sql = "INSERT INTO local_atendimento (nome) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletarLocal(int id) {
        String sql = "DELETE FROM local_atendimento WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- MÉTODOS DE GUICHÊ ---

    public List<Guiche> listarTodosGuiches() {
        List<Guiche> lista = new ArrayList<>();
        String sql = "SELECT id, numero, descricao, localizacao, ativo, alocado FROM guiches ORDER BY numero";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Guiche g = new Guiche();
                g.setId(rs.getInt("id"));
                g.setNumero(rs.getString("numero"));
                g.setDescricao(rs.getString("descricao"));
                g.setLocalizacao(rs.getString("localizacao"));
                g.setAtivo(rs.getBoolean("ativo"));
                g.setAlocado(rs.getBoolean("alocado"));
                lista.add(g);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * NOVO MÉTODO: Lista guichês filtrados por nome da localização
     */
    public List<Guiche> listarGuichesPorLocal(String nomeLocal) {
        List<Guiche> lista = new ArrayList<>();
        String sql = "SELECT id, numero, descricao, localizacao, ativo, alocado FROM guiches WHERE localizacao = ? ORDER BY numero";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeLocal);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Guiche g = new Guiche();
                    g.setId(rs.getInt("id"));
                    g.setNumero(rs.getString("numero"));
                    g.setDescricao(rs.getString("descricao"));
                    g.setLocalizacao(rs.getString("localizacao"));
                    g.setAtivo(rs.getBoolean("ativo"));
                    g.setAlocado(rs.getBoolean("alocado"));
                    lista.add(g);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean adicionarGuiche(Guiche guiche, int usuarioId) throws SQLException {
        String sql = "INSERT INTO guiches (numero, descricao, localizacao, ativo, alocado, cadastrado_por) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guiche.getNumero());
            pstmt.setString(2, guiche.getDescricao());
            pstmt.setString(3, guiche.getLocalizacao());
            pstmt.setBoolean(4, guiche.isAtivo());
            pstmt.setBoolean(5, guiche.isAlocado());
            pstmt.setInt(6, usuarioId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean atualizarGuiche(Guiche guiche) {
        String sql = "UPDATE guiches SET numero = ?, descricao = ?, localizacao = ?, ativo = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guiche.getNumero());
            pstmt.setString(2, guiche.getDescricao());
            pstmt.setString(3, guiche.getLocalizacao());
            pstmt.setBoolean(4, guiche.isAtivo());
            pstmt.setInt(5, guiche.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletarGuiche(int id) {
        String sql = "DELETE FROM guiches WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean alocarGuiche(int guicheId, int usuarioId, boolean status) {
        String sql = "UPDATE guiches SET alocado = ?, alocado_por = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, status);
            if (status) {
                pstmt.setInt(2, usuarioId);
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            pstmt.setInt(3, guicheId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- MÉTODOS DE VÍNCULO (GUICHE_SERVICOS) ---

    public boolean vincularServicoAoGuiche(int guicheId, int localServicoId) {
        String sql = "INSERT INTO guiche_servicos (guiche_id, local_servico_id, ativo) VALUES (?, ?, 1) "
                + "ON DUPLICATE KEY UPDATE ativo = 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, guicheId);
            pstmt.setInt(2, localServicoId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean desvincularServicoDoGuiche(int guicheId, int localServicoId) {
        String sql = "DELETE FROM guiche_servicos WHERE guiche_id = ? AND local_servico_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, guicheId);
            pstmt.setInt(2, localServicoId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}