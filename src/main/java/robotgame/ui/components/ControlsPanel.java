package robotgame.ui.components;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import robotgame.ui.theme.Tema;

/** CONTROLES: Pausar [P], Som [M], Novo jogo e (opcional) slider de velocidade 1–5. */
public class ControlsPanel extends NeonCard {

    private static final String[] NOMES_VELOCIDADE = {"Muito lento", "Lento", "Normal", "Rápido", "Muito rápido"};

    private final Button pausa;
    private final Button som;
    private final Button novoJogo;
    private final Slider velocidade;

    public ControlsPanel(boolean comVelocidade, Runnable aoPausar, Runnable aoSom, Runnable aoNovoJogo) {
        super("CONTROLES");
        pausa = Ui.primario("PAUSAR", "btn-laranja", 50);
        pausa.setOnAction(e -> aoPausar.run());
        som = Ui.secundario("Som: ligado", 50);
        som.setOnAction(e -> aoSom.run());
        novoJogo = Ui.secundario("Novo jogo", 50);
        novoJogo.setGraphic(Icones.recomecar(Tema.TEXTO));
        novoJogo.setOnAction(e -> aoNovoJogo.run());
        for (Button b : new Button[]{pausa, som, novoJogo}) {
            b.setStyle("-fx-padding: 0 6 0 6;");
        }
        // "Som: desligado" precisa de mais espaço que "Novo jogo".
        GridPane botoes = new GridPane();
        botoes.setHgap(10);
        for (double largura : new double[]{33, 37, 30}) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(largura);
            botoes.getColumnConstraints().add(c);
        }
        botoes.addRow(0, pausa, som, novoJogo);
        getChildren().add(botoes);
        setPausado(false);
        setMudo(false);

        if (comVelocidade) {
            Label nome = Ui.label("Normal", 13, "bold");
            HBox topo = Ui.linha(0, Ui.label("VELOCIDADE", "section-label"), Ui.espaco(), nome);
            velocidade = new Slider(1, 5, 3);
            velocidade.setMajorTickUnit(1);
            velocidade.setMinorTickCount(0);
            velocidade.setSnapToTicks(true);
            velocidade.setBlockIncrement(1);
            velocidade.setFocusTraversable(false);
            velocidade.valueProperty().addListener((obs, a, b) -> {
                nome.setText(NOMES_VELOCIDADE[(int) Math.round(b.doubleValue()) - 1]);
                pintarTrilho();
            });
            velocidade.skinProperty().addListener((obs, a, b) -> pintarTrilho());
            VBox blocoVelocidade = new VBox(2, topo, velocidade);
            getChildren().add(blocoVelocidade);
            setSpacing(10);
        } else {
            velocidade = null;
        }
    }

    public void setPausado(boolean pausado) {
        Label texto = Ui.label(pausado ? "CONTINUAR" : "PAUSAR", 10, "pixel");
        texto.setTextFill(Tema.FUNDO);
        HBox conteudo = new HBox(9, pausado ? Icones.play(Tema.FUNDO, 11) : Icones.pausa(Tema.FUNDO),
                texto, new KeyCap("P", false));
        conteudo.setAlignment(Pos.CENTER);
        pausa.setText(null);
        pausa.setGraphic(conteudo);
    }

    public void setPodePausar(boolean pode) {
        pausa.setDisable(!pode);
    }

    public void setMudo(boolean mudo) {
        HBox conteudo = new HBox(6, Icones.som(Tema.TEXTO, !mudo),
                Ui.label(mudo ? "Som: desligado" : "Som: ligado", 13.5, "bold"), new KeyCap("M", true));
        conteudo.setAlignment(Pos.CENTER);
        som.setText(null);
        som.setGraphic(conteudo);
    }

    public Slider getVelocidade() {
        return velocidade;
    }

    public void aoMudarVelocidade(ChangeListener<Number> ouvinte) {
        if (velocidade != null) {
            velocidade.valueProperty().addListener(ouvinte);
        }
    }

    // Parte do trilho à esquerda do "thumb" em amarelo.
    private void pintarTrilho() {
        if (velocidade == null) {
            return;
        }
        var trilho = velocidade.lookup(".track");
        if (trilho != null) {
            double p = (velocidade.getValue() - 1) / 4 * 100;
            trilho.setStyle(String.format(java.util.Locale.ROOT,
                    "-fx-background-color: linear-gradient(to right, #ffe14d %.1f%%, #10103a %.1f%%);", p, p));
        }
    }
}
