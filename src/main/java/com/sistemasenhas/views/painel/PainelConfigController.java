package com.sistemasenhas.views.painel;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PainelConfigController {

    @FXML
    private ColorPicker colorPickerFundo;

    @FXML
    private TextField txtImagemFundo;

    @FXML
    private CheckBox chkDividirTV;

    @FXML
    private TextField txtImagemTV;

    @FXML
    private TextArea txtMensagemFeed;

    @FXML
    private ColorPicker colorPickerFonteFeed;

    @FXML
    private ColorPicker colorPickerFundoFeed;

    // Referência ao controller principal para aplicar as configurações
    private PainelPrincipalController painelController;

    // Configurações atuais
    private String corFundo = "#1a1a2e";
    private String caminhoImagemFundo = "";
    private boolean dividirTV = false;
    private String caminhoImagemTV = "";
    private String mensagemFeed = "";
    private String corFonteFeed = "#e94560";
    private String corFundoFeed = "#16213e";

    public void initialize() {
        // Carregar configurações salvas
        carregarConfiguracoes();
    }

    public void setPainelController(PainelPrincipalController controller) {
        this.painelController = controller;

        // Carregar valores atuais do painel
        if (controller != null) {
            corFundo = controller.getCorFundo();
            caminhoImagemFundo = controller.getCaminhoImagemFundo();
            dividirTV = controller.isDividirTV();
            caminhoImagemTV = controller.getCaminhoImagemTV();
            mensagemFeed = controller.getMensagemFeed();
            corFonteFeed = controller.getCorFonteFeed();
            corFundoFeed = controller.getCorFundoFeed();

            atualizarCampos();
        }
    }

    private void carregarConfiguracoes() {
        // Valores padrão
        colorPickerFundo.setValue(Color.web("#1a1a2e"));
        colorPickerFonteFeed.setValue(Color.web("#e94560"));
        colorPickerFundoFeed.setValue(Color.web("#16213e"));
    }

    private void atualizarCampos() {
        try {
            colorPickerFundo.setValue(Color.web(corFundo));
        } catch (Exception e) {
            colorPickerFundo.setValue(Color.web("#1a1a2e"));
        }
        try {
            colorPickerFonteFeed.setValue(Color.web(corFonteFeed));
        } catch (Exception e) {
            colorPickerFonteFeed.setValue(Color.web("#e94560"));
        }
        try {
            colorPickerFundoFeed.setValue(Color.web(corFundoFeed));
        } catch (Exception e) {
            colorPickerFundoFeed.setValue(Color.web("#16213e"));
        }
        txtImagemFundo.setText(caminhoImagemFundo != null ? caminhoImagemFundo : "");
        chkDividirTV.setSelected(dividirTV);
        txtImagemTV.setText(caminhoImagemTV != null ? caminhoImagemTV : "");
        txtMensagemFeed.setText(mensagemFeed != null ? mensagemFeed : "");
    }

    @FXML
    private void handleAplicarCor() {
        if (painelController != null) {
            Color cor = colorPickerFundo.getValue();
            String hexCor = String.format("#%02X%02X%02X",
                    (int) (cor.getRed() * 255),
                    (int) (cor.getGreen() * 255),
                    (int) (cor.getBlue() * 255));
            painelController.aplicarCorFundo(hexCor);
        }
    }

    @FXML
    private void handleEscolherImagem() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolher Imagem de Fundo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*"));

        Stage stage = (Stage) txtImagemFundo.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            txtImagemFundo.setText(file.getAbsolutePath());
            if (painelController != null) {
                painelController.aplicarImagemFundo(file.getAbsolutePath());
            }
        }
    }

    @FXML
    private void handleLimparImagem() {
        txtImagemFundo.setText("");
        if (painelController != null) {
            painelController.limparImagemFundo();
        }
    }

    @FXML
    private void handleEscolherImagemTV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolher Imagem para TV");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*"));

        Stage stage = (Stage) txtImagemTV.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            txtImagemTV.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSalvar() {
        if (painelController != null) {
            // Aplicar cor de fundo
            Color cor = colorPickerFundo.getValue();
            String hexCor = String.format("#%02X%02X%02X",
                    (int) (cor.getRed() * 255),
                    (int) (cor.getGreen() * 255),
                    (int) (cor.getBlue() * 255));
            painelController.aplicarCorFundo(hexCor);

            // Aplicar imagem de fundo
            String imagemFundo = txtImagemFundo.getText();
            if (imagemFundo != null && !imagemFundo.isEmpty()) {
                painelController.aplicarImagemFundo(imagemFundo);
            } else {
                painelController.limparImagemFundo();
            }

            // Aplicar divisão de TV
            boolean dividir = chkDividirTV.isSelected();
            String imagemTV = txtImagemTV.getText();
            painelController.configurarDivisaoTV(dividir, imagemTV);

            // Aplicar cores do feed
            Color corFonte = colorPickerFonteFeed.getValue();
            String hexCorFonte = String.format("#%02X%02X%02X",
                    (int) (corFonte.getRed() * 255),
                    (int) (corFonte.getGreen() * 255),
                    (int) (corFonte.getBlue() * 255));
            Color corFundoFeedSelecionada = colorPickerFundoFeed.getValue();
            String hexCorFundoFeed = String.format("#%02X%02X%02X",
                    (int) (corFundoFeedSelecionada.getRed() * 255),
                    (int) (corFundoFeedSelecionada.getGreen() * 255),
                    (int) (corFundoFeedSelecionada.getBlue() * 255));
            painelController.aplicarCoresFeed(hexCorFonte, hexCorFundoFeed);

            // Aplicar mensagem do feed (limitar 500 caracteres)
            String mensagem = txtMensagemFeed.getText();
            if (mensagem != null && mensagem.length() > 500) {
                mensagem = mensagem.substring(0, 500);
            }
            if (mensagem != null && !mensagem.isEmpty()) {
                painelController.atualizarMensagemFeed(mensagem);
            }

            javax.swing.JOptionPane.showMessageDialog(null,
                    "Configurações aplicadas com sucesso!",
                    "Sucesso",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }

        handleFechar();
    }

    @FXML
    private void handleFechar() {
        Stage stage = (Stage) colorPickerFundo.getScene().getWindow();
        stage.close();
    }
}
