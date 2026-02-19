package com.sistemasenhas.views.trocarsenha;

import com.sistemasenhas.models.Usuario;
import com.sistemasenhas.services.UsuarioService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.regex.Pattern;

public class TrocarSenhaController {

    @FXML
    private PasswordField txtNovaSenha;
    @FXML
    private PasswordField txtConfirmarSenha;
    @FXML
    private TextField txtNovaSenhaVisivel;
    @FXML
    private TextField txtConfirmarSenhaVisivel;
    @FXML
    private ToggleButton toggleNovaSenha;
    @FXML
    private ToggleButton toggleConfirmarSenha;
    @FXML
    private Button btnConfirmar;
    @FXML
    private Button btnCancelar;
    @FXML
    private Label lblStatus;

    private Usuario usuario;
    private UsuarioService usuarioService;

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.usuarioService = new UsuarioService();
    }

    @FXML
    public void initialize() {
        // Configurar toggle buttons para mostrar/ocultar senha
        toggleNovaSenha.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Mostrar senha em texto
                txtNovaSenhaVisivel.setText(txtNovaSenha.getText());
                txtNovaSenhaVisivel.setVisible(true);
                txtNovaSenhaVisivel.setManaged(true);
                txtNovaSenha.setVisible(false);
                txtNovaSenha.setManaged(false);
                txtNovaSenhaVisivel.requestFocus();
                toggleNovaSenha.setText("Ocultar");
            } else {
                // Ocultar senha (mostrar bolinhas)
                txtNovaSenha.setText(txtNovaSenhaVisivel.getText());
                txtNovaSenha.setVisible(true);
                txtNovaSenha.setManaged(true);
                txtNovaSenhaVisivel.setVisible(false);
                txtNovaSenhaVisivel.setManaged(false);
                txtNovaSenha.requestFocus();
                toggleNovaSenha.setText("Mostrar");
            }
        });

        toggleConfirmarSenha.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Mostrar senha em texto
                txtConfirmarSenhaVisivel.setText(txtConfirmarSenha.getText());
                txtConfirmarSenhaVisivel.setVisible(true);
                txtConfirmarSenhaVisivel.setManaged(true);
                txtConfirmarSenha.setVisible(false);
                txtConfirmarSenha.setManaged(false);
                txtConfirmarSenhaVisivel.requestFocus();
                toggleConfirmarSenha.setText("Ocultar");
            } else {
                // Ocultar senha (mostrar bolinhas)
                txtConfirmarSenha.setText(txtConfirmarSenhaVisivel.getText());
                txtConfirmarSenha.setVisible(true);
                txtConfirmarSenha.setManaged(true);
                txtConfirmarSenhaVisivel.setVisible(false);
                txtConfirmarSenhaVisivel.setManaged(false);
                txtConfirmarSenha.requestFocus();
                toggleConfirmarSenha.setText("Mostrar");
            }
        });

        // Configurar ação dos botões principais
        btnConfirmar.setOnAction(e -> confirmarTrocaSenha());
        btnCancelar.setOnAction(e -> cancelar());
    }

    private String getNovaSenha() {
        return toggleNovaSenha.isSelected() ? txtNovaSenhaVisivel.getText() : txtNovaSenha.getText();
    }

    private String getConfirmarSenha() {
        return toggleConfirmarSenha.isSelected() ? txtConfirmarSenhaVisivel.getText() : txtConfirmarSenha.getText();
    }

    @FXML
    private void confirmarTrocaSenha() {
        String novaSenha = getNovaSenha();
        String confirmarSenha = getConfirmarSenha();

        // Validações
        if (novaSenha.isEmpty() || confirmarSenha.isEmpty()) {
            lblStatus.setText("Por favor, preencha todos os campos!");
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            lblStatus.setText("As senhas não coincidem! Confira novamente!");
            return;
        }

        if (!validarSenha(novaSenha)) {
            lblStatus.setText("A senha não atende aos requisitos mínimos! Deve conter pelo menos 10 caracteres, incluindo letras maiúsculas, minúsculas, números e caracteres especiais.");
            return;
        }

        try {
            // Atualiza a senha no banco de dados
            boolean sucesso = usuarioService.atualizarSenha(usuario.getId(), novaSenha);

            if (sucesso) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sucesso");
                alert.setHeaderText(null);
                alert.setContentText("Senha alterada com sucesso! Faça login novamente.");
                alert.showAndWait();

                // Volta para a tela de login
                voltarParaLogin();
            } else {
                lblStatus.setText("Erro ao atualizar senha! Tente novamente mais tarde!");
            }
        } catch (Exception e) {
            lblStatus.setText("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validarSenha(String senha) {
        // Mínimo 10 caracteres
        if (senha.length() < 10)
            return false;

        // Pelo menos uma letra maiúscula
        if (!Pattern.compile("[A-Z]").matcher(senha).find())
            return false;

        // Pelo menos uma letra minúscula
        if (!Pattern.compile("[a-z]").matcher(senha).find())
            return false;

        // Pelo menos um número
        if (!Pattern.compile("[0-9]").matcher(senha).find())
            return false;

        // Pelo menos um caractere especial
        if (!Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(senha).find())
            return false;

        return true;
    }

    @FXML
    private void cancelar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Cancelamento!");
        alert.setHeaderText("Deseja cancelar a troca de senha?");
        alert.setContentText("Você será desconectado.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            voltarParaLogin();
        }
    }

    private void voltarParaLogin() {
        try {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login - Sistema de Senhas");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}