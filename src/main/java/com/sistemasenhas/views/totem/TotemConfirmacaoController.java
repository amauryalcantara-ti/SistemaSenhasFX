package com.sistemasenhas.views.totem;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javax.print.DocPrintJob;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.sistemasenhas.database.DatabaseConnection;

public class TotemConfirmacaoController {

    @FXML
    private Label lblSenha;

    @FXML
    private Label lblServico;

    @FXML
    private Label lblLocal;

    @FXML
    private Label lblDataHora;

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblTipoServico;

    @FXML
    private Label lblTempoEspera;

    @FXML
    private Label lblMensagem;

    @FXML
    private Button btnOK;

    private String senha;
    private String servico;
    private String local;
    private int serviceId;
    private int tempoEsperaMinutos = 15; // padrão

    @FXML
    public void initialize() {
        System.out.println("TotemConfirmacaoController inicializado");
    }

    public void setDados(String senha, String servico, String local, int serviceId) {
        this.senha = senha;
        this.servico = servico;
        this.local = local;
        this.serviceId = serviceId;

        lblSenha.setText(senha);
        lblServico.setText(servico);
        lblLocal.setText(local);

        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        lblDataHora.setText(dataHora);

        // Calcular tempo de espera baseado na fila
        calcularTempoEspera();
        lblTipoServico.setText(servico + ":");
        if (lblTempoEspera != null) {
            lblTempoEspera.setText(tempoEsperaMinutos + " minutos");
        }

        carregarNomeEmpresa();
    }

    private void calcularTempoEspera() {
        try {
            // Buscar tempo médio por atendimento do serviço
            String sqlTempo = "SELECT COALESCE(tempo_medio_estimado, 5) as tempo_medio FROM tipos_atendimento WHERE id = ?";
            java.sql.PreparedStatement pstmtTempo = DatabaseConnection.getConnection().prepareStatement(sqlTempo);
            pstmtTempo.setInt(1, serviceId);
            java.sql.ResultSet rsTempo = pstmtTempo.executeQuery();

            int tempoMedioPorAtendimento = 5; // padrão 5 minutos
            if (rsTempo.next()) {
                tempoMedioPorAtendimento = rsTempo.getInt("tempo_medio");
                if (tempoMedioPorAtendimento <= 0)
                    tempoMedioPorAtendimento = 5;
            }
            rsTempo.close();
            pstmtTempo.close();

            // Contar quantas senhas estão aguardando (status 'A') para este serviço hoje
            String sqlFila = "SELECT COUNT(*) as fila FROM atendimentos WHERE tipo_id = ? AND status = 'A' AND DATE(hora_emissao) = CURDATE()";
            java.sql.PreparedStatement pstmtFila = DatabaseConnection.getConnection().prepareStatement(sqlFila);
            pstmtFila.setInt(1, serviceId);
            java.sql.ResultSet rsFila = pstmtFila.executeQuery();

            int pessoasNaFila = 0;
            if (rsFila.next()) {
                pessoasNaFila = rsFila.getInt("fila");
            }
            rsFila.close();
            pstmtFila.close();

            // Calcular tempo: (pessoas na fila - 1) * tempo médio (a pessoa atual já está
            // na fila)
            int pessoasAntes = Math.max(0, pessoasNaFila - 1);
            tempoEsperaMinutos = pessoasAntes * tempoMedioPorAtendimento;

            // Mínimo de 1 minuto se houver fila, ou mostrar "< 1 minuto"
            if (tempoEsperaMinutos == 0 && pessoasNaFila > 0) {
                tempoEsperaMinutos = 1;
            }

            System.out.println("Tempo de espera calculado: " + tempoEsperaMinutos + " min (" + pessoasAntes
                    + " pessoas x " + tempoMedioPorAtendimento + " min)");

        } catch (Exception e) {
            System.err.println("Erro ao calcular tempo de espera: " + e.getMessage());
            tempoEsperaMinutos = 15; // fallback
        }
    }

    private void carregarNomeEmpresa() {
        try {
            String sql = "SELECT empresa_nome FROM tb_config_sistema LIMIT 1";
            java.sql.Statement stmt = DatabaseConnection.getConnection().createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                String nomeEmpresa = rs.getString("empresa_nome");
                if (nomeEmpresa != null && !nomeEmpresa.trim().isEmpty()) {
                    lblTitulo.setText(nomeEmpresa);
                    System.out.println("Nome da empresa carregado: " + nomeEmpresa);
                } else {
                    lblTitulo.setText("SISTEMA DE SENHAS");
                    System.out.println("Nome da empresa nulo/vazio, usando padrão");
                }
            } else {
                lblTitulo.setText("SISTEMA DE SENHAS");
                System.out.println("Nenhum registro encontrado em tb_config_sistema");
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            System.err.println("Erro ao carregar nome da empresa: " + e.getMessage());
            e.printStackTrace();
            lblTitulo.setText("SISTEMA DE SENHAS");
        }
    }

