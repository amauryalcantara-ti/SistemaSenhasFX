package com.sistemasenhas.views.atendente;

import com.sistemasenhas.SessionManager;
import com.sistemasenhas.database.DatabaseConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.Duration;
import java.util.*;

public class AtendenteController {

    // Componentes da interface
    @FXML
    private Label lblNomeSistema;
    @FXML
    private Label lblNomeEmpresa;
    @FXML
    private Label lblNomeUsuario;
    @FXML
    private Label lblTempoAtividade;
    @FXML
    private Label lblDataHoraCompleta;
    @FXML
    private VBox containerServicos;
    @FXML
    private VBox containerUltimasSenhas;
    @FXML
    private VBox containerQuantidadePorTipo;

    // Botões de controle
    @FXML
    private Button btnPausarLogin;
    @FXML
    private Button btnRetomarLogin;
    @FXML
    private Button btnEncerrarAtendimento;

    // Variáveis de controle
    private Timeline relogio;
    private Timeline contadorTempo;
    private LocalDateTime horaLogin;
    private boolean loginPausado = false;
    private String senhaAtualChamada = null;
    private int guicheId = -1;
    private List<Map<String, Object>> servicosGuiche = new ArrayList<>();
    private List<Map<String, Object>> ultimasSenhas = new ArrayList<>();
    private PopupChamadaController popupChamadaController;

    @FXML
    public void initialize() {
        System.out.println("✅ AtendenteController inicializado");

        // Carregar informações do sistema
        carregarInformacoesSistema();

        // Carregar informações do usuário
        carregarInformacoesUsuario();

        // Verificar alocação do guichê ANTES de continuar
        if (!verificarAlocacaoGuiche()) {
            // Se não tiver alocação, não continuar com o restante
            return;
        }

        // Carregar serviços do guichê
        carregarServicosGuiche();

        // Iniciar relógio
        iniciarRelogio();

        // Iniciar contador de tempo
        iniciarContadorTempo();

        // Configurar eventos
        configurarEventos();
    }

