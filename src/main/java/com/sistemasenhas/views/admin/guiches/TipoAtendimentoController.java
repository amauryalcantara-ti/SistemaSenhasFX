package com.sistemasenhas.views.admin.guiches;

import com.sistemasenhas.factory.BotaoAtendimentoFactory;
import com.sistemasenhas.models.TipoAtendimento;
import com.sistemasenhas.services.TipoAtendimentoService;
import com.sistemasenhas.services.GuicheService;
import com.sistemasenhas.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TipoAtendimentoController {

    @FXML
    private TableView<TipoAtendimento> tvServicos;
    @FXML
    private TableColumn<TipoAtendimento, String> colPrefixo;
    @FXML
    private TableColumn<TipoAtendimento, String> colDescricao;

    @FXML
    private StackPane panePreview;
    @FXML
    private TextField txtDescricao;
    @FXML
    private ComboBox<String> cbFormato;
    @FXML
    private ColorPicker cpFundo;
    @FXML
    private ColorPicker cpTexto;
    @FXML
    private ComboBox<String> cbFonte;
    @FXML
    private ImageView imgPreview;

    @FXML
    private ListView<String> lvLocais;
    @FXML
    private ListView<String> lvGuiches;

    @FXML
    private ListView<TipoAtendimento> lvServicosLocal;

    @FXML
    private FlowPane fpServicosHabilitados;

    @FXML
    private Button btnAdicionarHabilitado;

    private TipoAtendimentoService service = new TipoAtendimentoService();
    private GuicheService guicheService = new GuicheService();
    private TipoAtendimento servicoSelecionado;
    private Set<Integer> linkedServiceIds = new HashSet<>();

    @FXML
    public void initialize() {
        configurarTabela();
        configurarListView();
        carregarCombos();
        atualizarLista();
        configurarListenersPreview();

        carregarLocaisAtendimento();

        // Problema 2: Listener para filtrar guichês ao selecionar um local
        lvLocais.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lvGuiches.getSelectionModel().clearSelection(); // Limpa seleção de guichê para não confundir a visão
                carregarGuichesPorLocal(newVal);
                carregarServicosHabilitadosNoLocal(newVal); // Carrega visualização por local
            }
        });

        // Listener para carregar botões e estado dos checkboxes quando um guichê for
        // selecionado
        lvGuiches.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                carregarBotoesHabilitados(newVal);
                carregarEstadoServicosParaGuiche(newVal);
            } else {
                linkedServiceIds.clear();
                if (lvServicosLocal != null)
                    lvServicosLocal.refresh();
            }
        });

        tvServicos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                servicoSelecionado = newSelection;
                exibirDetalhes(newSelection);
                atualizarPreview();
            }
        });
    }

    private void carregarLocaisAtendimento() {
        List<String> locais = new ArrayList<>();
        String sql = "SELECT nome FROM local_atendimento WHERE ativo = 1 ORDER BY nome ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                locais.add(rs.getString("nome"));
            }
            lvLocais.setItems(FXCollections.observableArrayList(locais));
        } catch (SQLException e) {
            System.err.println("Erro ao carregar locais: " + e.getMessage());
        }
    }

    private void carregarGuichesPorLocal(String nomeLocal) {
        List<String> guiches = new ArrayList<>();
        String sql = "SELECT numero FROM guiches WHERE localizacao = ? AND ativo = 1 ORDER BY numero ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeLocal);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    guiches.add("Guichê " + rs.getString("numero"));
                }
            }
            lvGuiches.setItems(FXCollections.observableArrayList(guiches));
        } catch (SQLException e) {
            System.err.println("Erro ao carregar guichês: " + e.getMessage());
        }
    }

    private void carregarServicosHabilitadosNoLocal(String nomeLocal) {
        fpServicosHabilitados.getChildren().clear();
        if (lvServicosLocal != null) {
            lvServicosLocal.getItems().clear();
        }
        String sql = "SELECT ls.*, ta.prefixo, ta.descricao as nome_original FROM local_servicos ls " +
                "JOIN tipos_atendimento ta ON ls.servico_id = ta.id " +
                "WHERE ls.localizacao = ? AND ls.ativo = 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeLocal);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TipoAtendimento t = new TipoAtendimento();
                t.setId(rs.getInt("id")); // ID de local_servicos
                t.setDescricao(rs.getString("nome_original"));
                t.setCorFundo(rs.getString("cor_fundo"));
                t.setCorTexto(rs.getString("cor_texto"));
                t.setFormatoBotao(rs.getString("formato_botao"));
                t.setEstiloFonte(rs.getString("estilo_fonte"));
                t.setTamanhoFonte(rs.getInt("tamanho_fonte"));
                t.setCaminhoIcone(rs.getString("caminho_icone"));

                Button btn = BotaoAtendimentoFactory.gerarBotao(t);
                btn.setScaleX(0.85);
                btn.setScaleY(0.85);

                // Clique para vincular ao guichê se houver um selecionado
                btn.setOnAction(e -> vincularAoGuiche(t.getId()));

                fpServicosHabilitados.getChildren().add(btn);
                if (lvServicosLocal != null) {
                    lvServicosLocal.getItems().add(t);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar serviços do local: " + e.getMessage());
        }
    }

    private void carregarBotoesHabilitados(String identificadorGuiche) {
        fpServicosHabilitados.getChildren().clear();

        String numeroGuiche = identificadorGuiche.replace("Guichê ", "").trim();
        String localSel = lvLocais.getSelectionModel().getSelectedItem();

        // SQL corrigido para buscar da local_servicos com o vínculo da guiche_servicos
        String sql = "SELECT ls.*, ta.descricao as nome_original FROM local_servicos ls " +
                "JOIN guiche_servicos gs ON ls.id = gs.local_servico_id " +
                "JOIN guiches g ON g.id = gs.guiche_id " +
                "JOIN tipos_atendimento ta ON ls.servico_id = ta.id " +
                "WHERE g.numero = ? AND g.localizacao = ? AND gs.ativo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroGuiche);
            stmt.setString(2, localSel);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TipoAtendimento tipo = new TipoAtendimento();
                tipo.setId(rs.getInt("id"));
                tipo.setDescricao(rs.getString("nome_original"));
                tipo.setCorFundo(rs.getString("cor_fundo"));
                tipo.setCorTexto(rs.getString("cor_texto"));
                tipo.setFormatoBotao(rs.getString("formato_botao"));
                tipo.setEstiloFonte(rs.getString("estilo_fonte"));
                tipo.setTamanhoFonte(rs.getInt("tamanho_fonte"));
                tipo.setCaminhoIcone(rs.getString("caminho_icone"));

                Button btn = BotaoAtendimentoFactory.gerarBotao(tipo);
                btn.setScaleX(0.9);
                btn.setScaleY(0.9);

                // Clique para desvincular
                btn.setOnAction(e -> desvincularDoGuiche(tipo.getId()));

                fpServicosHabilitados.getChildren().add(btn);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar botões habilitados: " + e.getMessage());
        }
    }

    private void configurarTabela() {
        colPrefixo.setCellValueFactory(new PropertyValueFactory<>("prefixo"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        tvServicos.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    private void carregarCombos() {
        cbFormato.setItems(FXCollections.observableArrayList(
                "RETANGULAR-ARREDONDADO",
                "RETANGULAR",
                "QUADRADO",
                "QUADRADO-ARREDONDADO",
                "CIRCULAR"));
        cbFonte.setItems(FXCollections.observableArrayList("Segoe UI", "Arial", "Roboto", "Verdana"));
        cpFundo.setValue(Color.WHITE);
        cpTexto.setValue(Color.BLACK);
    }

    private void configurarListenersPreview() {
        txtDescricao.textProperty().addListener((obs, oldVal, newVal) -> atualizarPreview());
        cbFormato.setOnAction(e -> atualizarPreview());
        cbFonte.setOnAction(e -> atualizarPreview());
        cpFundo.setOnAction(e -> atualizarPreview());
        cpTexto.setOnAction(e -> atualizarPreview());
    }

    private void configurarListView() {
        if (lvServicosLocal == null)
            return;
        lvServicosLocal.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // célula com checkbox inline para vincular/desvincular de um guichê selecionado
        lvServicosLocal.setCellFactory(lv -> new ListCell<TipoAtendimento>() {
            private CheckBox cb = new CheckBox();

            @Override
            protected void updateItem(TipoAtendimento item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String prefixo = item.getPrefixo() != null ? item.getPrefixo() + " - " : "";
                    setText(prefixo + item.getDescricao());
                    cb.setSelected(linkedServiceIds.contains(item.getId()));

                    cb.setOnAction(e -> {
                        String guicheSel = lvGuiches.getSelectionModel().getSelectedItem();
                        String localSel = lvLocais.getSelectionModel().getSelectedItem();
                        if (guicheSel == null || localSel == null) {
                            cb.setSelected(!cb.isSelected());
                            mostrarAlerta("Atenção", "Selecione um local e um guichê primeiro.");
                            return;
                        }
                        int guicheId = obterIdGuiche(guicheSel, localSel);
                        if (guicheId <= 0) {
                            cb.setSelected(!cb.isSelected());
                            mostrarAlerta("Erro", "Guichê não encontrado.");
                            return;
                        }

                        if (cb.isSelected()) {
                            if (guicheService.vincularServicoAoGuiche(guicheId, item.getId())) {
                                linkedServiceIds.add(item.getId());
                            } else {
                                cb.setSelected(true);
                            }
                        } else {
                            if (guicheService.desvincularServicoDoGuiche(guicheId, item.getId())) {
                                linkedServiceIds.remove(item.getId());
                            } else {
                                cb.setSelected(false);
                            }
                        }

                        carregarBotoesHabilitados(lvGuiches.getSelectionModel().getSelectedItem());
                    });

                    setGraphic(cb);
                }
            }
        });
    }

    /**
     * Carrega os ids de serviços vinculados ao guichê selecionado e atualiza
     * checkboxes
     */
    private void carregarEstadoServicosParaGuiche(String guicheIdent) {
        linkedServiceIds.clear();
        String numeroGuiche = guicheIdent.replace("Guichê ", "").trim();
        String localSel = lvLocais.getSelectionModel().getSelectedItem();
        if (localSel == null)
            return;

        String sql = "SELECT ls.id FROM guiche_servicos gs JOIN guiches g ON gs.guiche_id = g.id "
                + "JOIN local_servicos ls ON gs.local_servico_id = ls.id "
                + "WHERE g.numero = ? AND g.localizacao = ? AND gs.ativo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroGuiche);
            stmt.setString(2, localSel);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    linkedServiceIds.add(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar estado dos serviços por guichê: " + e.getMessage());
        }

        if (lvServicosLocal != null)
            lvServicosLocal.refresh();
    }

    private void atualizarPreview() {
        if (panePreview == null)
            return;

        TipoAtendimento temp = new TipoAtendimento();
        temp.setDescricao(txtDescricao.getText());
        temp.setFormatoBotao(cbFormato.getValue());
        temp.setEstiloFonte(cbFonte.getValue());
        temp.setTamanhoFonte(14);
        temp.setCorFundo(toHexString(cpFundo.getValue()));
        temp.setCorTexto(toHexString(cpTexto.getValue()));

        if (servicoSelecionado != null) {
            temp.setCaminhoIcone(servicoSelecionado.getCaminhoIcone());
        }

        Button botaoPreview = BotaoAtendimentoFactory.gerarBotao(temp);
        panePreview.getChildren().clear();
        panePreview.getChildren().add(botaoPreview);
    }

    private void atualizarLista() {
        try {
            tvServicos.setItems(FXCollections.observableArrayList(service.listarTodos()));
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao carregar banco: " + e.getMessage());
        }
    }

    private void exibirDetalhes(TipoAtendimento tipo) {
        txtDescricao.setText(tipo.getDescricao());
        cbFormato.setValue(tipo.getFormatoBotao());
        cbFonte.setValue(tipo.getEstiloFonte());

        if (tipo.getCorFundo() != null)
            cpFundo.setValue(Color.web(tipo.getCorFundo()));
        if (tipo.getCorTexto() != null)
            cpTexto.setValue(Color.web(tipo.getCorTexto()));

        if (tipo.getCaminhoIcone() != null) {
            File file = new File("src/main/resources/icones_atendimento/" + tipo.getCaminhoIcone());
            if (file.exists())
                imgPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void selecionarIcone() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Ícones de Atendimento");
        File pasta = new File("src/main/resources/icones_atendimento");
        if (pasta.exists())
            fc.setInitialDirectory(pasta);

        File selecionado = fc.showOpenDialog(txtDescricao.getScene().getWindow());
        if (selecionado != null) {
            imgPreview.setImage(new Image(selecionado.toURI().toString()));
            if (servicoSelecionado != null) {
                servicoSelecionado.setCaminhoIcone(selecionado.getName());
                atualizarPreview();
            }
        }
    }

    @FXML
    private void novo() {
        limpar();
        tvServicos.getSelectionModel().clearSelection();
        servicoSelecionado = null;
    }

    @FXML
    private void limpar() {
        txtDescricao.clear();
        cbFormato.setValue("RETANGULAR-ARREDONDADO");
        cpFundo.setValue(Color.WHITE);
        cpTexto.setValue(Color.BLACK);
        imgPreview.setImage(null);
        panePreview.getChildren().clear();
    }

    @FXML
    private void excluir() {
        String localSel = lvLocais.getSelectionModel().getSelectedItem();
        if (servicoSelecionado == null || localSel == null) {
            mostrarAlerta("Aviso", "Selecione um serviço e um local para excluir o vínculo.");
            return;
        }
        String sql = "DELETE FROM local_servicos WHERE localizacao = ? AND servico_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, localSel);
            stmt.setInt(2, servicoSelecionado.getId());
            stmt.executeUpdate();
            carregarServicosHabilitadosNoLocal(localSel);
            mostrarAlerta("Sucesso", "Vínculo removido do local.");
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao excluir: " + e.getMessage());
        }
    }

    @FXML
    private void salvar() {
        String localSel = lvLocais.getSelectionModel().getSelectedItem();
        if (servicoSelecionado == null || localSel == null) {
            mostrarAlerta("Aviso", "Selecione um serviço e um local.");
            return;
        }

        // Adicionado ON DUPLICATE KEY UPDATE e coluna ativo=1
        String sql = "INSERT INTO local_servicos (localizacao, servico_id, cor_fundo, cor_texto, formato_botao, estilo_fonte, caminho_icone, ativo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 1) ON DUPLICATE KEY UPDATE "
                + "cor_fundo=VALUES(cor_fundo), cor_texto=VALUES(cor_texto), formato_botao=VALUES(formato_botao), "
                + "caminho_icone=VALUES(caminho_icone), estilo_fonte=VALUES(estilo_fonte), ativo=1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, localSel);
            stmt.setInt(2, servicoSelecionado.getId());
            stmt.setString(3, toHexString(cpFundo.getValue()));
            stmt.setString(4, toHexString(cpTexto.getValue()));
            stmt.setString(5, cbFormato.getValue());
            stmt.setString(6, cbFonte.getValue());
            stmt.setString(7, servicoSelecionado.getCaminhoIcone());

            stmt.executeUpdate();

            // Atualiza a visualização imediata
            carregarServicosHabilitadosNoLocal(localSel);

            // Recarrega os botões se houver um guichê selecionado
            String guicheSel = lvGuiches.getSelectionModel().getSelectedItem();
            if (guicheSel != null) {
                carregarBotoesHabilitados(guicheSel);
            }

            mostrarAlerta("Sucesso", "Configuração salva para o local " + localSel);
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    public void salvareLimpar() {
        salvar();
        // Limpa todos os campos de formatação
        txtDescricao.clear();
        cbFormato.setValue(null);
        cpFundo.setValue(Color.WHITE);
        cpTexto.setValue(Color.BLACK);
        cbFonte.setValue(null);
        imgPreview.setImage(null);
        panePreview.getChildren().clear();
        servicoSelecionado = null;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void vincularAoGuiche(int localServicoId) {
        String guicheSel = lvGuiches.getSelectionModel().getSelectedItem();
        String localSel = lvLocais.getSelectionModel().getSelectedItem();
        if (guicheSel == null)
            return;

        int guicheId = obterIdGuiche(guicheSel, localSel);
        if (guicheId > 0 && guicheService.vincularServicoAoGuiche(guicheId, localServicoId)) {
            carregarBotoesHabilitados(guicheSel);
        }
    }

    private void desvincularDoGuiche(int localServicoId) {
        String guicheSel = lvGuiches.getSelectionModel().getSelectedItem();
        String localSel = lvLocais.getSelectionModel().getSelectedItem();
        if (guicheSel == null)
            return;

        int guicheId = obterIdGuiche(guicheSel, localSel);
        if (guicheId > 0 && guicheService.desvincularServicoDoGuiche(guicheId, localServicoId)) {
            carregarBotoesHabilitados(guicheSel);
        }
    }

    private int obterIdGuiche(String ident, String local) {
        String num = ident.replace("Guichê ", "").trim();
        String sql = "SELECT id FROM guiches WHERE numero = ? AND localizacao = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, num);
            stmt.setString(2, local);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @FXML
    private void adicionarAosHabilitados() {
        if (servicoSelecionado == null) {
            mostrarAlerta("Atenção", "Por favor, selecione um serviço na tabela primeiro.");
            return;
        }

        String guicheSel = lvGuiches.getSelectionModel().getSelectedItem();
        String localSel = lvLocais.getSelectionModel().getSelectedItem();

        if (guicheSel == null) {
            mostrarAlerta("Atenção", "Por favor, selecione um guichê primeiro.");
            return;
        }

        if (localSel == null) {
            mostrarAlerta("Atenção", "Por favor, selecione um local primeiro.");
            return;
        }

        // Tenta vincular o serviço ao guichê
        int localServicoId = servicoSelecionado.getId();
        int guicheId = obterIdGuiche(guicheSel, localSel);

        if (guicheId > 0) {
            if (guicheService.vincularServicoAoGuiche(guicheId, localServicoId)) {
                mostrarAlerta("Sucesso", "Serviço '" + servicoSelecionado.getDescricao()
                        + "' vinculado ao " + guicheSel + "!");
                // Recarrega os botões habilitados para mostrar a adição
                carregarBotoesHabilitados(guicheSel);
            } else {
                mostrarAlerta("Erro", "O serviço já está vinculado a este guichê.");
            }
        } else {
            mostrarAlerta("Erro", "Guichê não encontrado.");
        }
    }
}