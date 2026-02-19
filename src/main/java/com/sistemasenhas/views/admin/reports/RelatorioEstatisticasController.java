package com.sistemasenhas.views.admin.reports;

import com.sistemasenhas.views.admin.guiches.EstatisticasController.EstatRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RelatorioEstatisticasController {

    @FXML private DatePicker dpData;
    @FXML private TableView<EstatRow> tableRel;
    @FXML private TableColumn<EstatRow,String> colData, colGuiche, colServico, colLocal;
    @FXML private TableColumn<EstatRow,Number> colTMA, colQtd;

    @FXML
    public void initialize() {
        if (tableRel != null) {
            colData.setCellValueFactory(new PropertyValueFactory<>("data"));
            colGuiche.setCellValueFactory(new PropertyValueFactory<>("guiche"));
            colServico.setCellValueFactory(new PropertyValueFactory<>("servico"));
            colLocal.setCellValueFactory(new PropertyValueFactory<>("local"));
            colTMA.setCellValueFactory(new PropertyValueFactory<>("tma"));
            colQtd.setCellValueFactory(new PropertyValueFactory<>("qtd"));
        }
    }

    @FXML
    public void handleCarregar() {
        LocalDate d = dpData.getValue();
        if (d == null) return;
        List<EstatRow> rows = new ArrayList<>();
        String sql = "SELECT data, guiche, servico, localizacao, tma_seconds, qtd FROM estatisticas_diarias WHERE data = ?";
        try (Connection conn = com.sistemasenhas.database.DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new EstatRow(rs.getString("data"), rs.getString("guiche"), rs.getString("servico"), rs.getString("localizacao"), rs.getInt("tma_seconds"), rs.getInt("qtd")));
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar relatório: " + e.getMessage());
        }
        ObservableList<EstatRow> obs = FXCollections.observableArrayList(rows);
        tableRel.setItems(obs);
    }
}
