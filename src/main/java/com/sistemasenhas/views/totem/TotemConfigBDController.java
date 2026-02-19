package com.sistemasenhas.views.totem;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class TotemConfigBDController {

    @FXML
    private Label lblMensagem;

    @FXML
    private TextField txtHost;

    @FXML
    private TextField txtPorta;

    @FXML
    private TextField txtBanco;

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnCancelar;

    public void initialize() {
        // Carregar configurações atuais se existirem
        loadCurrentConfig();
    }

    private void loadCurrentConfig() {
        // Implementar carregamento de configurações do arquivo de propriedades
        if (txtHost != null)
            txtHost.setText("localhost");
        if (txtPorta != null)
            txtPorta.setText("3306");
        if (txtBanco != null)
            txtBanco.setText("db_gerasenhas");
        if (txtUsuario != null)
            txtUsuario.setText("root");
        if (txtSenha != null)
            txtSenha.setText("");
    }

    @FXML
    private void handleSave() {
        try {
            String host = txtHost.getText();
            String port = txtPorta.getText();
            String database = txtBanco.getText();
            String user = txtUsuario.getText();
            String password = txtSenha.getText();

            // Validar campos
            if (host.isEmpty() || port.isEmpty() || database.isEmpty() || user.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Preencha todos os campos obrigatórios!",
                        "Erro de Validação",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Salvar configurações em arquivo de propriedades
            saveConfigToFile(host, port, database, user, password);

            javax.swing.JOptionPane.showMessageDialog(null,
                    "Configurações salvas com sucesso!",
                    "Sucesso",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);

            // Fechar janela
            Stage stage = (Stage) btnSalvar.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Erro ao salvar configurações: " + e.getMessage(),
                    "Erro",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    private void handleCancel() {
        // Fechar janela sem salvar
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void saveConfigToFile(String host, String port, String database, String user, String password) {
        // Implementar salvamento em arquivo de propriedades
        System.out.println("Configurações salvas:");
        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
        System.out.println("Database: " + database);
        System.out.println("User: " + user);
        System.out.println("Password: " + (password.isEmpty() ? "[vazio]" : "[preenchido]"));
    }

    @FXML
    private void handleTestConnection() {
        try {
            String host = txtHost.getText();
            String port = txtPorta.getText();
            String database = txtBanco.getText();
            String user = txtUsuario.getText();
            String password = txtSenha.getText();

            // Validar campos
            if (host.isEmpty() || port.isEmpty() || database.isEmpty() || user.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Preencha todos os campos obrigatórios!",
                        "Erro de Validação",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Testar conexão real
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&allowPublicKeyRetrieval=true";

            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, user, password)) {
                if (conn != null && !conn.isClosed()) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Conexão realizada com sucesso!\n\n" +
                                    "Host: " + host + "\n" +
                                    "Porta: " + port + "\n" +
                                    "Banco: " + database + "\n" +
                                    "Usuário: " + user,
                            "Teste de Conexão - SUCESSO",
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (java.sql.SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Falha na conexão!\n\nErro: " + e.getMessage(),
                    "Teste de Conexão - FALHOU",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Erro ao testar conexão: " + e.getMessage(),
                    "Erro",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
