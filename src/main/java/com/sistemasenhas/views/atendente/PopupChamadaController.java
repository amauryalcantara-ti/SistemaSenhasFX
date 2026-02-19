package com.sistemasenhas.views.atendente;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Map;

public class PopupChamadaController {

    @FXML
    private Label lblSenhaChamada;

    @FXML
    private Label lblTipoServico;

    @FXML
    private Button btnRechamar;

    @FXML
    private Button btnOk;

    private String senha;
    private Map<String, Object> servico;
    private int cliquesRechamar = 0;
    private Stage stage;

    @FXML
    public void initialize() {
        // Configurar eventos
        btnRechamar.setOnAction(e -> handleRechamar());
        btnOk.setOnAction(e -> handleOk());
    }

    public void setSenha(String senha) {
        this.senha = senha;
        lblSenhaChamada.setText(senha);
    }

    public void setServico(Map<String, Object> servico) {
        this.servico = servico;
        lblTipoServico.setText((String) servico.get("descricao"));
    }

    private void handleRechamar() {
        cliquesRechamar++;

        if (cliquesRechamar >= 3) {
            // Fechar popup após 3 cliques em rechamar
            fecharPopup();
        } else {
            // Continuar mostrando popup
            System.out.println("📢 Rechamada #" + cliquesRechamar + " para senha: " + senha);
        }
    }

    private void handleOk() {
        cliquesRechamar = 0;

        // Mostrar confirmação
        if (mostrarConfirmacao()) {
            fecharPopup();
        }
    }

    private boolean mostrarConfirmacao() {
        // Aqui você pode implementar a lógica de confirmação
        // Por enquanto, vamos apenas fechar o popup
        return true;
    }

    private void fecharPopup() {
        if (stage != null) {
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
