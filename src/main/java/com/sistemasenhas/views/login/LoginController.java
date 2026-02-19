package com.sistemasenhas.views.login;

import com.sistemasenhas.SessionManager;
import com.sistemasenhas.models.Usuario;
import com.sistemasenhas.services.UsuarioService;
import com.sistemasenhas.views.trocarsenha.TrocarSenhaController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtSenha;
    @FXML
    private TextField txtSenhaVisivel;
    @FXML
    private ToggleButton toggleSenha;
    @FXML
    private Button btnLogin;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblDataHora;

    private UsuarioService usuarioService;
    private Usuario usuarioLogado;

    @FXML
    public void initialize() {
        usuarioService = new UsuarioService();

        btnLogin.setOnAction(event -> handleLogin());
        // Campos voltaram - descomentados
        txtSenha.setOnAction(event -> handleLogin());
        txtSenhaVisivel.setOnAction(event -> handleLogin());
        txtUsuario.setOnAction(event -> {
            if (toggleSenha.isSelected()) {
                txtSenhaVisivel.requestFocus();
            } else {
                txtSenha.requestFocus();
            }
        });

        toggleSenha.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                txtSenhaVisivel.setText(txtSenha.getText());
                txtSenhaVisivel.setVisible(true);
                txtSenhaVisivel.setManaged(true);
                txtSenha.setVisible(false);
                txtSenha.setManaged(false);
                txtSenhaVisivel.requestFocus();
                // Não mudar o texto - usar o que está no FXML
            } else {
                txtSenha.setText(txtSenhaVisivel.getText());
                txtSenha.setVisible(true);
                txtSenha.setManaged(true);
                txtSenhaVisivel.setVisible(false);
                txtSenhaVisivel.setManaged(false);
                txtSenha.requestFocus();
                // Não mudar o texto - usar o que está no FXML
            }
        });

        // Configurar comportamento de fechar para tela de login
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            if (stage != null) {
                // Tamanho maior para evitar sobreposição
                stage.setResizable(false);
                stage.setWidth(450);
                stage.setHeight(480);
                stage.centerOnScreen();

                // Apenas X para fechar
                stage.setOnCloseRequest(event -> {
                    System.exit(0); // Fecha o sistema na tela de login
                });
            }
        });

        // Iniciar atualização de data e hora
        atualizarDataHora();
    }

    @FXML
    private void handleLogin() {
        String usuarioInput = txtUsuario.getText().trim();
        String senha = toggleSenha.isSelected() ? txtSenhaVisivel.getText() : txtSenha.getText();

        if (usuarioInput.isEmpty() || senha.isEmpty()) {
            lblStatus.setText("Por favor, preencha todos os campos.");
            return;
        }

        try {
            usuarioLogado = usuarioService.autenticar(usuarioInput, senha);

            if (usuarioLogado != null) {
                // ATUALIZAÇÃO DA SESSÃO COM OS CAMPOS CORRETOS DA TABELA
                SessionManager session = SessionManager.getInstance();

                // Pegando o campo "usuario" (ex: Master_User) para exibir no cabeçalho
                session.setUsuarioLogado(usuarioLogado.getUsuario());

                // Guardando os demais dados necessários
                session.setNomeUsuario(usuarioLogado.getNomeCompleto());
                session.setTipoUsuario(String.valueOf(usuarioLogado.getTipoUsuarioId()));
                session.setIdUsuario(usuarioLogado.getId());

                if (usuarioLogado.getPrimeiroLogin() == 1 && usuarioLogado.getTipoUsuarioId() != 1) {
                    redirectToTrocarSenha();
                } else {
                    redirectByUserType();
                }

            } else {
                System.out.println("Usuário ou senha inválidos!");
                showError("Usuário ou senha inválidos!");
            }
        } catch (Exception e) {
            System.out.println("Erro de conexão! Tente novamente mais tarde!");
            e.printStackTrace();
            showError("Erro de conexão! Tente novamente mais tarde!");
        }
    }

    private void redirectToTrocarSenha() {
        abrirCena("/fxml/trocarsenha/TrocarSenhaView.fxml", "Trocar Senha", true);
    }

    private void redirectByUserType() {
        String fxmlFile;
        String title;

        switch (usuarioLogado.getTipoUsuarioId()) {
            case 1:
                fxmlFile = "/fxml/admin/AdminView.fxml";
                title = "Sistema de Senhas - Administração";
                break;
            case 4:
                fxmlFile = "/fxml/atendente/AtendenteView.fxml";
                title = "Atendimento";
                break;
            default:
                fxmlFile = "/fxml/escolha/EscolhaAreaView.fxml";
                title = "Selecionar Área";
                break;
        }
        abrirCena(fxmlFile, title, false);
    }

    private void abrirCena(String fxmlPath, String title, boolean isModal) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new RuntimeException("Arquivo não encontrado: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            if (fxmlPath.contains("trocarSenha")) {
                TrocarSenhaController controller = loader.getController();
                controller.setUsuario(usuarioLogado);
            }

            if (isModal) {
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle(title);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
                ((Stage) txtUsuario.getScene().getWindow()).close();
            } else {
                // Criar novo Stage para não herdar tamanho da tela de login
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle(title);

                // Configurar tamanho específico para cada tela
                if (fxmlPath.contains("AdminView.fxml")) {
                    stage.setWidth(1000);
                    stage.setHeight(700);
                } else if (fxmlPath.contains("AtendenteView.fxml")) {
                    stage.setWidth(900);
                    stage.setHeight(600);
                }

                stage.centerOnScreen();
                stage.show();
                ((Stage) btnLogin.getScene().getWindow()).close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao carregar a tela [" + title + "]: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Método para atualizar data e hora
    private void atualizarDataHora() {
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dataHoraFormatada = agora.format(formatter);

        if (lblDataHora != null) {
            lblDataHora.setText(dataHoraFormatada);
        }

        // Agendar próxima atualização em 1 segundo
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(1000);
                atualizarDataHora();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}