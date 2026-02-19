package com.sistemasenhas.views.painel;

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

public class PainelLoginController {

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
        System.out.println("PainelLoginController inicializado");

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
                txtLogin.setPromptText("Usuário do Painel");
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

            // Validar credenciais
            if (validarCredenciais(login, senha)) {
                System.out.println("SUCESSO: Credenciais válidas");

                // Abrir tela principal do painel
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/painel/PainelConfigBDView.fxml"));
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

            // Verificar se o login começa com "Painel"
            if (!login.toLowerCase().startsWith("painel")) {
                System.out.println("Login não começa com prefixo 'Painel'");
                return false;
            }

            // Remover prefixo "Painel" e espaços extras
            String nomeLocal = login.substring(6).trim(); // Remove "Painel" (6 caracteres)
            System.out.println("Nome do local após remoção do prefixo: '" + nomeLocal + "'");

            if (nomeLocal.isEmpty()) {
                System.out.println("Nome do local vazio após remover prefixo");
                return false;
            }

            // Buscar o local de atendimento na tabela local_atendimento (LIKE para ser
            // flexível)
            String sqlLocal = "SELECT id, nome FROM local_atendimento WHERE LOWER(REPLACE(nome, ' ', '')) = LOWER(REPLACE(?, ' ', '')) OR LOWER(nome) = LOWER(?) LIMIT 1";
            java.sql.PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sqlLocal);
            pstmt.setString(1, nomeLocal);
            pstmt.setString(2, nomeLocal);

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

                // Validar a senha genérica do painel
                System.out.println("Validando senha genérica do painel...");

                // Primeiro tenta buscar senha_painel, se não existir, usa "Painel123" como
                // padrão
                String senhaPainelConfig = "Painel123"; // Senha padrão

                try {
                    String sqlSenhaPainel = "SELECT senha_painel FROM tb_config_sistema LIMIT 1";
                    java.sql.Statement stmt = DatabaseConnection.getConnection().createStatement();
                    java.sql.ResultSet rsSenha = stmt.executeQuery(sqlSenhaPainel);

                    if (rsSenha.next()) {
                        String senhaConfig = rsSenha.getString("senha_painel");
                        if (senhaConfig != null && !senhaConfig.isEmpty()) {
                            senhaPainelConfig = senhaConfig;
                        }
                    }
                    rsSenha.close();
                    stmt.close();
                } catch (Exception e) {
                    // Se a coluna não existir, usa a senha padrão
                    System.out.println("Coluna senha_painel não existe, usando senha padrão: Painel123");
                }

                boolean senhaValida = senhaPainelConfig.equals(senha);
                System.out.println("Senha do painel válida: " + senhaValida);

                if (senhaValida) {
                    System.out.println("Login aceito para painel: " + nomeEncontrado);
                    return true;
                } else {
                    System.out.println("Senha incorreta para painel: " + nomeEncontrado);
                    return false;
                }
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
            System.out.println("Abrindo tela principal do painel...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/painel/PainelPrincipalView.fxml"));
            Parent root = loader.load();

            // Obter o controller e passar o local logado
            PainelPrincipalController controller = loader.getController();
            controller.setLocalAtendimento(localIdLogado, nomeLocalLogado);

            // Criar nova janela SEM decoração (sem botões minimizar/maximizar/fechar)
            Stage painelStage = new Stage(StageStyle.UNDECORATED);
            painelStage.setTitle("Painel de Chamadas - " + nomeLocalLogado);

            Scene scene = new Scene(root);
            painelStage.setScene(scene);

            // Abrir configurações ANTES de mostrar o painel
            abrirConfiguracoesAntes(controller);

            // Configurar callback para voltar ao login
            controller.setVoltarParaLoginCallback(() -> {
                reabrirTelaLogin();
            });

            // Configurar teclas de atalho (ESC para menu sair, F11 para fullscreen)
            controller.configurarTeclasAtalho(painelStage);

            painelStage.setOnCloseRequest(event -> {
                controller.pararAtualizacao();
            });

            // Fechar tela de login
            Stage loginStage = (Stage) btnEntrar.getScene().getWindow();
            loginStage.close();

            // Colocar em tela cheia real (oculta barra de tarefas)
            painelStage.setFullScreen(true);
            painelStage.setFullScreenExitHint(""); // Remove mensagem de ESC
            painelStage.show();
            System.out.println("Painel aberto em modo tela cheia!");

        } catch (Exception e) {
            System.err.println("Erro ao abrir tela principal: " + e.getMessage());
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Erro ao abrir painel: " + e.getMessage(),
                    "Erro",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reabrirTelaLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/painel/PainelLoginView.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Painel de Chamadas - Login");
            loginStage.setScene(new Scene(root));
            loginStage.centerOnScreen();
            loginStage.show();

        } catch (Exception e) {
            System.err.println("Erro ao reabrir login: " + e.getMessage());
            e.printStackTrace();
            // Se falhar ao reabrir login, fechar aplicação
            System.exit(0);
        }
    }

    private void abrirConfiguracoesAntes(PainelPrincipalController controller) {
        try {
            int opcao = javax.swing.JOptionPane.showConfirmDialog(null,
                    "Deseja configurar o painel antes de abrir?\n(Cor de fundo, feed, divisão TV, etc.)",
                    "Configurações do Painel",
                    javax.swing.JOptionPane.YES_NO_OPTION);

            if (opcao == javax.swing.JOptionPane.YES_OPTION) {
                FXMLLoader configLoader = new FXMLLoader(getClass().getResource("/fxml/painel/PainelConfigView.fxml"));
                Parent configRoot = configLoader.load();

                PainelConfigController configController = configLoader.getController();
                configController.setPainelController(controller);

                Stage configStage = new Stage();
                configStage.setTitle("Configurações do Painel");
                configStage.setScene(new Scene(configRoot));
                configStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                configStage.setResizable(false);
                configStage.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Erro ao abrir configurações: " + e.getMessage());
        }
    }
}
