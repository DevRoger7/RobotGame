package robotgame.ui.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import robotgame.ui.theme.Tema;

/** Grade de legenda: ícone + nome + descrição curta. */
public class LegendGrid extends GridPane {

    private final int colunas;
    private int total;

    /** Colunas para uma legenda de {@code itens} itens: uma linha até 4, senão 3 por linha. */
    public static int colunasPara(int itens) {
        return itens <= 4 ? itens : 3;
    }

    public LegendGrid(int colunas) {
        this.colunas = colunas;
        setHgap(12);
        setVgap(6);
        for (int i = 0; i < colunas; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(100.0 / colunas);
            getColumnConstraints().add(c);
        }
    }

    public LegendGrid item(Node icone, String nome, String descricao) {
        StackPane caixa = new StackPane(icone);
        caixa.setStyle("-fx-background-color: #10103a; -fx-background-radius: 8;");
        Ui.tamanhoFixo(caixa, 32, 32);
        VBox textos = new VBox(0, Ui.label(nome, "legend-title"), Ui.label(descricao, "legend-desc"));
        HBox linha = new HBox(10, caixa, textos);
        linha.setAlignment(Pos.CENTER_LEFT);
        add(linha, total % colunas, total / colunas);
        total++;
        return this;
    }

    /** Ícone da faixa "Proibido" (listras vermelhas). */
    public static Node iconeProibido() {
        StackPane p = new StackPane();
        for (int i = 0; i < 5; i++) {
            Rectangle r = new Rectangle(4, 30, Tema.VERMELHO);
            r.setRotate(45);
            r.setTranslateX(-12 + i * 6);
            p.getChildren().add(r);
        }
        Rectangle clip = new Rectangle(20, 20);
        p.setClip(clip);
        p.setMaxSize(20, 20);
        p.setMinSize(20, 20);
        p.setStyle("-fx-background-color: " + Tema.hex(Color.web("#3a0f22")) + ";");
        clip.setArcWidth(3);
        clip.setArcHeight(3);
        return p;
    }
}