    private boolean verificarAlocacaoGuiche() {
        int usuarioId = SessionManager.getInstance().getIdUsuario();

        System.out.println("🔍 Verificando alocação para usuário ID: " + usuarioId);
        System.out.println("🔍 Nome de usuário: " + SessionManager.getInstance().getNomeUsuario());

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT g.id, g.numero, g.descricao, l.nome as localizacao " +
                                "FROM alocacoes_diarias ad " +
                                "JOIN guiches g ON ad.guiche_id = g.id " +
                                "JOIN local_atendimento l ON g.localizacao = l.nome " +
                                "WHERE ad.usuario_id = ? AND ad.data_alocacao = CURDATE() AND ad.ativo = 1")) {

            stmt.setInt(1, usuarioId);
            System.out.println("🔍 Executando query: " + stmt.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                guicheId = rs.getInt("id");
                String numeroGuiche = rs.getString("numero");
                String localizacao = rs.getString("localizacao");

                System.out.println("✅ Guichê " + numeroGuiche + " (" + localizacao + ") alocado para o atendente");
                return true;
            } else {
                System.out.println("⚠️ Nenhuma alocação encontrada para o atendente hoje");
                System.out.println("🔍 Query executada mas não retornou resultados");

                // Vamos verificar se há alocações para este usuário em qualquer data
                try (PreparedStatement stmt2 = conn.prepareStatement(
                        "SELECT ad.data_alocacao, ad.ativo, g.numero FROM alocacoes_diarias ad " +
                                "JOIN guiches g ON ad.guiche_id = g.id WHERE ad.usuario_id = ?")) {

                    stmt2.setInt(1, usuarioId);
                    ResultSet rs2 = stmt2.executeQuery();

                    System.out.println("🔍 Todas as alocações do usuário:");
                    while (rs2.next()) {
                        System.out.println("   - Data: " + rs2.getDate("data_alocacao") +
                                ", Ativo: " + rs2.getBoolean("ativo") +
                                ", Guichê: " + rs2.getString("numero"));
                    }
                }

                mostrarAlertaERedirecionar();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao carregar alocação do guichê: " + e.getMessage());
            e.printStackTrace();
            mostrarAlertaERedirecionar();
            return false;
        }
    }

    private void carregarInformacoesSistema() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT empresa_nome FROM tb_config_sistema WHERE id = 1");
            if (rs.next()) {
                String nomeEmpresa = rs.getString("empresa_nome");
                lblNomeSistema.setText("SISTEMA DE GERENCIAMENTO DE SENHAS");
                lblNomeEmpresa.setText(nomeEmpresa);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao carregar informações do sistema: " + e.getMessage());
            lblNomeSistema.setText("SISTEMA DE GERENCIAMENTO DE SENHAS");
            lblNomeEmpresa.setText("EMPRESA");
        }
    }

    private void carregarInformacoesUsuario() {
        String nomeUsuario = SessionManager.getInstance().getNomeUsuario();
        if (nomeUsuario != null && !nomeUsuario.isEmpty()) {
            lblNomeUsuario.setText("Usuário: " + nomeUsuario);
        } else {
            lblNomeUsuario.setText("Usuário: Não identificado");
        }
    }

    private void mostrarAlertaERedirecionar() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Acesso Negado");
        alert.setHeaderText("Usuário não alocado a nenhum guichê hoje");
        alert.setContentText("Você não está alocado a nenhum guichê para hoje.\n\n" +
                "Por favor, procure um supervisor ou administrador para solicitar sua alocação.\n\n" +
                "Você será redirecionado para a tela de login.");
        alert.showAndWait();

        // Redirecionar para a tela de login após fechar o alerta
        // Usar Platform.runLater para garantir que a cena esteja carregada
        Platform.runLater(this::redirecionarParaLogin);
    }

    private void redirecionarParaLogin() {
        try {
            // Limpar sessão
            SessionManager.getInstance().clearSession();

            // Verificar se a cena está disponível
            if (lblNomeUsuario.getScene() == null) {
                System.err.println("❌ Cena não disponível para redirecionamento");
                return;
            }

            // Carregar tela de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblNomeUsuario.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Sistema de Senhas - Login");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Erro ao redirecionar para login: " + e.getMessage());
        }
    }

    private void carregarServicosGuiche() {
        if (guicheId == -1)
            return;

        System.out.println("🔍 Iniciando carregarServicosGuiche() para guichê ID: " + guicheId);

        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("🔍 Conexão estabelecida com sucesso");

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT ls.*, ta.descricao as servico_desc, ta.prefixo " +
                            "FROM guiche_servicos gs " +
                            "JOIN local_servicos ls ON gs.local_servico_id = ls.id " +
                            "JOIN tipos_atendimento ta ON ls.servico_id = ta.id " +
                            "WHERE gs.guiche_id = ? AND gs.ativo = 1 " +
                            "ORDER BY gs.ordem");

            stmt.setInt(1, guicheId);
            System.out.println("🔍 PreparedStatement criado e parâmetro definido");

            ResultSet rs = stmt.executeQuery();
            System.out.println("🔍 Query executada com sucesso");

            // Primeiro, carregar todos os serviços em uma lista
            List<Map<String, Object>> servicosTemp = new ArrayList<>();
            servicosGuiche.clear();
            containerServicos.getChildren().clear();

            boolean temServicos = false;
            while (rs.next()) {
                temServicos = true;
                System.out.println("🔍 Processando serviço: " + rs.getString("servico_desc"));

                Map<String, Object> servico = new HashMap<>();
                servico.put("id", rs.getInt("id"));
                servico.put("servico_id", rs.getInt("servico_id"));
                servico.put("descricao", rs.getString("servico_desc"));
                servico.put("prefixo", rs.getString("prefixo"));
                servico.put("cor_fundo", rs.getString("cor_fundo"));
                servico.put("cor_texto", rs.getString("cor_texto"));

                servicosTemp.add(servico);
            }

            rs.close();
            stmt.close();
            conn.close();

            // Agora, criar botões para cada serviço
            for (Map<String, Object> servico : servicosTemp) {
                servicosGuiche.add(servico);

                // Criar botão para o serviço
                Button btnServico = criarBotaoServico(servico);
                containerServicos.getChildren().add(btnServico);
            }

            if (!temServicos) {
                Label lblSemServicos = new Label("Nenhum serviço alocado para este guichê hoje");
                lblSemServicos.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-font-style: italic;");
                containerServicos.getChildren().add(lblSemServicos);
            }

            // Atualizar estatísticas
            atualizarEstatisticas();

            System.out.println("✅ carregarServicosGuiche() concluído com sucesso");

        } catch (SQLException e) {
            System.err.println("❌ Erro ao carregar serviços do guichê: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Button criarBotaoServico(Map<String, Object> servico) {
        Button btn = new Button();
        String descricao = (String) servico.get("descricao");
        String corFundo = (String) servico.get("cor_fundo");
        String corTexto = (String) servico.get("cor_texto");
        int servicoId = (Integer) servico.get("servico_id");

        // Contar senhas na fila para este serviço
        int quantidadeSenhas = contarSenhasNaFila(servicoId);

        btn.setText(descricao + "\n[Qtd: " + quantidadeSenhas + "]");
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 12; -fx-font-weight: bold; " +
                        "-fx-pref-height: 60; -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-width: 1;",
                corFundo, corTexto));
        btn.setPrefWidth(350);
        btn.setWrapText(true);
        btn.setOnAction(e -> chamarSenha(servico));

        return btn;
    }

    private int contarSenhasNaFila(int servicoId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) as quantidade " +
                                "FROM atendimentos a " +
                                "WHERE a.tipo_id = ? AND a.status = 'AGUARDANDO' " +
                                "AND DATE(a.hora_emissao) = CURDATE()")) {

            stmt.setInt(1, servicoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantidade");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao contar senhas na fila: " + e.getMessage());
        }
        return 0;
    }

    private void chamarSenha(Map<String, Object> servico) {
        if (loginPausado) {
            mostrarAlerta("Login Pausado", "Retome o login antes de chamar senhas.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT a.senha_codigo, a.id " +
                                "FROM atendimentos a " +
                                "WHERE a.tipo_id = ? AND a.status = 'AGUARDANDO' " +
                                "AND DATE(a.hora_emissao) = CURDATE() " +
                                "ORDER BY a.hora_emissao ASC " +
                                "LIMIT 1")) {

            stmt.setInt(1, (Integer) servico.get("servico_id"));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String senhaCodigo = rs.getString("senha_codigo");
                int atendimentoId = rs.getInt("id");

                // Atualizar status para CHAMADA
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE atendimentos SET status = 'CHAMADA', guiche_id = ?, operador_id = ?, hora_inicio = NOW() "
                                +
                                "WHERE id = ?")) {

                    updateStmt.setInt(1, guicheId);
                    updateStmt.setInt(2, SessionManager.getInstance().getIdUsuario());
                    updateStmt.setInt(3, atendimentoId);
                    updateStmt.executeUpdate();
                }

                senhaAtualChamada = senhaCodigo;
                mostrarPopupChamada(senhaCodigo, servico);

                // Adicionar às últimas senhas
                adicionarUltimaSenha(senhaCodigo, servico);

                // Atualizar interface
                carregarServicosGuiche(); // Recarregar para atualizar quantidades

            } else {
                mostrarAlerta("Sem Senhas", "Não há senhas aguardando para este serviço.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao chamar senha: " + e.getMessage());
        }
    }

    private void mostrarPopupChamada(String senha, Map<String, Object> servico) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/atendente/PopupChamadaView.fxml"));
            Parent root = loader.load();

            PopupChamadaController controller = loader.getController();
            controller.setSenha(senha);
            controller.setServico(servico);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Senha Chamada");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();

            popupChamadaController.setStage(stage);
            popupChamadaController = controller;
        } catch (IOException e) {
            System.err.println("❌ Erro ao carregar popup de chamada: " + e.getMessage());
        }
    }

    private void adicionarUltimaSenha(String senha, Map<String, Object> servico) {
        Map<String, Object> ultimaSenha = new HashMap<>();
        ultimaSenha.put("senha", senha);
        ultimaSenha.put("servico", servico.get("descricao"));
        ultimaSenha.put("hora", LocalDateTime.now());

        ultimasSenhas.add(0, ultimaSenha);

        // Manter apenas as 5 últimas
        if (ultimasSenhas.size() > 5) {
            ultimasSenhas.remove(ultimasSenhas.size() - 1);
        }

        atualizarUltimasSenhas();
    }

    private void atualizarUltimasSenhas() {
        containerUltimasSenhas.getChildren().clear();

        if (ultimasSenhas.isEmpty()) {
            Label lblVazia = new Label("Nenhuma senha chamada ainda");
            lblVazia.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11; -fx-font-style: italic;");
            containerUltimasSenhas.getChildren().add(lblVazia);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Map<String, Object> senha : ultimasSenhas) {
            HBox hboxSenha = new HBox(5);
            hboxSenha.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5; -fx-background-radius: 3;");

            Label lblSenha = new Label((String) senha.get("senha"));
            lblSenha.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

            Label lblHora = new Label(((LocalDateTime) senha.get("hora")).format(formatter));
            lblHora.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10;");

            hboxSenha.getChildren().addAll(lblSenha, lblHora);
            containerUltimasSenhas.getChildren().add(hboxSenha);
        }
    }

    private void atualizarEstatisticas() {
        containerQuantidadePorTipo.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(
                    "SELECT ta.descricao, COUNT(*) as quantidade " +
                            "FROM atendimentos a " +
                            "JOIN tipos_atendimento ta ON a.tipo_id = ta.id " +
                            "WHERE DATE(a.hora_emissao) = CURDATE() " +
                            "GROUP BY ta.descricao " +
                            "ORDER BY quantidade DESC")) {

                boolean temDados = false;
                while (rs.next()) {
                    temDados = true;
                    String tipo = rs.getString("descricao");
                    int quantidade = rs.getInt("quantidade");

                    HBox hbox = new HBox(10);
                    hbox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5; -fx-background-radius: 3;");

                    Label lblTipo = new Label(tipo + ":");
                    lblTipo.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");

                    Label lblQuantidade = new Label(String.valueOf(quantidade));
                    lblQuantidade.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-font-size: 12;");

                    hbox.getChildren().addAll(lblTipo, lblQuantidade);
                    containerQuantidadePorTipo.getChildren().add(hbox);
                }

                if (!temDados) {
                    Label lblVazia = new Label("Nenhuma senha emitida hoje");
                    lblVazia.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11; -fx-font-style: italic;");
                    containerQuantidadePorTipo.getChildren().add(lblVazia);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao atualizar estatísticas: " + e.getMessage());
        }
    }

    private void iniciarRelogio() {
        relogio = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> atualizarDataHora()));
        relogio.setCycleCount(Timeline.INDEFINITE);
        relogio.play();
    }

    private void atualizarDataHora() {
        LocalDateTime agora = LocalDateTime.now();
        Locale ptBR = new Locale("pt", "BR");

        String diaSemana = agora.getDayOfWeek().getDisplayName(TextStyle.FULL, ptBR);
        diaSemana = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);
        String dia = String.valueOf(agora.getDayOfMonth());
        String mes = agora.getMonth().getDisplayName(TextStyle.FULL, ptBR);
        mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
        String ano = String.valueOf(agora.getYear());
        String hora = String.format("%02d:%02d", agora.getHour(), agora.getMinute());

        lblDataHoraCompleta.setText(String.format("%s - %s de %s de %s - %s",
                diaSemana, dia, mes, ano, hora));
    }

    private void iniciarContadorTempo() {
        horaLogin = LocalDateTime.now();
        contadorTempo = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> atualizarContador()));
        contadorTempo.setCycleCount(Timeline.INDEFINITE);
        contadorTempo.play();
    }

    private void atualizarContador() {
        if (loginPausado)
            return;

        LocalDateTime agora = LocalDateTime.now();
        long segundos = Duration.between(horaLogin, agora).getSeconds();

        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long seg = segundos % 60;

        lblTempoAtividade.setText(String.format("Tempo ativo: %02d:%02d:%02d", horas, minutos, seg));
    }

    private void configurarEventos() {
        btnPausarLogin.setOnAction(e -> handlePausarLogin());
        btnRetomarLogin.setOnAction(e -> handleRetomarLogin());
        btnEncerrarAtendimento.setOnAction(e -> handleEncerrarAtendimento());
    }

    @FXML
    private void handlePausarLogin() {
        loginPausado = true;
        btnPausarLogin.setDisable(true);
        btnRetomarLogin.setDisable(false);

        // Desabilitar botões de serviços
        containerServicos.setDisable(true);

        mostrarAlerta("Login Pausado", "Seu login foi pausado. Retome para continuar chamando senhas.");
    }

    @FXML
    private void handleRetomarLogin() {
        loginPausado = false;
        btnPausarLogin.setDisable(false);
        btnRetomarLogin.setDisable(true);

        // Habilitar botões de serviços
        containerServicos.setDisable(false);

        mostrarAlerta("Login Retomado", "Seu login foi retomado. Você já pode chamar senhas.");
    }

    @FXML
    private void handleEncerrarAtendimento() {
        if (senhaAtualChamada == null) {
            mostrarAlerta("Sem Atendimento", "Não há atendimento em andamento para encerrar.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE atendimentos SET status = 'FINALIZADO', hora_fim = NOW(), " +
                                "tempo_atendimento = TIMESTAMPDIFF(SECOND, hora_inicio, NOW()) " +
                                "WHERE senha_codigo = ? AND guiche_id = ? AND status = 'CHAMADA'")) {

            stmt.setString(1, senhaAtualChamada);
            stmt.setInt(2, guicheId);
            stmt.executeUpdate();

            senhaAtualChamada = null;
            btnEncerrarAtendimento.setDisable(true);

            mostrarAlerta("Atendimento Encerrado", "Atendimento encerrado com sucesso.");

            // Atualizar estatísticas
            atualizarEstatisticas();

        } catch (SQLException e) {
            System.err.println("❌ Erro ao encerrar atendimento: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}