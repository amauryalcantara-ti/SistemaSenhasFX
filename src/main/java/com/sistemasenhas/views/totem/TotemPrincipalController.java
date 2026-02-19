package com.sistemasenhas.views.totem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.HPos;
import javafx.geometry.VPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sistemasenhas.database.DatabaseConnection;
import com.sistemasenhas.services.SenhaService;

public class TotemPrincipalController {

    // Variáveis para armazenar o local logado
    private int localId = 0;
    private String nomeLocal = "";
    private Stage stage;

    // Lista de serviços carregados
    private List<ServicoInfo> servicosCarregados = new ArrayList<>();

    @FXML
    private Label lblTitle;

    @FXML
    private GridPane containerServicos;

    @FXML
    private Button btnBack;

    public void initialize() {
        System.out.println("TotemPrincipalController inicializado");
        // Não carregar serviços aqui - aguardar setLocal() ser chamado
    }

    /**
     * Define o Stage e configura o handler de ESC
     */
    public void setStage(Stage stage) {
        this.stage = stage;

        // Configurar handler de tecla ESC
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                confirmarSaida();
                event.consume();
            }
        });

        // Listener para redimensionamento da tela
        stage.widthProperty().addListener((obs, oldVal, newVal) -> reorganizarBotoes());
        stage.heightProperty().addListener((obs, oldVal, newVal) -> reorganizarBotoes());
    }

    /**
     * Mostra diálogo de confirmação para sair do totem
     */
    private void confirmarSaida() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sair do Totem");
        alert.setHeaderText("Deseja realmente sair?");
        alert.setContentText("Você será redirecionado para a tela de login.");

        // Estilizar o alerta
        alert.initStyle(StageStyle.UTILITY);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            voltarParaLogin();
        }
    }

    /**
     * Volta para a tela de login do totem
     */
    private void voltarParaLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/totem/TotemLoginView.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Totem - Sistema de Senhas");
            loginStage.setScene(new Scene(root));
            loginStage.setMaximized(true);

            // Configurar para fechar completamente ao clicar no X
            loginStage.setOnCloseRequest(event -> {
                System.exit(0);
            });

            loginStage.show();

            // Fechar janela atual
            if (stage != null) {
                stage.close();
            }

        } catch (Exception e) {
            System.err.println("Erro ao voltar para tela de login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Define o local de atendimento logado e carrega os serviços alocados
     */
    public void setLocal(int localId, String nomeLocal) {
        this.localId = localId;
        this.nomeLocal = nomeLocal;
        System.out.println("Local definido: " + nomeLocal + " (ID: " + localId + ")");

        // Atualizar título da tela
        if (lblTitle != null) {
            lblTitle.setText("Totem - " + nomeLocal);
        }

        // Agora sim carregar os serviços do local
        loadAvailableServices();
    }

    private void loadAvailableServices() {
        try {
            // Limpar lista de serviços
            servicosCarregados.clear();

            // Carregar serviços habilitados para este local específico
            String sql = "SELECT ta.id, ta.descricao, ls.cor_fundo, ls.cor_texto " +
                    "FROM local_servicos ls " +
                    "INNER JOIN tipos_atendimento ta ON ls.servico_id = ta.id " +
                    "WHERE ls.localizacao = ? AND ta.ativo = 1 AND ls.ativo = 1 " +
                    "ORDER BY ta.descricao";

            java.sql.PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql);
            pstmt.setString(1, nomeLocal);
            java.sql.ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String descricao = rs.getString("descricao");
                String corFundo = rs.getString("cor_fundo");
                String corTexto = rs.getString("cor_texto");

                servicosCarregados.add(new ServicoInfo(id, descricao, corFundo, corTexto));
            }

            rs.close();
            pstmt.close();

            System.out.println("Carregados " + servicosCarregados.size() + " serviços para o local: " + nomeLocal);

            // Reorganizar botões na tela
            reorganizarBotoes();

        } catch (Exception e) {
            System.err.println("Erro ao carregar serviços: " + e.getMessage());
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Erro ao carregar serviços: " + e.getMessage(),
                    "Erro no Sistema",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reorganiza os botões dinamicamente para preencher toda a tela sem espaços
     * ociosos
     * Lógica:
     * - 1 botão: preenche toda a tela
     * - 2 botões: 1 coluna, 2 linhas (um em cima do outro)
     * - 3 botões: 1 coluna, 3 linhas
     * - 4 botões: 2 colunas x 2 linhas
     * - 5 botões: 2 colunas x 2 linhas + 1 esticado na 3ª linha
     * - 6 botões: 2 colunas x 3 linhas
     * - 7 botões: 2 colunas x 3 linhas + 1 esticado na 4ª linha
     * E assim por diante...
     */
    private void reorganizarBotoes() {
        containerServicos.getChildren().clear();
        containerServicos.getColumnConstraints().clear();
        containerServicos.getRowConstraints().clear();

        if (servicosCarregados.isEmpty()) {
            Label lblSemServicos = new Label(
                    "Nenhum serviço habilitado para este local.\nContate o administrador.");
            lblSemServicos.setStyle("-fx-font-size: 24px; -fx-text-fill: #666666;");
            containerServicos.add(lblSemServicos, 0, 0);
            return;
        }

        int totalServicos = servicosCarregados.size();

        // Determinar layout baseado na quantidade de serviços
        int numColunas;
        int numLinhasCompletas;
        boolean temLinhaExtra;

        if (totalServicos == 1) {
            // 1 botão: preenche tudo
            numColunas = 1;
            numLinhasCompletas = 1;
            temLinhaExtra = false;
        } else if (totalServicos == 2) {
            // 2 botões: 1 coluna, 2 linhas
            numColunas = 1;
            numLinhasCompletas = 2;
            temLinhaExtra = false;
        } else if (totalServicos == 3) {
            // 3 botões: 1 coluna, 3 linhas
            numColunas = 1;
            numLinhasCompletas = 3;
            temLinhaExtra = false;
        } else {
            // 4+ botões: 2 colunas
            numColunas = 2;
            numLinhasCompletas = totalServicos / 2;
            temLinhaExtra = (totalServicos % 2) == 1; // Se ímpar, tem linha extra com 1 botão
        }

        int totalLinhas = numLinhasCompletas + (temLinhaExtra ? 1 : 0);

        // Configurar constraints das colunas (distribuição igual)
        for (int i = 0; i < numColunas; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / numColunas);
            col.setHalignment(HPos.CENTER);
            col.setHgrow(Priority.ALWAYS);
            containerServicos.getColumnConstraints().add(col);
        }

        // Configurar constraints das linhas (distribuição igual)
        for (int i = 0; i < totalLinhas; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / totalLinhas);
            row.setValignment(VPos.CENTER);
            row.setVgrow(Priority.ALWAYS);
            containerServicos.getRowConstraints().add(row);
        }

        // Adicionar botões
        int index = 0;

        // Adicionar botões nas linhas completas (com todas as colunas preenchidas)
        for (int row = 0; row < numLinhasCompletas && index < totalServicos; row++) {
            for (int col = 0; col < numColunas && index < totalServicos; col++) {
                ServicoInfo servico = servicosCarregados.get(index);
                Button btn = createServiceButton(servico.id, servico.descricao,
                        servico.corFundo, servico.corTexto);

                // O botão expande para preencher a célula
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setMaxHeight(Double.MAX_VALUE);
                GridPane.setHgrow(btn, Priority.ALWAYS);
                GridPane.setVgrow(btn, Priority.ALWAYS);
                GridPane.setFillWidth(btn, true);
                GridPane.setFillHeight(btn, true);

                containerServicos.add(btn, col, row);
                index++;
            }
        }

        // Se tem linha extra (número ímpar de botões com 2 colunas)
        if (temLinhaExtra && index < totalServicos) {
            ServicoInfo servico = servicosCarregados.get(index);
            Button btn = createServiceButton(servico.id, servico.descricao,
                    servico.corFundo, servico.corTexto);

            // O último botão ocupa todas as colunas (colspan)
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setMaxHeight(Double.MAX_VALUE);
            GridPane.setHgrow(btn, Priority.ALWAYS);
            GridPane.setVgrow(btn, Priority.ALWAYS);
            GridPane.setFillWidth(btn, true);
            GridPane.setFillHeight(btn, true);
            GridPane.setColumnSpan(btn, numColunas); // Ocupa todas as colunas

            containerServicos.add(btn, 0, numLinhasCompletas);
        }

        // Definir gaps mínimos entre botões
        containerServicos.setHgap(5);
        containerServicos.setVgap(5);

        System.out.println("Botões reorganizados: " + numColunas + " colunas x " + totalLinhas + " linhas" +
                (temLinhaExtra ? " (última linha esticada)" : ""));
    }

    private Button createServiceButton(int serviceId, String serviceName, String corFundo, String corTexto) {
        Button button = new Button(serviceName);

        // Usar cores do banco ou cores padrão
        String bgColor = (corFundo != null && !corFundo.isEmpty()) ? corFundo : "#3498db";
        String txtColor = (corTexto != null && !corTexto.isEmpty()) ? corTexto : "#ffffff";

        button.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; " +
                "-fx-background-color: " + bgColor + "; " +
                "-fx-text-fill: " + txtColor + "; " +
                "-fx-border-radius: 15px; " +
                "-fx-background-radius: 15px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 20px;");

        button.setOnAction(event -> handleServiceSelection(serviceId, serviceName));

        return button;
    }

    private void handleServiceSelection(int serviceId, String serviceName) {
        try {
            // Gerar senha
            String senha = generateNewPassword(serviceId, serviceName);

            // Abrir tela de confirmação
            openConfirmationScreen(serviceId, serviceName, senha);

        } catch (Exception e) {
            System.err.println("Erro ao processar seleção de serviço: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateNewPassword(int serviceId, String servico) {
        try {
            return SenhaService.gerarNovaSenha(serviceId, servico);
        } catch (Exception e) {
            System.err.println("Erro ao gerar senha: " + e.getMessage());
            e.printStackTrace();
            return "ERR001";
        }
    }

    private void openConfirmationScreen(int serviceId, String serviceName, String senha) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/totem/TotemConfirmacaoView.fxml"));
            Parent root = loader.load();

            TotemConfirmacaoController controller = loader.getController();
            controller.setDados(senha, serviceName, nomeLocal, serviceId);

            Stage stage = new Stage();
            stage.setTitle("Confirmação de Senha");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Atualizar lista de serviços após fechar tela de confirmação
            loadAvailableServices();

        } catch (Exception e) {
            System.err.println("Erro ao abrir tela de confirmação: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        // Chamar confirmação de saída
        confirmarSaida();
    }

    @FXML
    private void handleRefresh() {
        // Atualizar lista de serviços
        loadAvailableServices();
    }

    @FXML
    private void handleConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/totem/TotemConfigBDView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Configuração do Banco de Dados");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            System.err.println("Erro ao abrir tela de configuração: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Classe interna para armazenar informações do serviço
     */
    private static class ServicoInfo {
        int id;
        String descricao;
        String corFundo;
        String corTexto;

        ServicoInfo(int id, String descricao, String corFundo, String corTexto) {
            this.id = id;
            this.descricao = descricao;
            this.corFundo = corFundo;
            this.corTexto = corTexto;
        }
    }
}
