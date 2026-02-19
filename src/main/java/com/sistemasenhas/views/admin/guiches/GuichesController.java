package com.sistemasenhas.views.admin.guiches;

import com.sistemasenhas.SessionManager;
import com.sistemasenhas.models.Guiche;
import com.sistemasenhas.models.LocalAtendimento;
import com.sistemasenhas.services.GuicheService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class GuichesController {

    private final GuicheService guicheService = new GuicheService();
    private Guiche guicheSelecionado;

    @FXML
    private TextField txtNumero, txtDescricao;
    @FXML
    private ComboBox<LocalAtendimento> comboLocalizacao;
    @FXML
    private ComboBox<String> comboStatus;
    @FXML
    private Button btnAdicionar, btnEditar, btnSalvar, btnExcluir;
    @FXML
    private TableView<Guiche> tableViewGuiches;
    @FXML
    private TableColumn<Guiche, String> colNumero, colDescricao, colLocalizacao, colStatus, colAlocacao;

    // Novo campo para gerenciar a troca de conteúdo interna se necessário
    @FXML
    private StackPane guicheContentArea;

    @FXML
    public void initialize() {
        // Formatação visual da coluna Número
        colNumero.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                "Guichê " + cellData.getValue().getNumero()));

        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colLocalizacao.setCellValueFactory(new PropertyValueFactory<>("localizacao"));

        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isAtivo() ? "Ativo" : "Inativo"));

        // Coluna de Alocação agora é estritamente informativa
        colAlocacao.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isAlocado() ? "Alocado" : "Livre"));

        carregarLocais();
        comboStatus.setItems(FXCollections.observableArrayList("Ativo", "Inativo"));

        carregarDados();

        tableViewGuiches.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            if (selecionado != null)
                preencherCampos(selecionado);
        });

        limparCampos();
    }

    /**
     * Método para carregar a tela de Tipos de Atendimento (Serviços)
     * caso o acionamento venha de dentro da tela de Guichês.
     */
    @FXML
    private void showServicosView(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/fxml/admin/guiches/TipoAtendimentoView.fxml");
            if (fxmlLocation == null) {
                System.err.println("Erro: FXML TipoAtendimentoView não encontrado.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Node node = loader.load();

            if (guicheContentArea != null) {
                guicheContentArea.getChildren().setAll(node);
            } else {
                // Se não houver área interna, o AdminController deve gerenciar.
                System.out.println("Aviso: guicheContentArea não definido. A navegação deve ser via AdminController.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
        carregarDados();
    }

    private void carregarLocais() {
        comboLocalizacao.setItems(FXCollections.observableArrayList(guicheService.listarLocais()));
    }

    private void carregarDados() {
        tableViewGuiches.setItems(FXCollections.observableArrayList(guicheService.listarTodosGuiches()));
        tableViewGuiches.refresh(); // Garante a atualização visual imediata
    }

    private void preencherCampos(Guiche g) {
        guicheSelecionado = g;
        txtNumero.setText(g.getNumero());
        txtDescricao.setText(g.getDescricao());
        comboStatus.setValue(g.isAtivo() ? "Ativo" : "Inativo");

        if (comboLocalizacao.getItems() != null) {
            comboLocalizacao.getItems().stream()
                    .filter(l -> l.getNome().equals(g.getLocalizacao()))
                    .findFirst().ifPresent(comboLocalizacao::setValue);
        }

        btnAdicionar.setDisable(true);
        btnEditar.setDisable(false);
        btnExcluir.setDisable(false);
        btnSalvar.setDisable(true);
        setCamposEditaveis(false);
    }

    private void setCamposEditaveis(boolean editavel) {
        txtNumero.setEditable(editavel);
        txtDescricao.setEditable(editavel);
        comboLocalizacao.setDisable(!editavel);
        comboStatus.setDisable(!editavel);
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        setCamposEditaveis(true);
        btnSalvar.setDisable(false);
        btnEditar.setDisable(true);
    }

    @FXML
    private void handleSalvar(ActionEvent event) {
        if (guicheSelecionado != null && validarCampos()) {
            guicheSelecionado.setNumero(txtNumero.getText());
            guicheSelecionado.setDescricao(txtDescricao.getText());
            guicheSelecionado.setLocalizacao(comboLocalizacao.getValue().getNome());
            guicheSelecionado.setAtivo("Ativo".equals(comboStatus.getValue()));

            if (guicheService.atualizarGuiche(guicheSelecionado)) {
                carregarDados();
                limparCampos();
                mostrarAlerta("Sucesso", "Guichê atualizado!", Alert.AlertType.INFORMATION);
            }
        }
    }

    @FXML
    private void handleExcluir(ActionEvent event) {
        if (guicheSelecionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja excluir este guichê?", ButtonType.YES,
                    ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                if (guicheService.deletarGuiche(guicheSelecionado.getId())) {
                    carregarDados();
                    limparCampos();
                }
            }
        }
    }

    @FXML
    private void handleAdicionarGuiche(ActionEvent event) {
        if (validarCampos()) {
            Guiche g = new Guiche();
            g.setNumero(txtNumero.getText());
            g.setDescricao(txtDescricao.getText());
            g.setLocalizacao(comboLocalizacao.getValue().getNome());
            g.setAtivo("Ativo".equals(comboStatus.getValue()));
            g.setAlocado(false); // Sempre inicia como livre no cadastro

            try {
                int usuarioLogado = SessionManager.getInstance().getIdUsuario();
                if (guicheService.adicionarGuiche(g, usuarioLogado)) {
                    carregarDados();
                    limparCampos();
                    mostrarAlerta("Sucesso", "Guichê cadastrado com sucesso!", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    mostrarAlerta("Erro", "O Guichê " + g.getNumero() + " já existe para o local " + g.getLocalizacao(),
                            Alert.AlertType.ERROR);
                } else {
                    mostrarAlerta("Erro", "Erro ao salvar: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }
    }

    private boolean validarCampos() {
        if (txtNumero.getText().isBlank() || comboLocalizacao.getValue() == null) {
            mostrarAlerta("Erro", "Número e Localização são obrigatórios.", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    @FXML
    private void handleVoltar(ActionEvent event) {
        limparCampos();
    }

    private void limparCampos() {
        txtNumero.clear();
        txtDescricao.clear();
        if (comboLocalizacao != null)
            comboLocalizacao.setValue(null);
        if (comboStatus != null)
            comboStatus.setValue("Ativo");

        guicheSelecionado = null;
        setCamposEditaveis(true);
        btnAdicionar.setDisable(false);
        btnEditar.setDisable(true);
        btnSalvar.setDisable(true);
        btnExcluir.setDisable(true);
        tableViewGuiches.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}