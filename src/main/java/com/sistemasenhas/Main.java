package com.sistemasenhas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("=== SISTEMA DE GERENCIAMENTO DE SENHAS ===");

            // O caminho deve ser relativo à raiz do classpath (resources)
            String fxmlPath = "/fxml/login/LoginView.fxml";
            System.out.println("Buscando recurso FXML em: " + fxmlPath);

            URL fxmlLocation = getClass().getResource(fxmlPath);

            if (fxmlLocation == null) {
                throw new Exception("ERRO: Arquivo FXML não encontrado no caminho: " + fxmlPath +
                        "\nVerifique se o arquivo está em: src/main/resources/fxml/login/LoginView.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            System.out.println("✅ FXML carregado com sucesso através do Classpath!");

            Scene scene = new Scene(root);

            primaryStage.setTitle("Sistema de Gerenciamento de Senhas");
            primaryStage.setScene(scene);

            // Não definir resizable aqui - deixar cada controller controlar sua própria
            // janela
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("ERRO CRITICO ao carregar tela de login: ");
            System.err.println("Causa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Lançar a aplicação JavaFX
        launch(args);
    }
}