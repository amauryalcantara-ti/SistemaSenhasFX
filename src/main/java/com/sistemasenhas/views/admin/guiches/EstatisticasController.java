package com.sistemasenhas.views.admin.guiches;

import com.sistemasenhas.services.EstatisticasService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class EstatisticasController {

    @FXML
    private DatePicker dpDataInicio, dpDataFim;

    @FXML
    private BarChart<String, Number> chartGuiche;

    @FXML
    private TableView<EstatRow> tableEstat;
    @FXML private TableColumn<EstatRow, String> colData;
    @FXML private TableColumn<EstatRow, String> colGuiche;
    @FXML private TableColumn<EstatRow, String> colServico;
    @FXML private TableColumn<EstatRow, String> colLocal;
    @FXML private TableColumn<EstatRow, Number> colTMA;
    @FXML private TableColumn<EstatRow, Number> colQtd;

    private final EstatisticasService service = new EstatisticasService();

    @FXML
    public void initialize() {
        if (dpDataInicio != null) dpDataInicio.setValue(LocalDate.now().minusDays(7));
        if (dpDataFim != null) dpDataFim.setValue(LocalDate.now());

        if (tableEstat != null) {
            colData.setCellValueFactory(new PropertyValueFactory<>("data"));
            colGuiche.setCellValueFactory(new PropertyValueFactory<>("guiche"));
            colServico.setCellValueFactory(new PropertyValueFactory<>("servico"));
            colLocal.setCellValueFactory(new PropertyValueFactory<>("local"));
            colTMA.setCellValueFactory(new PropertyValueFactory<>("tma"));
            colQtd.setCellValueFactory(new PropertyValueFactory<>("qtd"));
        }
    }

    @FXML
    public void handleGerar() {
        LocalDate inicio = dpDataInicio.getValue();
        LocalDate fim = dpDataFim.getValue();
        if (inicio == null || fim == null) return;

        Map<String, Double> tmaByGuiche = service.calcularTmaPorGuiche(inicio, fim);

        // Popular gráfico
        chartGuiche.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("TMA (s)");
        for (Map.Entry<String, Double> e : tmaByGuiche.entrySet()) {
            serie.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chartGuiche.getData().add(serie);

        // Popular tabela com detalhes (service retorna lista de linhas)
        List<EstatRow> rows = service.buscarEstatisticas(inicio, fim);
        ObservableList<EstatRow> obs = FXCollections.observableArrayList(rows);
        tableEstat.setItems(obs);
    }

    @FXML
    public void handleSalvarAgregados() {
        LocalDate inicio = dpDataInicio.getValue();
        LocalDate fim = dpDataFim.getValue();
        if (inicio == null || fim == null) return;
        service.salvarAgregadosPorPeriodo(inicio, fim);
    }

    public static class EstatRow {
        private String data, guiche, servico, local;
        private Integer tma, qtd;

        public EstatRow(String data, String guiche, String servico, String local, Integer tma, Integer qtd) {
            this.data = data; this.guiche = guiche; this.servico = servico; this.local = local; this.tma = tma; this.qtd = qtd;
        }

        public String getData() { return data; }
        public String getGuiche() { return guiche; }
        public String getServico() { return servico; }
        public String getLocal() { return local; }
        public Integer getTma() { return tma; }
        public Integer getQtd() { return qtd; }
    }
}
