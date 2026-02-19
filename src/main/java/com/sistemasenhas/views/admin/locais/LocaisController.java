package com.sistemasenhas.views.admin.locais;

import com.sistemasenhas.models.LocalAtendimento;
import com.sistemasenhas.services.GuicheService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class LocaisController {

    private final GuicheService guicheService = new GuicheService();

    @FXML 
    private TextField txtNomeLocal;
    
    @FXML 
    private TableView<LocalAtendimento> tableViewLocais;
    
    @FXML 
    private TableColumn<LocalAtendimento, Integer> colId;
    
    @FXML 
    private TableColumn<LocalAtendimento, String> colNome;

    @FXML
    public void initialize() {
        // Vincula as colunas aos atributos do modelo LocalAtendimento
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        
        // Carrega a tabela com os dados do banco
        carregarLocais();
    }

    private void carregarLocais() {
        // Atualiza a lista da tabela buscando do GuicheService
        tableViewLocais.setItems(FXCollections.observableArrayList(guicheService.listarLocais()));
    }

    @FXML
    private void handleAdicionarLocal(ActionEvent event) {
        String nome = txtNomeLocal.getText();
        
        if (nome != null && !nome.trim().isEmpty()) {
            // Chamada real ao banco de dados
            if (guicheService.adicionarLocal(nome.trim())) {
                // Se salvou com sucesso, limpa o campo e recarrega a tabela
                txtNomeLocal.clear();
                carregarLocais();
                
                // Feedback opcional para o usuário
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Local adicionado com sucesso!");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar o local no banco de dados.");
            }
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Por favor, digite o nome do local.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}