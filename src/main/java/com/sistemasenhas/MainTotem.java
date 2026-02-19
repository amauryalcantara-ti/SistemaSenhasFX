package com.sistemasenhas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainTotem extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Carregar tela de login do totém
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/totem/TotemLoginView.fxml"));

            Scene scene = new Scene(root);

            primaryStage.setTitle("Totem - Sistema de Senhas");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();

            // Configurar para fechar completamente
            primaryStage.setOnCloseRequest(event -> {
                System.exit(0);
            });

            primaryStage.show();

        } catch (Exception e) {
            System.err.println("❌ Erro ao iniciar aplicação do totém: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
