package com.sistemasenhas.services;

import com.sistemasenhas.views.admin.guiches.EstatisticasController.EstatRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class EstatisticasService {

    // Calcula TMA por guiche (média de duração em segundos) no período
    public Map<String, Double> calcularTmaPorGuiche(LocalDate inicio, LocalDate fim) {
        Map<String, Double> resultado = new LinkedHashMap<>();
        String sql = "SELECT g.numero AS guiche, AVG(a.duracao_seconds) AS tma " +
                "FROM atendimentos a JOIN guiches g ON a.guiche_id = g.id " +
                "WHERE DATE(a.data_atendimento) BETWEEN ? AND ? GROUP BY g.numero ORDER BY g.numero";
        try (Connection conn = com.sistemasenhas.database.DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String guiche = rs.getString("guiche");
                double tma = rs.getDouble("tma");
                resultado.put(guiche, tma);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular TMA por guichê: " + e.getMessage());
        }
        return resultado;
    }

    // Retorna linhas de estatística para popular a tabela
    public List<EstatRow> buscarEstatisticas(LocalDate inicio, LocalDate fim) {
        List<EstatRow> rows = new ArrayList<>();
        String sql = "SELECT DATE(a.data_atendimento) as data, g.numero as guiche, t.nome_original as servico, a.localizacao as local, " +
                "AVG(a.duracao_seconds) as tma, COUNT(*) as qtd " +
                "FROM atendimentos a " +
                "LEFT JOIN guiches g ON a.guiche_id = g.id " +
                "LEFT JOIN tipos_atendimento t ON a.servico_id = t.id " +
                "WHERE DATE(a.data_atendimento) BETWEEN ? AND ? " +
                "GROUP BY DATE(a.data_atendimento), g.numero, t.nome_original, a.localizacao " +
                "ORDER BY DATE(a.data_atendimento) DESC";

        try (Connection conn = com.sistemasenhas.database.DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String data = rs.getString("data");
                String guiche = rs.getString("guiche");
                String servico = rs.getString("servico");
                String local = rs.getString("local");
                int tma = (int) Math.round(rs.getDouble("tma"));
                int qtd = rs.getInt("qtd");
                rows.add(new EstatRow(data, guiche, servico, local, tma, qtd));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar estatísticas: " + e.getMessage());
        }
        return rows;
    }

    // Cria tabela de agregados se não existir e grava os valores agregados por dia
    public void salvarAgregadosPorPeriodo(LocalDate inicio, LocalDate fim) {
        String create = "CREATE TABLE IF NOT EXISTS estatisticas_diarias (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, data DATE, guiche VARCHAR(50), servico VARCHAR(150), localizacao VARCHAR(150), tma_seconds INT, qtd INT)";

        String insert = "INSERT INTO estatisticas_diarias (data, guiche, servico, localizacao, tma_seconds, qtd) VALUES (?, ?, ?, ?, ?, ?)";

        List<EstatRow> rows = buscarEstatisticas(inicio, fim);
        if (rows.isEmpty()) {
            System.out.println("Nenhuma estatística encontrada para salvar no período.");
            return;
        }

        try (Connection conn = com.sistemasenhas.database.DatabaseConnection.getConnection()) {
            try (PreparedStatement psCreate = conn.prepareStatement(create)) {
                psCreate.execute();
            }
            try (PreparedStatement psIns = conn.prepareStatement(insert)) {
                for (EstatRow r : rows) {
                    psIns.setString(1, r.getData());
                    psIns.setString(2, r.getGuiche());
                    psIns.setString(3, r.getServico());
                    psIns.setString(4, r.getLocal());
                    psIns.setInt(5, r.getTma());
                    psIns.setInt(6, r.getQtd());
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }
            System.out.println("Estatísticas agregadas salvas com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao salvar estatísticas agregadas: " + e.getMessage());
        }
    }
}
