package com.sistemasenhas.factory;

import com.sistemasenhas.models.TipoAtendimento;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;

public class BotaoAtendimentoFactory {

    /**
     * Gera um botão customizado com base nas configurações de um TipoAtendimento.
     */
    public static Button gerarBotao(TipoAtendimento tipo) {
        Button btn = new Button(tipo.getDescricao());

        // Verificação de segurança para valores nulos (Padrões caso não configurado no
        // local)
        String corFundo = tipo.getCorFundo() != null ? tipo.getCorFundo() : "#0066CC";
        String corTexto = tipo.getCorTexto() != null ? tipo.getCorTexto() : "#FFFFFF";
        String fonte = tipo.getEstiloFonte() != null ? tipo.getEstiloFonte() : "Segoe UI";
        int tamFonte = tipo.getTamanhoFonte() > 0 ? tipo.getTamanhoFonte() : 10;

        // 1. Estilização Base (Cores, Fonte e Tamanho)
        StringBuilder style = new StringBuilder();
        style.append(String.format("-fx-background-color: %s; ", corFundo));
        style.append(String.format("-fx-text-fill: %s; ", corTexto));
        style.append(String.format("-fx-font-family: '%s'; ", fonte));
        style.append(String.format("-fx-font-size: %dpx; ", tamFonte));
        style.append("-fx-font-weight: bold; ");

        // 2. Aplicação do Formato e Dimensões Geométricas
        String formato = tipo.getFormatoBotao() != null ? tipo.getFormatoBotao().toUpperCase() : "RETANGULAR";

        // Define o raio da borda baseado no formato
        style.append(definirRaioBorda(formato));
        btn.setStyle(style.toString());

        // Define proporções reais: Retangulares são mais largos que altos. Quadrados
        // são 1:1.
        if (formato.startsWith("RETANGULAR")) {
            btn.setMinWidth(180);
            btn.setPrefWidth(180);
            btn.setMinHeight(70);
            btn.setPrefHeight(70);
        } else if (formato.startsWith("QUADRADO") || formato.equals("CIRCULAR")) {
            btn.setMinWidth(120);
            btn.setPrefWidth(120);
            btn.setMinHeight(120);
            btn.setPrefHeight(120);
        }

        btn.setAlignment(Pos.CENTER);
        btn.setContentDisplay(ContentDisplay.TOP);

        // 3. Adição do Icone
        if (tipo.getCaminhoIcone() != null && !tipo.getCaminhoIcone().isEmpty()) {
            try {
                String caminho = "/icones_atendimento/" + tipo.getCaminhoIcone();
                InputStream is = BotaoAtendimentoFactory.class.getResourceAsStream(caminho);

                if (is != null) {
                    Image img = new Image(is);
                    ImageView view = new ImageView(img);
                    view.setFitHeight(30);
                    view.setFitWidth(30);
                    view.setPreserveRatio(true);

                    btn.setGraphic(view);
                    btn.setGraphicTextGap(5);
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar ícone: " + tipo.getCaminhoIcone());
            }
        }

        return btn;
    }

    private static String definirRaioBorda(String formato) {
        switch (formato) {
            case "CIRCULAR":
                return "-fx-background-radius: 100; ";
            case "QUADRADO":
                return "-fx-background-radius: 0; ";
            case "QUADRADO-ARREDONDADO":
                return "-fx-background-radius: 20; ";
            case "RETANGULAR-ARREDONDADO":
                return "-fx-background-radius: 15; ";
            case "RETANGULAR":
            default:
                return "-fx-background-radius: 0; ";
        }
    }
}
