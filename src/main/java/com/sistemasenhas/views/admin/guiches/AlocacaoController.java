package com.sistemasenhas.views.admin.guiches;

import com.sistemasenhas.SessionManager;
import com.sistemasenhas.models.Alocacao;
import com.sistemasenhas.models.Guiche;
import com.sistemasenhas.models.LocalAtendimento;
import com.sistemasenhas.models.Usuario;
import com.sistemasenhas.services.AlocacaoService;
import com.sistemasenhas.services.GuicheService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.util.List;

public class AlocacaoController {

    private final AlocacaoService alocacaoService = new AlocacaoService();
    private final GuicheService guicheService = new GuicheService();

    @FXML
    private ComboBox<LocalAtendimento> comboLocalFiltro;
    @FXML
    private TableView<Guiche> tableViewAlocacao;
    @FXML
    private TableColumn<Guiche, String> colGuiche, colStatusCadastro, colSituacao, colOperador;
    @FXML
    private Label lblInfo;

    @FXML
    private ComboBox<Usuario> comboAtendente;
    @FXML
    private ComboBox<Guiche> comboGuiche;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Button btnAlocar;
    @FXML
    private Button btnDesalocar;

    @FXML
    public void initialize() {
        configurarFormatacaoObjetos();
        configurarColunasTabela();
        carregarLocais();
        atualizarDados();

        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }

        // CORREÇÃO: Botão alocar inicia habilitado por padrão
        if (btnAlocar != null) {
            btnAlocar.setDisable(false);
        }

        // Listener para o filtro de local e atualização do combo de guichês
        if (comboLocalFiltro != null) {
            comboLocalFiltro.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
                if (novo != null) {
                    handleAtualizar();
                    // Filtra o combo de guichês para exibir apenas os do local selecionado e
                    // disponíveis
                    comboGuiche.setItems(FXCollections.observableArrayList(
                            alocacaoService.listarGuichesDisponiveis(novo.getNome())));
                }
            });
        }

        // Listener de seleção na tabela para gerenciar o botão Desalocar
        tableViewAlocacao.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                // CORREÇÃO: Mantém Alocar habilitado e apenas alterna o Desalocar baseado no
                // status do guichê
                btnDesalocar.setDisable(!novo.isAlocado());
            } else {
                btnDesalocar.setDisable(true);
            }
        });
    }

    private void configurarFormatacaoObjetos() {
        if (comboAtendente != null) {
            comboAtendente.setConverter(new StringConverter<Usuario>() {
                @Override
                public String toString(Usuario u) {
                    return (u == null) ? "" : u.getUsuario();
                }

                @Override
                public Usuario fromString(String s) {
                    return null;
                }
            });
        }

        if (comboGuiche != null) {
            comboGuiche.setConverter(new StringConverter<Guiche>() {
                @Override
                public String toString(Guiche g) {
                    return (g == null) ? "" : "Guichê " + g.getNumero();
                }

                @Override
                public Guiche fromString(String s) {
                    return null;
                }
            });
        }

        if (comboLocalFiltro != null) {
            comboLocalFiltro.setConverter(new StringConverter<LocalAtendimento>() {
                @Override
                public String toString(LocalAtendimento l) {
                    return (l == null) ? "" : l.getNome();
                }

                @Override
                public LocalAtendimento fromString(String s) {
                    return null;
                }
            });
        }
    }

    private void configurarColunasTabela() {
        if (colGuiche != null)
            colGuiche.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                    "Guichê " + cellData.getValue().getNumero()));

        if (colStatusCadastro != null)
            colStatusCadastro.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().isAtivo() ? "Ativo" : "Inativo"));

        if (colSituacao != null)
            colSituacao.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().isAlocado() ? "Alocado" : "Livre"));

        if (colOperador != null)
            colOperador.setCellValueFactory(new PropertyValueFactory<>("nomeOperador"));
    }

    public void atualizarDados() {
        if (comboAtendente != null) {
            comboAtendente.setItems(FXCollections.observableArrayList(alocacaoService.listarAtendentes()));
        }
        carregarAlocacoesGlobais();
    }

    private void carregarLocais() {
        if (comboLocalFiltro != null) {
            comboLocalFiltro.setItems(FXCollections.observableArrayList(guicheService.listarLocais()));
        }
    }

    @FXML
    public void handleAtualizar() {
        LocalAtendimento local = comboLocalFiltro.getValue();
        if (local != null) {
            // Atualizado para usar listarGuichesComOperador e preencher o nomeOperador via
            // JOIN
            List<Guiche> lista = alocacaoService.listarGuichesComOperador(local.getNome());
            tableViewAlocacao.setItems(FXCollections.observableArrayList(lista));
            if (lblInfo != null)
                lblInfo.setText("Exibindo " + lista.size() + " guichê(s) em: " + local.getNome());
        }
    }

    @FXML
    private void handleAlocar() {
        Usuario selecionado = comboAtendente.getValue();
        Guiche guiche = comboGuiche.getValue();

        // Se não selecionou no combo, tenta pegar da tabela
        if (guiche == null) {
            guiche = tableViewAlocacao.getSelectionModel().getSelectedItem();
        }

        if (selecionado == null || guiche == null || (datePicker != null && datePicker.getValue() == null)) {
            mostrarAlerta("Aviso", "Selecione o Atendente, o Guichê e a Data!", Alert.AlertType.WARNING);
            return;
        }

        Alocacao a = new Alocacao();
        a.setUsuarioId(selecionado.getId());
        a.setGuicheId(guiche.getId());
        a.setDataAlocacao(datePicker.getValue());

        int supervisorId = SessionManager.getInstance().getIdUsuario();

        if (alocacaoService.salvarAlocacao(a, supervisorId)) {
            atualizarDados();
            handleAtualizar();
            limparCampos();
            mostrarAlerta("Sucesso", "Alocação realizada!", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Erro", "Erro ao realizar alocação.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDesalocar() {
        Guiche selecionado = tableViewAlocacao.getSelectionModel().getSelectedItem();

        if (selecionado == null || !selecionado.isAlocado()) {
            mostrarAlerta("Aviso", "Selecione um guichê alocado na tabela.", Alert.AlertType.WARNING);
            return;
        }

        if (alocacaoService.desalocarGuiche(selecionado.getId())) {
            atualizarDados();
            handleAtualizar();
            mostrarAlerta("Sucesso", "Operador desalocado com sucesso!", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Erro", "Não foi possível desalocar o operador.", Alert.AlertType.ERROR);
        }
    }

    private void carregarAlocacoesGlobais() {
        // Método para atualizar informações gerais se necessário
    }

    private void limparCampos() {
        if (comboAtendente != null)
            comboAtendente.setValue(null);
        if (comboGuiche != null)
            comboGuiche.setValue(null);
    }

    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}