    @FXML
    private void handleOK() {
        try {
            System.out.println("=== BOTÃO IMPRIMIR CLICADO ===");

            boolean impressoraEncontrada = detectarImpressorasDisponiveis();
            System.out.println("Resultado da detecção de impressoras: " + impressoraEncontrada);

            if (impressoraEncontrada) {
                System.out.println("Impressora encontrada, tentando imprimir diretamente");
                boolean sucessoImprimir = tentarImpressaoDireta();
                System.out.println("Resultado da impressão direta: " + sucessoImprimir);

                if (!sucessoImprimir) {
                    System.out.println("Falha na impressão direta, gerando PDF como fallback");
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Falha ao imprimir diretamente!\n\n" +
                                    "Gerando PDF como alternativa...",
                            "Falha na Impressão",
                            javax.swing.JOptionPane.WARNING_MESSAGE);

                    boolean sucessoPDF = gerarPDFReal();
                    System.out.println("Resultado da geração de PDF: " + sucessoPDF);
                }
            } else {
                System.out.println("Nenhuma impressora encontrada, gerando PDF");
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Impressora não encontrada!\n\n" +
                                "Gerando comprovante em PDF...",
                        "Impressora Não Encontrada",
                        javax.swing.JOptionPane.ERROR_MESSAGE);

                boolean sucessoPDF = gerarPDFReal();
                System.out.println("Resultado da geração de PDF: " + sucessoPDF);
            }

        } catch (Exception e) {
            System.err.println("ERRO EXCEÇÃO no handleOK: " + e.getMessage());
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Erro ao processar o comprovante:\n" + e.getMessage() + "\n\n" +
                            "Por favor, tente novamente ou contate o suporte.",
                    "Erro no Sistema",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }

        Stage stage = (Stage) btnOK.getScene().getWindow();
        stage.close();
    }

    private boolean detectarImpressorasDisponiveis() {
        try {
            System.out.println("Iniciando detecção de impressoras...");

            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

            System.out.println("Impressoras encontradas: " + printServices.length);

            boolean encontrouImpressoraFisica = false;

            for (PrintService service : printServices) {
                System.out.println("Impressora detectada: " + service.getName());
                String nome = service.getName().toLowerCase();

                boolean ehVirtual = nome.contains("pdf") ||
                        nome.contains("onenote") ||
                        nome.contains("xps") ||
                        nome.contains("microsoft") ||
                        nome.contains("send to") ||
                        nome.contains("fax") ||
                        nome.contains("anydesk") ||
                        nome.contains("teamviewer") ||
                        nome.contains("remote") ||
                        nome.contains("virtual");

                if (!ehVirtual) {
                    System.out.println("Impressora física encontrada: " + service.getName());
                    encontrouImpressoraFisica = true;
                }
            }

            System.out.println("Resultado final: " + (encontrouImpressoraFisica ? "Encontrou impressora física"
                    : "Nenhuma impressora física encontrada"));
            return encontrouImpressoraFisica;

        } catch (Exception e) {
            System.err.println("Erro ao detectar impressoras: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean tentarImpressaoDireta() {
        try {
            System.out.println("Iniciando tentativa de impressão direta...");

            String conteudo = gerarConteudoComprovante80mm();

            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

            PrintService impressoraFisica = null;
            for (PrintService service : printServices) {
                String nome = service.getName().toLowerCase();
                boolean ehVirtual = nome.contains("pdf") ||
                        nome.contains("onenote") ||
                        nome.contains("xps") ||
                        nome.contains("microsoft") ||
                        nome.contains("send to") ||
                        nome.contains("fax") ||
                        nome.contains("anydesk") ||
                        nome.contains("teamviewer") ||
                        nome.contains("remote") ||
                        nome.contains("virtual");

                if (!ehVirtual) {
                    impressoraFisica = service;
                    System.out.println("Usando impressora: " + service.getName());
                    break;
                }
            }

            if (impressoraFisica == null) {
                System.out.println("Nenhuma impressora física encontrada para impressão direta");
                return false;
            }

            DocPrintJob printJob = impressoraFisica.createPrintJob();

            byte[] bytes = conteudo.getBytes("ISO-8859-1");
            javax.print.SimpleDoc doc = new javax.print.SimpleDoc(
                    new java.io.ByteArrayInputStream(bytes),
                    DocFlavor.INPUT_STREAM.AUTOSENSE,
                    null);

            HashPrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(MediaSizeName.JAPANESE_POSTCARD);
            attributes.add(OrientationRequested.PORTRAIT);

            printJob.print(doc, attributes);
            System.out.println("Impressão enviada com sucesso para: " + impressoraFisica.getName());

            return true;

        } catch (Exception e) {
            System.err.println("Erro na impressão direta: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String gerarConteudoComprovante80mm() {
        StringBuilder sb = new StringBuilder();

        // Reset impressora
        sb.append("\u001B@");

        // LINHA 1 - NOME DA EMPRESA (OK - fonte normal)
        sb.append("\u001Ba\u0001"); // Centralizar
        sb.append("\u001B!0"); // Fonte normal
        sb.append(lblTitulo.getText()).append("\n");

        // LINHA 2 - SEPARADOR (OK)
        sb.append("------------------------\n");

        // LINHA 3 - SENHA (GRANDE - dobro altura + dobro largura + negrito)
        sb.append("\u001B!\u0038"); // ESC ! 0x38 = dobro altura + dobro largura + negrito
        sb.append(senha).append("\n");
        sb.append("\u001B!0"); // Resetar

        // LINHA 4 - SEPARADOR (mesmo tamanho linha 2)
        sb.append("------------------------\n");

        // LINHAS 5, 6, 7 - SERVIÇO, LOCAL, DATA/HORA
        sb.append("\u001Ba\u0000"); // Alinhar esquerda
        sb.append("Servico: ").append(servico).append("\n");
        sb.append("Local: ").append(local).append("\n");
        sb.append("Data: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")))
                .append("\n");

        // LINHA 8 - TEMPO MEDIO DE ESPERA
        sb.append("\u001Ba\u0001"); // Centralizar
        sb.append("TEMPO MEDIO DE ESPERA:\n");

        // LINHA 9 - SERVIÇO + TEMPO (fonte normal - proporcional)
        sb.append(servico).append(": ").append(tempoEsperaMinutos).append(" min\n");

        // LINHAS 10 e 11 - MENSAGEM FINAL (2 linhas curtas)
        sb.append("Aguarde chamar no painel.\n");
        sb.append("Obrigado!\n");
        sb.append("\n\n\n\n\n"); // Espaço extra para corte

        // Corte de papel
        sb.append("\u001Bm");

        return sb.toString();
    }

    private boolean gerarPDFReal() {
        try {
            String fileName = "comprovante_" + senha.replace(" ", "_") + ".pdf";
            String filePath = System.getProperty("user.dir") + "/" + fileName;

            boolean sucesso = criarPDFSimples(filePath);

            if (sucesso) {
                File pdfFile = new File(filePath);

                if (pdfFile.exists()) {
                    System.out.println("PDF criado com sucesso!");
                    System.out.println("Tamanho: " + pdfFile.length() + " bytes");
                    System.out.println("Caminho completo: " + pdfFile.getAbsolutePath());

                    abrirComPDFPadrao(filePath);

                    return true;
                } else {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "PDF Não Criado\n\nO arquivo PDF nao foi encontrado no caminho especificado:\n" + filePath,
                            "Erro no PDF",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    return gerarRTFFallback();
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Erro ao Gerar PDF\n\nOcorreu um erro ao tentar gerar o arquivo PDF.\n\nTentando gerar arquivo RTF como alternativa...",
                        "Erro no PDF",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return gerarRTFFallback();
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Erro no PDF\n\nOcorreu um erro inesperado ao gerar o PDF:\n" + e.getMessage()
                            + "\n\nTentando gerar arquivo RTF como alternativa...",
                    "Erro no PDF",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return gerarRTFFallback();
        }
    }

    private boolean criarPDFSimples(String filePath) {
        try {
            java.io.FileWriter writer = new java.io.FileWriter(filePath);

            writer.write("%PDF-1.4\n");
            writer.write("1 0 obj\n");
            writer.write("<<\n");
            writer.write("/Type /Catalog\n");
            writer.write("/Pages 2 0 R\n");
            writer.write(">>\n");
            writer.write("endobj\n");
            writer.write("3 0 obj\n");
            writer.write("<<\n");
            writer.write("/Type /Pages\n");
            writer.write("/Kids [3 0 R]\n");
            writer.write("/Count 1\n");
            writer.write(">>\n");
            writer.write("endobj\n");
            writer.write("4 0 obj\n");
            writer.write("<<\n");
            writer.write("/Type /Page\n");
            writer.write("/Parent 2 0 R\n");
            writer.write("/MediaBox [0 0 612 792]\n");
            writer.write("/Contents 5 0 R\n");
            writer.write("/Length 250\n");
            writer.write(">>\n");
            writer.write("stream\n");
            writer.write("BT\n");
            writer.write("/F1 12 Tf\n");
            writer.write("72 720 Td\n");
            writer.write("(COMPROVANTE DE SENHA) Tj\n");
            writer.write("ET\n");
            writer.write("BT\n");
            writer.write("/F1 10 Tf\n");
            writer.write("72 700 Td\n");
            writer.write("(Senha: " + senha + ") Tj\n");
            writer.write("ET\n");
            writer.write("BT\n");
            writer.write("/F1 10 Tf\n");
            writer.write("72 680 Td\n");
            writer.write("(Servico: " + servico + ") Tj\n");
            writer.write("ET\n");
            writer.write("BT\n");
            writer.write("/F1 10 Tf\n");
            writer.write("72 660 Td\n");
            writer.write("(Local: Ambulatorio) Tj\n");
            writer.write("ET\n");
            writer.write("BT\n");
            writer.write("/F1 10 Tf\n");
            writer.write("72 640 Td\n");
            writer.write("(Data/Hora: ");
            writer.write(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ") Tj\n");
            writer.write("ET\n");
            writer.write("ET\n");
            writer.write("endstream\n");
            writer.write("endobj\n");
            writer.write("5 0 obj\n");
            writer.write("<<\n");
            writer.write("/Type /Font\n");
            writer.write("/Subtype /Type1\n");
            writer.write("/BaseFont /Helvetica\n");
            writer.write(">>\n");
            writer.write("endobj\n");
            writer.write("xref\n");
            writer.write("0 6\n");
            writer.write("0000000000 65535 f\n");
            writer.write("0000000010 00000 n\n");
            writer.write("0000000256 00000 n\n");
            writer.write("0000000263 00000 n\n");
            writer.write("0000000349 00000 n\n");
            writer.write("0000000564 00000 n\n");
            writer.write("0000000625 00000 n\n");
            writer.write("trailer\n");
            writer.write("<<\n");
            writer.write("/Size 6\n");
            writer.write("/Root 1 0 R\n");
            writer.write("startxref\n");
            writer.write("625\n");
            writer.write("%%EOF\n");

            writer.close();

            System.out.println("PDF simples criado: " + filePath);
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao criar PDF simples: " + e.getMessage());
            return false;
        }
    }

    private void abrirComPDFPadrao(String filePath) {
        try {
            File pdfFile = new File(filePath);

            if (pdfFile.exists()) {
                ProcessBuilder pb = new ProcessBuilder("pdf24", filePath);
                pb.start();

                Thread.sleep(3000);

                java.awt.Desktop.getDesktop().open(pdfFile);

                System.out.println("PDF aberto com o programa padrão do sistema!");
            } else {
                System.out.println("Arquivo PDF não encontrado: " + filePath);
            }

        } catch (Exception e) {
            System.err.println("Erro ao abrir PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean gerarRTFFallback() {
        try {
            String fileName = "comprovante_" + senha.replace(" ", "_") + ".rtf";
            String filePath = System.getProperty("user.dir") + "/" + fileName;

            String conteudoRTF = gerarConteudoRTF();
            java.io.FileWriter writer = new java.io.FileWriter(filePath);
            writer.write(conteudoRTF);
            writer.close();

            File rtfFile = new File(filePath);
            if (rtfFile.exists()) {
                java.awt.Desktop.getDesktop().open(rtfFile);
                System.out.println("RTF aberto com sucesso!");
                return true;
            } else {
                System.out.println("Erro ao criar RTF");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Erro ao gerar RTF: " + e.getMessage());
            return false;
        }
    }

    private String gerarConteudoRTF() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\\rtf1\\ansi\\deff0");
        sb.append("{\\fonttbl{\\f0\\fnil\\fcharset0 Arial;}}");
        sb.append("\\f0\\fs24");
        sb.append("\\qc\\b\\fs32 COMPROVANTE DE SENHA\\b0\\fs24\\par\\par");
        sb.append("\\brdrb\\brdrs\\brdrw10\\brsp20\\par\\par");
        sb.append("\\b Senha:\\b0  ").append(senha).append("\\par");
        sb.append("\\b Servico:\\b0  ").append(servico).append("\\par");
        sb.append("\\b Local:\\b0  ").append(local).append("\\par");
        sb.append("\\b Data/Hora:\\b0  ").append(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append("\\par\\par");
        sb.append("\\brdrb\\brdrs\\brdrw10\\brsp20\\par\\par");
        sb.append("\\qc\\i Por favor, Aguarde sua senha\\i0\\b ser chamada no painel eletronico.\\b0\\par\\par");
        sb.append("\\brdrb\\brdrs\\brdrw10\\brsp20\\par\\par");
        sb.append("\\qc\\fs20 Sistema de Gerenciamento de Senhas\\fs24\\par\\par");
        sb.append("\\brdrb\\brdrs\\brdrw10\\brsp20\\par\\par}");
        return sb.toString();
    }
}
