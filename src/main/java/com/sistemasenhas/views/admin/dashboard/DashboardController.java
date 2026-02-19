package com.sistemasenhas.views.admin.dashboard;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;

public class DashboardController {

    @FXML private Label lblAtendimentosHoje;
    @FXML private Label lblAtendentesAtivos;
    @FXML private Label lblSenhasFila;
    @FXML private Label lblTotensAtivos;

    @FXML private PieChart chartAtendimentosTipo;
    @FXML private LineChart<String, Number> chartAtendimentosHora;

    @FXML
    public void initialize() {
        // Inicializar dados de exemplo (Mockup para interface)
        carregarDadosDashboard();
    }

    private void carregarDadosDashboard() {
        // Valores iniciais zerados até a implementação dos Services de estatísticas
        lblAtendimentosHoje.setText("0");
        lblAtendentesAtivos.setText("0");
        lblSenhasFila.setText("0");
        lblTotensAtivos.setText("0");

        // Configurar gráfico de pizza (Exemplo visual)
        chartAtendimentosTipo.getData().clear();
        chartAtendimentosTipo.getData().add(new PieChart.Data("Normal", 25));
        chartAtendimentosTipo.getData().add(new PieChart.Data("Prioritário", 15));
        chartAtendimentosTipo.getData().add(new PieChart.Data("Exame", 10));
        chartAtendimentosTipo.getData().add(new PieChart.Data("Consulta", 8));

        // Configurar gráfico de linha (Exemplo visual de fluxo)
        chartAtendimentosHora.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Fluxo de Atendimento (Hoje)");

        String[] horas = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
        int[] valores = {5, 12, 18, 22, 15, 8, 20, 25, 18, 10};

        for (int i = 0; i < horas.length; i++) {
            series.getData().add(new XYChart.Data<>(horas[i], valores[i]));
        }

        chartAtendimentosHora.getData().add(series);
    }

    @FXML
    private void abrirNovoUsuario() {
        showMessage("Módulo de Usuários", "Redirecionando para o gerenciamento de usuários...");
    }

    @FXML
    private void abrirNovoTipoAtendimento() {
        showMessage("Módulo de Atendimento", "Redirecionando para tipos de atendimento...");
    }

    @FXML
    private void abrirConfigTotem() {
        showMessage("Configuração", "Abrindo configurações do Totem...");
    }

    @FXML
    private void abrirRelatorios() {
        showMessage("Relatórios", "Gerando base para relatórios gerenciais...");
    }

    private void showMessage(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}