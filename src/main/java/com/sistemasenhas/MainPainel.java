package com.sistemasenhas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainPainel extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Carregar tela de login do painel
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/painel/PainelLoginView.fxml"));

            Scene scene = new Scene(root);

            primaryStage.setTitle("Painel de Chamadas - Sistema de Senhas");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();

            // Configurar para fechar completamente
            primaryStage.setOnCloseRequest(event -> {
                System.exit(0);
            });

            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Erro ao iniciar aplicação do painel: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
