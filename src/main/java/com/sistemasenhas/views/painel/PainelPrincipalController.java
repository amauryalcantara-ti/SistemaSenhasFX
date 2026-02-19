package com.sistemasenhas.views.painel;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.sistemasenhas.database.DatabaseConnection;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PainelPrincipalController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label lblNomeEmpresa;

    @FXML
    private Button btnConfig;

    @FXML
    private Label lblFeedRolante;

    @FXML
    private HBox feedContainer;

    @FXML
    private HBox containerPrincipal;

    @FXML
    private VBox areaPainel;

    @FXML
    private VBox areaSenhaAtual;

    @FXML
    private Label lblSenhaAtual;

    @FXML
    private Label lblGuicheAtual;

    @FXML
    private Label lblTipoAtendimento;

    @FXML
    private HBox containerHistorico;

    @FXML
    private VBox areaTV;

    @FXML
    private ImageView imagemTV;

    @FXML
    private Label lblTV;

    @FXML
    private Label lblNomeLocal;

    @FXML
    private Label lblDataHora;

    // Variáveis de controle
    private int localId;
    private String nomeLocal;
    private Timeline atualizacaoTimeline;
    private Timeline dataHoraTimeline;
    private Timeline feedTimeline;
    private String ultimaSenhaChamada = "";
    private List<ChamadaInfo> historicoChamadas = new ArrayList<>();

    // Controle de erros e navegação
    private boolean erroTabelaReportado = false;
    private Stage painelStage;
    private Runnable voltarParaLoginCallback;

    // Configurações de personalização
    private String corFundo = "#1a1a2e";
    private String caminhoImagemFundo = "";
    private boolean dividirTV = false;
    private String caminhoImagemTV = "";
    private String mensagemFeed = "";
    private String corFonteFeed = "#e94560";
    private String corFundoFeed = "#16213e";

    // Controle do feed
    private TranslateTransition feedTransition;
    private double feedWidth = 0;

    // Classe auxiliar para armazenar informações de chamada
    private static class ChamadaInfo {
        String senha;
        String guiche;
        String tipoAtendimento;
        String nomeServico;

        ChamadaInfo(String senha, String guiche, String tipoAtendimento, String nomeServico) {
            this.senha = senha;
            this.guiche = guiche;
            this.tipoAtendimento = tipoAtendimento;
            this.nomeServico = nomeServico;
        }
    }

    public void initialize() {
        System.out.println("PainelPrincipalController inicializado");

        // Carregar nome da empresa
        carregarNomeEmpresa();

        // Iniciar atualização de data/hora
        iniciarAtualizacaoDataHora();

        // Iniciar feed rolante
        iniciarFeedRolante();
    }

    // ===================== GETTERS PARA CONFIGURAÇÕES =====================

    public String getCorFundo() {
        return corFundo;
    }

    public String getCaminhoImagemFundo() {
        return caminhoImagemFundo;
    }

    public boolean isDividirTV() {
        return dividirTV;
    }

    public String getCaminhoImagemTV() {
        return caminhoImagemTV;
    }

    public String getMensagemFeed() {
        return mensagemFeed;
    }

    public String getCorFonteFeed() {
        return corFonteFeed;
    }

    public String getCorFundoFeed() {
        return corFundoFeed;
    }

    // ===================== MÉTODOS DE CONFIGURAÇÃO =====================

    @FXML
    private void handleConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/painel/PainelConfigView.fxml"));
            Parent root = loader.load();

            PainelConfigController configController = loader.getController();
            configController.setPainelController(this);

            Stage configStage = new Stage();
            configStage.setTitle("Configurações do Painel");
            configStage.setScene(new Scene(root));
            configStage.initModality(Modality.APPLICATION_MODAL);
            configStage.setResizable(false);
            configStage.showAndWait();

        } catch (Exception e) {
            System.err.println("Erro ao abrir configurações: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void aplicarCorFundo(String hexCor) {
        this.corFundo = hexCor;
        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: " + hexCor + ";");
        }
    }

    public void aplicarImagemFundo(String caminho) {
        this.caminhoImagemFundo = caminho;
        try {
            File file = new File(caminho);
            if (file.exists()) {
                String uri = file.toURI().toString();
                if (rootPane != null) {
                    rootPane.setStyle("-fx-background-image: url('" + uri + "'); " +
                            "-fx-background-size: cover; " +
                            "-fx-background-position: center;");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao aplicar imagem de fundo: " + e.getMessage());
        }
    }

    public void limparImagemFundo() {
        this.caminhoImagemFundo = "";
        if (rootPane != null) {
            rootPane.setStyle("-fx-background-color: " + corFundo + ";");
        }
    }

    public void configurarDivisaoTV(boolean dividir, String caminhoImagem) {
        this.dividirTV = dividir;
        this.caminhoImagemTV = caminhoImagem;

        if (areaTV != null) {
            areaTV.setVisible(dividir);
            areaTV.setManaged(dividir);

            if (dividir && caminhoImagem != null && !caminhoImagem.isEmpty()) {
                try {
                    File file = new File(caminhoImagem);
                    if (file.exists()) {
                        Image img = new Image(file.toURI().toString());
                        imagemTV.setImage(img);
                        lblTV.setVisible(false);
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao carregar imagem da TV: " + e.getMessage());
                }
            } else if (dividir) {
                lblTV.setVisible(true);
                lblTV.setText("Área de Mídia/TV");
            }
        }
    }

    public void atualizarMensagemFeed(String mensagem) {
        this.mensagemFeed = mensagem;
        if (lblFeedRolante != null && mensagem != null && !mensagem.isEmpty()) {
            lblFeedRolante.setText(mensagem);
            // Reiniciar a animação com o novo texto
            reiniciarFeedRolante();
        }
    }

    public void aplicarCoresFeed(String corFonte, String corFundo) {
        this.corFonteFeed = corFonte;
        this.corFundoFeed = corFundo;

        if (lblFeedRolante != null) {
            lblFeedRolante.setStyle("-fx-text-fill: " + corFonte + "; -fx-font-size: 20; -fx-font-weight: bold;");
        }
        if (feedContainer != null) {
            feedContainer.setStyle("-fx-background-color: " + corFundo + "; -fx-padding: 10;");
        }
    }

    // Configurar listener de ESC para sair
    public void configurarTeclasAtalho(Stage stage) {
        this.painelStage = stage;
        stage.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                mostrarMenuSair();
            }
            if (event.getCode() == javafx.scene.input.KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
    }

    public void setVoltarParaLoginCallback(Runnable callback) {
        this.voltarParaLoginCallback = callback;
    }

    private void mostrarMenuSair() {
        int opcao = javax.swing.JOptionPane.showConfirmDialog(null,
                "Deseja sair do Painel de Chamadas?\n\nClique SIM para voltar ao Login\nClique NÃO para continuar no Painel",
                "Sair do Painel",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (opcao == javax.swing.JOptionPane.YES_OPTION) {
            voltarParaLogin();
        }
    }

    private void voltarParaLogin() {
        pararAtualizacao();
        if (painelStage != null) {
            painelStage.close();
        }
        if (voltarParaLoginCallback != null) {
            Platform.runLater(() -> voltarParaLoginCallback.run());
        }
    }

    public void setLocalAtendimento(int localId, String nomeLocal) {
        this.localId = localId;
        this.nomeLocal = nomeLocal;

        System.out.println("Local de atendimento configurado: " + nomeLocal + " (ID: " + localId + ")");

        if (lblNomeLocal != null) {
            lblNomeLocal.setText("Painel: " + nomeLocal);
        }

        // Iniciar busca de senhas chamadas
        iniciarAtualizacaoSenhas();
    }

    private void carregarNomeEmpresa() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                return;
            }

            String sql = "SELECT nome_empresa FROM tb_config_sistema LIMIT 1";
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                String nomeEmpresa = rs.getString("nome_empresa");
                if (lblNomeEmpresa != null && nomeEmpresa != null) {
                    lblNomeEmpresa.setText(nomeEmpresa);
                }
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            // Ignorar erro silenciosamente para não poluir logs
        }
    }

    private void iniciarAtualizacaoDataHora() {
        dataHoraTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            String dataHora = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            if (lblDataHora != null) {
                lblDataHora.setText(dataHora);
            }
        }));
        dataHoraTimeline.setCycleCount(Animation.INDEFINITE);
        dataHoraTimeline.play();
    }

    private void iniciarFeedRolante() {
        // Carregar mensagem personalizada do BD ou usar padrão
        String mensagemCarregada = "Bem-vindo! Aguarde sua senha ser chamada no painel.  ★  Atendimento por ordem de chegada.  ★  Obrigado pela preferência!";

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT mensagem_painel FROM tb_config_sistema LIMIT 1";
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    String msg = rs.getString("mensagem_painel");
                    if (msg != null && !msg.isEmpty()) {
                        mensagemCarregada = msg;
                    }
                }
                rs.close();
                stmt.close();
            }
        } catch (Exception e) {
            // Se a coluna não existir, usa mensagem padrão
            System.out.println("Usando mensagem padrão do feed");
        }

        this.mensagemFeed = mensagemCarregada;
        lblFeedRolante.setText(mensagemCarregada);

        // Aguardar layout ser calculado e iniciar animação
        Platform.runLater(() -> {
            Platform.runLater(() -> reiniciarFeedRolante());
        });
    }

    private void reiniciarFeedRolante() {
        if (feedTransition != null) {
            feedTransition.stop();
        }

        if (lblFeedRolante == null || feedContainer == null)
            return;

        // Calcular larguras reais
        lblFeedRolante.applyCss();
        feedContainer.applyCss();

        double containerWidth = feedContainer.getWidth();
        if (containerWidth <= 0)
            containerWidth = 1200; // valor padrão para tela grande

        double textWidth = lblFeedRolante.getBoundsInLocal().getWidth();
        if (textWidth <= 0)
            textWidth = lblFeedRolante.getText().length() * 12; // estimativa

        // Velocidade: ~80 pixels por segundo
        double distanciaTotal = containerWidth + textWidth;
        double duracao = distanciaTotal / 80.0;

        feedTransition = new TranslateTransition(Duration.seconds(duracao), lblFeedRolante);
        feedTransition.setFromX(containerWidth);
        feedTransition.setToX(-textWidth);
        feedTransition.setCycleCount(Animation.INDEFINITE);
        feedTransition.setInterpolator(javafx.animation.Interpolator.LINEAR);
        feedTransition.play();
    }

    private void tocarCampainha() {
        // Gerar som programático usando Java Sound API
        tocarSomProgramatico();
    }

    private void tocarSomProgramatico() {
        new Thread(() -> {
            try {
                // Gerar som de campainha sintetizado
                javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(44100, 16, 1, true, false);
                javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(
                        javax.sound.sampled.SourceDataLine.class, format);
                javax.sound.sampled.SourceDataLine line = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem
                        .getLine(info);
                line.open(format);
                line.start();

                // Gerar tom de campainha (2 tons)
                // Primeiro tom (mais alto)
                gerarTom(line, 880, 300); // Lá5
                Thread.sleep(50);
                // Segundo tom (mais baixo)
                gerarTom(line, 659, 400); // Mi5

                line.drain();
                line.close();
            } catch (Exception e) {
                System.err.println("Erro ao tocar som: " + e.getMessage());
            }
        }).start();
    }

    private void gerarTom(javax.sound.sampled.SourceDataLine line, double frequencia, int duracaoMs) {
        int samples = (int) (44100 * duracaoMs / 1000.0);
        byte[] buffer = new byte[samples * 2];

        for (int i = 0; i < samples; i++) {
            double angle = 2.0 * Math.PI * i * frequencia / 44100;
            // Envelope para suavizar início e fim
            double envelope = Math.min(1.0, Math.min(i / 200.0, (samples - i) / 200.0));
            short sample = (short) (Math.sin(angle) * 32767 * 0.7 * envelope);
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        line.write(buffer, 0, buffer.length);
    }

    private void iniciarAtualizacaoSenhas() {
        // Atualizar a cada 2 segundos
        atualizacaoTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            buscarSenhasChamadas();
        }));
        atualizacaoTimeline.setCycleCount(Animation.INDEFINITE);
        atualizacaoTimeline.play();

        // Buscar imediatamente
        buscarSenhasChamadas();
    }

    private void buscarSenhasChamadas() {
        // Se já reportou erro de tabela, não tenta mais
        if (erroTabelaReportado) {
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                return;
            }

            // Primeiro verificar se as tabelas existem
            java.sql.DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "senhas", null);
            if (!tables.next()) {
                // Tabela não existe - silenciar e não tentar mais
                erroTabelaReportado = true;
                tables.close();
                return;
            }
            tables.close();

            // Buscar senhas com status CHAMANDO para este local
            String sql = "SELECT s.numero, g.numero as guiche_num, ta.descricao as tipo_atendimento, " +
                    "ta.prefixo, s.data_chamada " +
                    "FROM senhas s " +
                    "JOIN guiches g ON s.guiche_id = g.id " +
                    "JOIN tipos_atendimento ta ON s.servico_id = ta.id " +
                    "WHERE s.status = 'CHAMANDO' AND g.local_id = ? " +
                    "ORDER BY s.data_chamada DESC " +
                    "LIMIT 6";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, localId);
            ResultSet rs = pstmt.executeQuery();

            List<ChamadaInfo> chamadasAtuais = new ArrayList<>();
            boolean primeiroRegistro = true;
            String senhaMaisRecente = "";

            while (rs.next()) {
                String senha = rs.getString("numero");
                String guiche = rs.getString("guiche_num");
                String tipoAtendimento = rs.getString("tipo_atendimento");
                String prefixo = rs.getString("prefixo");

                ChamadaInfo chamada = new ChamadaInfo(senha, guiche, tipoAtendimento, prefixo);
                chamadasAtuais.add(chamada);

                if (primeiroRegistro) {
                    senhaMaisRecente = senha;
                    primeiroRegistro = false;
                }
            }

            rs.close();
            pstmt.close();

            // Verificar se há nova chamada
            final String senhaNova = senhaMaisRecente;
            final List<ChamadaInfo> chamadasFinal = chamadasAtuais;

            Platform.runLater(() -> {
                if (!chamadasFinal.isEmpty()) {
                    // Verificar se é uma nova senhaF
                    if (!senhaNova.equals(ultimaSenhaChamada) && !senhaNova.isEmpty()) {
                        ultimaSenhaChamada = senhaNova;
                        // Tocar som de campainha
                        tocarCampainha();
                        // Piscar a área da senha
                        piscarSenhaAtual();
                    }

                    // Atualizar senha atual (primeira da lista)
                    ChamadaInfo senhaAtual = chamadasFinal.get(0);
                    lblSenhaAtual.setText(senhaAtual.senha);
                    lblGuicheAtual.setText(senhaAtual.guiche);
                    lblTipoAtendimento.setText(senhaAtual.tipoAtendimento);

                    // Atualizar histórico (demais senhas)
                    atualizarHistorico(chamadasFinal);
                }
            });

        } catch (Exception e) {
            // Se for erro de tabela não existente, marcar para não tentar mais
            if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                erroTabelaReportado = true;
            }
            // Ignorar erro silenciosamente para não poluir logs em loop
        }
    }

    private void piscarSenhaAtual() {
        // Animação de piscar para chamar atenção
        Timeline piscar = new Timeline(
                new KeyFrame(Duration.millis(0), e -> areaSenhaAtual.setStyle(
                        "-fx-background-color: #e94560; -fx-background-radius: 15; -fx-padding: 40;")),
                new KeyFrame(Duration.millis(200), e -> areaSenhaAtual.setStyle(
                        "-fx-background-color: #0f3460; -fx-background-radius: 15; -fx-padding: 40;")),
                new KeyFrame(Duration.millis(400), e -> areaSenhaAtual.setStyle(
                        "-fx-background-color: #e94560; -fx-background-radius: 15; -fx-padding: 40;")),
                new KeyFrame(Duration.millis(600), e -> areaSenhaAtual.setStyle(
                        "-fx-background-color: #0f3460; -fx-background-radius: 15; -fx-padding: 40;")),
                new KeyFrame(Duration.millis(800), e -> areaSenhaAtual.setStyle(
                        "-fx-background-color: #e94560; -fx-background-radius: 15; -fx-padding: 40;")),
                new KeyFrame(Duration.millis(1000), e -> areaSenhaAtual.setStyle(
                        "-fx-background-color: #0f3460; -fx-background-radius: 15; -fx-padding: 40;")));
        piscar.play();
    }

    private void atualizarHistorico(List<ChamadaInfo> chamadas) {
        containerHistorico.getChildren().clear();

        // Pegar até 5 senhas do histórico (excluindo a primeira que é a atual)
        int inicio = chamadas.size() > 1 ? 1 : 0;
        int fim = Math.min(chamadas.size(), 6);

        for (int i = inicio; i < fim; i++) {
            ChamadaInfo chamada = chamadas.get(i);
            VBox card = criarCardHistorico(chamada);
            containerHistorico.getChildren().add(card);
        }
    }

    private VBox criarCardHistorico(ChamadaInfo chamada) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10; -fx-padding: 15; -fx-min-width: 150;");

        // Prefixo + Número da senha
        Label lblSenha = new Label(chamada.senha);
        lblSenha.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24; -fx-font-weight: bold;");
        lblSenha.setFont(Font.font("Arial Bold", 24));

        // Guichê
        Label lblGuiche = new Label("Guichê " + chamada.guiche);
        lblGuiche.setStyle("-fx-text-fill: #94b3fd; -fx-font-size: 14;");
        lblGuiche.setFont(Font.font("Arial", 14));

        card.getChildren().addAll(lblSenha, lblGuiche);

        return card;
    }

    public void pararAtualizacao() {
        if (atualizacaoTimeline != null) {
            atualizacaoTimeline.stop();
        }
        if (dataHoraTimeline != null) {
            dataHoraTimeline.stop();
        }
        if (feedTimeline != null) {
            feedTimeline.stop();
        }
        if (feedTransition != null) {
            feedTransition.stop();
        }
        System.out.println("Atualização do painel parada.");
    }
}
