package com.sistemasenhas.services;

import com.sistemasenhas.database.DatabaseConnection;
import java.sql.*;

public class SenhaService {

    /**
     * Gera uma nova senha para um tipo de atendimento
     */
    public static String gerarNovaSenha(int serviceId, String servico) throws SQLException {
        try {
            System.out.println("=== GERANDO NOVA SENHA PARA SERVIÇO: " + servico + " (ID: " + serviceId + ") ===");

            // Buscar prefixo do serviço
            String siglaSql = "SELECT prefixo FROM tipos_atendimento WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement siglaPstmt = conn.prepareStatement(siglaSql)) {

                siglaPstmt.setInt(1, serviceId);
                try (ResultSet prefixoRs = siglaPstmt.executeQuery()) {

                    String prefixo = "";
                    if (prefixoRs.next()) {
                        prefixo = prefixoRs.getString("prefixo");
                        System.out.println("PREFIXO ENCONTRADO NO BANCO: '" + prefixo + "'");
                        if (prefixo == null || prefixo.trim().isEmpty()) {
                            prefixo = "A";
                            System.out.println("PREFIXO NULO/VAZIO, USANDO FALLBACK 'A'");
                        }
                    } else {
                        System.out.println("NENHUM PREFIXO ENCONTRADO PARA ID " + serviceId);
                        prefixo = "A";
                    }

                    // Buscar última senha para este serviço HOJE
                    // Usa LENGTH do prefixo para extrair corretamente o número sequencial
                    int tamPrefixo = prefixo.length();
                    String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(senha_codigo, " + (tamPrefixo + 1)
                            + ") AS UNSIGNED)), 0) as ultima " +
                            "FROM atendimentos " +
                            "WHERE tipo_id = ? AND DATE(hora_emissao) = CURDATE()";

                    System.out.println("SQL: " + sql + " | serviceId: " + serviceId);

                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, serviceId);
                        System.out.println("Param 1 setado: " + serviceId);
                        try (ResultSet rs = pstmt.executeQuery()) {

                            int ultimaSenha = 0;
                            if (rs.next()) {
                                ultimaSenha = rs.getInt("ultima");
                                System.out.println("Ultima senha obtida: " + ultimaSenha);
                            } else {
                                System.out.println("Nenhum resultado encontrado");
                            }

                            int proximaSenha = ultimaSenha + 1;
                            String senhaGerada = prefixo + String.format("%03d", proximaSenha);
                            System.out
                                    .println("Senha a ser gerada: " + senhaGerada + " (Próxima: " + proximaSenha + ")");

                            System.out.println("=== DEBUG GERAÇÃO SENHA ===");
                            System.out.println("Service ID: " + serviceId);
                            System.out.println("Serviço: " + servico);
                            System.out.println("Prefixo: " + prefixo);
                            System.out.println("Tamanho prefixo: " + tamPrefixo);
                            System.out.println("Ultima senha: " + ultimaSenha);
                            System.out.println("Proxima senha: " + proximaSenha);
                            System.out.println("Senha gerada: " + senhaGerada);
                            System.out.println("===============================");

                            // Inserir novo atendimento
                            String insertSql = "INSERT INTO atendimentos (tipo_id, senha_codigo, status, hora_emissao) "
                                    +
                                    "VALUES (?, ?, 'A', NOW())";

                            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                                insertPstmt.setInt(1, serviceId);
                                insertPstmt.setString(2, senhaGerada);
                                insertPstmt.executeUpdate();
                            }

                            return senhaGerada;
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao gerar senha: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao gerar senha", e);
        }
    }
}
