package com.sistemasenhas.views.escolha;

import com.sistemasenhas.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class EscolhaAreaController {

    @FXML
    private Label lblUsuario;
    @FXML
    private Button btnAreaAdmin;
    @FXML
    private Button btnAreaAtendente;
    @FXML
    private Button btnVoltar;

    @FXML
    public void initialize() {
        String nomeUsuario = SessionManager.getInstance().getNomeUsuario();
        lblUsuario.setText("Usuário: " + (nomeUsuario != null ? nomeUsuario : "Não identificado!"));

        btnAreaAdmin.setOnAction(event -> handleAreaAdmin());
        btnAreaAtendente.setOnAction(event -> handleAreaAtendente());
        btnVoltar.setOnAction(event -> handleVoltar());

        // Configurar janela após carregar
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) btnVoltar.getScene().getWindow();
            if (stage != null) {
                // Tamanho fixo
                stage.setResizable(false);
                stage.setWidth(400);
                stage.setHeight(350);
                stage.centerOnScreen();

                // Apenas X para fechar
                stage.setOnCloseRequest(event -> {
                    event.consume(); // Impede fechar
                    handleVoltar(); // Redireciona para login
                });
            }
        });
    }

    @FXML
    private void handleAreaAdmin() {
        loadScreen("/fxml/admin/AdminView.fxml", "Área Administrativa");
    }

    @FXML
    private void handleAreaAtendente() {
        loadScreen("/fxml/atendente/AtendenteView.fxml", "Área do Atendente");
    }

    @FXML
    private void handleVoltar() {
        try {
            // Limpar sessão
            SessionManager.getInstance().logout();

            // Carregar tela de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnVoltar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sistema de Senhas - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível voltar para a tela de login.");
        }
    }

    private void loadScreen(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) btnVoltar.getScene().getWindow();

            // Resetar configurações da janela para outras telas
            stage.setResizable(true);
            // Todas as telas voltam para login no X (exceto login)
            stage.setOnCloseRequest(event -> {
                event.consume(); // Impede fechar
                handleVoltar(); // Redireciona para login
            });

            stage.setScene(new Scene(root));
            stage.setTitle(title);
            // Definir tamanho para cada área
            if (fxmlPath.contains("AdminView.fxml")) {
                stage.setWidth(1000);
                stage.setHeight(700);
                // Não maximizar automaticamente, permitir que usuário maximize se quiser
            } else if (fxmlPath.contains("AtendenteView.fxml")) {
                stage.setWidth(900);
                stage.setHeight(600);
            }
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível carregar: " + fxmlPath);
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}