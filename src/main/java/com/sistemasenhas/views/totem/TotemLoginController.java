package com.sistemasenhas.views.totem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.sistemasenhas.database.DatabaseConnection;

public class TotemLoginController {

    // Variáveis para armazenar o local logado
    private int localIdLogado = 0;
    private String nomeLocalLogado = "";

    @FXML
    private Label lblNomeEmpresa;

    @FXML
    private Label lblDataHora;

    @FXML
    private Label lblMensagem;

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private Button btnEntrar;

    @FXML
    private Button btnConfigurarBD;

    public void initialize() {
        // Inicialização do controller
        System.out.println("TotemLoginController inicializado");

        try {
            // Configurar data e hora atual
            String dataHora = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            lblDataHora.setText(dataHora);

            // Limpar mensagem de erro
            lblMensagem.setText("");

            // Forçar inicialização dos campos
            if (txtLogin != null) {
                txtLogin.setText("");
                txtLogin.setPromptText("Usuário do Totem");
            }

            if (txtSenha != null) {
                txtSenha.setText("");
                txtSenha.setPromptText("Senha");
            }

            if (btnEntrar != null) {
                btnEntrar.setOnAction(event -> handleEntrar());
            }

            if (btnConfigurarBD != null) {
                btnConfigurarBD.setOnAction(event -> handleConfigurarBD());
            }

            // Mensagem de teste removida - só mostrar erros agora

        } catch (Exception e) {
            System.err.println("Erro na inicialização: " + e.getMessage());
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Erro ao inicializar o sistema: " + e.getMessage(),
                    "Erro de Inicialização",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    private void handleEntrar() {
        try {
            System.out.println("=== BOTÃO ENTRAR CLICADO ===");

            String login = txtLogin.getText();
            String senha = txtSenha.getText();

            System.out.println("Login digitado: " + login);
            System.out.println("Senha digitada: " + (senha.isEmpty() ? "[vazia]" : "[preenchida]"));

            // Validar campos
            if (login.isEmpty() || senha.isEmpty()) {
                System.out.println("ERRO: Campos vazios");
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Preencha todos os campos!",
                        "Erro de Validação",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validar credenciais (implementar lógica real)
            if (validarCredenciais(login, senha)) {
                System.out.println("SUCESSO: Credenciais válidas");

                // Abrir tela principal do totem
                abrirTelaPrincipal();

            } else {
                System.out.println("ERRO: Credenciais inválidas");
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Login ou senha inválidos!\n\nVerifique suas credenciais e tente novamente.",
                        "Erro de Login",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            System.err.println("ERRO EXCEÇÃO ao fazer login: " + e.getMessage());
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Erro ao processar login: " + e.getMessage(),
                    "Erro",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    private void handleConfigurarBD() {
        try {
            System.out.println("=== BOTÃO CONFIGURAR BD CLICADO ===");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/totem/TotemConfigBDView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Configuração do Banco de Dados");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            System.err.println("ERRO EXCEÇÃO ao abrir tela de configuração: " + e.getMessage());
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Erro ao abrir configuração: " + e.getMessage(),
                    "Erro",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validarCredenciais(String login, String senha) {
        try {
            System.out.println("Validando credenciais para: " + login);

            // Verificar se o login começa com "Totem"
            if (!login.toLowerCase().startsWith("totem")) {
                System.out.println("Login não começa com prefixo 'Totem'");
                return false;
            }

            // Remover prefixo "Totem" e espaços extras
            String nomeLocal = login.substring(5).trim(); // Remove "Totem" (5 caracteres)
            System.out.println("Nome do local após remoção do prefixo: '" + nomeLocal + "'");

            if (nomeLocal.isEmpty()) {
                System.out.println("Nome do local vazio após remover prefixo");
                return false;
            }

            // Buscar o local de atendimento (totem) exatamente na tabela local_atendimento
            String sqlLocal = "SELECT id, nome FROM local_atendimento WHERE nome = ? LIMIT 1";
            java.sql.PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sqlLocal);
            pstmt.setString(1, nomeLocal);

            java.sql.ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int localId = rs.getInt("id");
                String nomeEncontrado = rs.getString("nome");
                System.out.println("Local de atendimento encontrado: '" + nomeEncontrado + "' (ID: " + localId + ")");

                // Armazenar o local logado
                this.localIdLogado = localId;
                this.nomeLocalLogado = nomeEncontrado;

                rs.close();
                pstmt.close();

                // Se encontrou o local, validar a senha genérica do totem
                System.out.println("Validando senha genérica do totem...");

                String sqlSenhaTotem = "SELECT senha_totem FROM tb_config_sistema LIMIT 1";
                java.sql.Statement stmt = DatabaseConnection.getConnection().createStatement();
                java.sql.ResultSet rsSenha = stmt.executeQuery(sqlSenhaTotem);

                if (rsSenha.next()) {
                    String senhaTotemConfig = rsSenha.getString("senha_totem");
                    System.out.println("Senha do totem configurada encontrada");

                    boolean senhaValida = senhaTotemConfig.equals(senha);
                    System.out.println("Senha do totem válida: " + senhaValida);

                    rsSenha.close();
                    stmt.close();

                    if (senhaValida) {
                        System.out.println("Login aceito para totem: " + nomeEncontrado);
                        return true;
                    } else {
                        System.out.println("Senha incorreta para totem: " + nomeEncontrado);
                        return false;
                    }
                }

                rsSenha.close();
                stmt.close();
                return false;
            } else {
                System.out.println("Local de atendimento não encontrado com nome: '" + nomeLocal + "'");

                // Listar locais disponíveis para debug
                try {
                    String sqlListar = "SELECT nome FROM local_atendimento";
                    java.sql.Statement stmtList = DatabaseConnection.getConnection().createStatement();
                    java.sql.ResultSet rsList = stmtList.executeQuery(sqlListar);
                    System.out.println("Locais de atendimento disponíveis:");
                    while (rsList.next()) {
                        System.out.println("  - '" + rsList.getString("nome") + "'");
                    }
                    rsList.close();
                    stmtList.close();
                } catch (Exception e) {
                    System.err.println("Erro ao listar locais: " + e.getMessage());
                }

                rs.close();
                pstmt.close();
                return false;
            }

        } catch (Exception e) {
            System.err.println("Erro ao validar credenciais: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void abrirTelaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/totem/TotemPrincipalView.fxml"));
            Parent root = loader.load();

            // Passar o local logado para o controller da tela principal
            TotemPrincipalController controller = loader.getController();
            controller.setLocal(localIdLogado, nomeLocalLogado);

            Stage stage = new Stage();
            stage.setTitle("Sistema de Senhas - " + nomeLocalLogado);

            // Remover decoração da janela (sem botões de fechar/minimizar/maximizar)
            stage.initStyle(StageStyle.UNDECORATED);

            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Colocar em tela cheia
            stage.setFullScreen(true);
            stage.setFullScreenExitHint(""); // Remover mensagem de "ESC para sair"

            // Passar o stage para o controller poder gerenciar ESC
            controller.setStage(stage);

            stage.show();

            // Fechar janela atual
            Stage currentStage = (Stage) btnEntrar.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            System.err.println("Erro ao abrir tela principal: " + e.getMessage());
            lblMensagem.setText("Erro ao abrir tela principal!");
            lblMensagem.setStyle("-fx-text-fill: red;");
        }
    }

    // Métodos antigos mantidos para compatibilidade
    @FXML
    private void handleAdminLogin() {
        handleEntrar();
    }

    @FXML
    private void handleAtendenteLogin() {
        handleEntrar();
    }

    @FXML
    private void handleTotemMode() {
        abrirTelaPrincipal();
    }
}
