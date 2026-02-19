package com.sistemasenhas.services;

import com.sistemasenhas.database.DatabaseConnection;
import com.sistemasenhas.models.Usuario;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UsuarioService {

    public Usuario autenticar(String usuario, String senha) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM usuarios WHERE usuario = ? AND ativo = 1 AND bloqueado = 0";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int tipoUsuarioId = rs.getInt("tipo_usuario_id");
                boolean senhaValida = false;

                // REGRA MASTER: Texto Puro
                if (tipoUsuarioId == 1) {
                    String senhaTexto = rs.getString("senha_texto");
                    senhaValida = (senhaTexto != null && senha.equals(senhaTexto));
                } else {
                    // OUTROS: Senha Temporária ou MD5
                    String senhaTemporaria = rs.getString("senha_temporaria");
                    if (senhaTemporaria != null && senha.equals(senhaTemporaria)) {
                        senhaValida = true;
                    } else {
                        String senhaHash = rs.getString("senha_hash");
                        if (senhaHash != null) {
                            String senhaFornecidaHash = gerarMD5(senha);
                            senhaValida = senhaHash.equals(senhaFornecidaHash);
                        }
                    }
                }

                if (senhaValida) {
                    Usuario user = new Usuario();
                    user.setId(rs.getInt("id"));
                    user.setUsuario(usuario);
                    user.setNomeCompleto(rs.getString("nome_completo"));
                    user.setPrimeiroLogin(rs.getInt("primeiro_login"));
                    user.setTipoUsuarioId(tipoUsuarioId);
                    user.setAtivo(1);
                    return user;
                }
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    public boolean atualizarSenha(int usuarioId, String novaSenha) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet tipoRs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Verifica o tipo de usuário antes de salvar
            String tipoSql = "SELECT tipo_usuario_id FROM usuarios WHERE id = ?";
            PreparedStatement tipoStmt = conn.prepareStatement(tipoSql);
            tipoStmt.setInt(1, usuarioId);
            tipoRs = tipoStmt.executeQuery();

            if (tipoRs.next()) {
                int tipoUsuarioId = tipoRs.getInt("tipo_usuario_id");
                String sql;

                if (tipoUsuarioId == 1) {
                    // Master salva em texto puro
                    sql = "UPDATE usuarios SET senha_texto = ?, primeiro_login = 0 WHERE id = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, novaSenha);
                } else {
                    // Demais salvam em MD5 e limpam temporária
                    String novaSenhaHash = gerarMD5(novaSenha);
                    sql = "UPDATE usuarios SET senha_hash = ?, senha_temporaria = NULL, " +
                        "primeiro_login = 0, ultima_troca_senha = CURRENT_TIMESTAMP WHERE id = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, novaSenhaHash);
                }

                stmt.setInt(2, usuarioId);
                return stmt.executeUpdate() > 0;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(tipoRs, stmt, conn);
        }
    }

    public String gerarMD5(String senha) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(senha.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}