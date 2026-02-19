package com.sistemasenhas.views.admin;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class AdminController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label lblDataHora;

    @FXML
    public void initialize() {
        System.out.println("AdminController inicializado. Aguardando comandos...");
        iniciarRelogio();
    }

    private void iniciarRelogio() {
        Timeline relogio = new Timeline(new KeyFrame(Duration.seconds(1), e -> atualizarDataHora()));
        relogio.setCycleCount(Timeline.INDEFINITE);
        relogio.play();
    }

    private void atualizarDataHora() {
        LocalDateTime agora = LocalDateTime.now();
        Locale ptBR = new Locale("pt", "BR");

        // Obtém o dia da semana e o mês com a primeira letra maiúscula
        String diaSemana = agora.getDayOfWeek().getDisplayName(TextStyle.FULL, ptBR);
        diaSemana = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);

        String mes = agora.getMonth().getDisplayName(TextStyle.FULL, ptBR);
        mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);

        int dia = agora.getDayOfMonth();
        int ano = agora.getYear();
        int hora = agora.getHour();
        int minuto = agora.getMinute();
        int segundo = agora.getSecond();

        String dataFormatada = String.format("%s, %02d de %s de %d - %02d:%02d:%02d",
                diaSemana, dia, mes, ano, hora, minuto, segundo);

        if (lblDataHora != null) {
            lblDataHora.setText(dataFormatada);
        }
    }

    @FXML
    private void showDashboard() {
        System.out.println("Ação: Exibir Dashboard");
    }

    @FXML
    private void showUsuarios() {
        System.out.println("Ação: Exibir Usuários");
        loadContent("/fxml/admin/usuarios/UsuariosView.fxml");
    }

    @FXML
    private void showServicos() {
        System.out.println("Ação: Exibir Tipos de Atendimento");
        loadContent("/fxml/admin/guiches/TipoAtendimentoView.fxml");
    }

    @FXML
    private void showGuiches() {
        System.out.println("🔵 showGuiches() chamado!");
        loadContent("/fxml/admin/guiches/GuichesView.fxml");
    }

    @FXML
    private void showConfigTotem() {
        System.out.println("Ação: Exibir Config. Totem");
    }

    @FXML
    private void showConfigPainel() {
        System.out.println("Ação: Exibir Config. Painel");
    }

    @FXML
    private void showModelosSenha() {
        System.out.println("Ação: Exibir Modelos de Senha");
    }

    @FXML
    private void showConfiguracoes() {
        System.out.println("Ação: Exibir Configurações");
    }

    @FXML
    private void showRelatorios() {
        System.out.println("Ação: Exibir Relatórios");
        loadContent("/fxml/admin/reports/RelatorioEstatisticasView.fxml");
    }

    @FXML
    private void handleLogout() {
        System.out.println("Saindo da conta Admin...");
        try {
            URL loginRes = getClass().getResource("/fxml/login/LoginView.fxml");
            if (loginRes == null) {
                System.err.println("Erro: FXML de Login não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(loginRes);
            Parent loginRoot = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            System.err.println("Erro ao retornar para a tela de login.");
            e.printStackTrace();
        }
    }

    private void loadContent(String fxmlPath) {
        try {
            System.out.println("📂 Tentando carregar: " + fxmlPath);
            URL res = getClass().getResource(fxmlPath);

            if (res == null) {
                System.err.println("❌ ERRO: O recurso FXML NÃO FOI ENCONTRADO em: " + fxmlPath);
                System.err.println("   Verifique se o arquivo existe em: src/main/resources" + fxmlPath);
                return;
            }

            System.out.println("✓ Recurso encontrado em: " + res.getPath());
            
            FXMLLoader loader = new FXMLLoader(res);
            System.out.println("⏳ Carregando FXML com FXMLLoader...");
            
            Node content = loader.load();
            System.out.println("✓ FXML carregado com sucesso!");

            if (contentArea != null) {
                System.out.println("✓ Inserindo conteúdo na área principal...");
                contentArea.getChildren().setAll(content);
                System.out.println("✓ Conteúdo inserido com sucesso!");
            } else {
                System.err.println("❌ ERRO: contentArea é null!");
            }
        } catch (Exception e) {
            System.err.println("❌ FALHA CRÍTICA ao carregar FXML: " + fxmlPath);
            System.err.println("   Mensagem de erro: " + e.getMessage());
            System.err.println("   Tipo de erro: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }
}