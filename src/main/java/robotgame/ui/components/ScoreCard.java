package robotgame.ui.components;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import robotgame.ui.theme.Tema;

public class ScoreCard extends NeonCard {

    private final Chip chip;
    private final StatTile[] tiles;
    private final HBox topo;
    private final VBox nomes;

    public ScoreCard(Color cor, String titulo, String subtitulo, double altura, String... rotulos) {
        super(null);
        setSpacing(0);
        setPadding(new javafx.geometry.Insets(14, 14, 14, 14));
        setMinHeight(altura);
        setPrefHeight(altura);
        setMaxHeight(altura);

        chip = new Chip("ATIVO", Tema.VERDE);
        javafx.scene.control.Label nome = Ui.label(titulo, 10.5, "pixel");
        nome.setTextFill(cor);
        nome.setMinWidth(USE_PREF_SIZE);
        nomes = new VBox(5, nome, Ui.label(subtitulo, 12.5, "muted"));
        chip.setMinWidth(USE_PREF_SIZE);
        topo = Ui.linha(10, Sprites.pacman(cor, 30), nomes, Ui.espaco(), chip);
        topo.setAlignment(Pos.TOP_LEFT);

        tiles = new StatTile[rotulos.length];
        for (int i = 0; i < rotulos.length; i++) {
            tiles[i] = new StatTile(rotulos[i], "0", 13);
        }
        HBox linha = Ui.linhaIgual(8, tiles);
        getChildren().addAll(topo, Ui.espaco(), linha);
    }

    // Para cartões estreitos (3 lado a lado): o status sai do lado do nome e vai para baixo dele.
    public void statusAbaixoDoNome() {
        topo.getChildren().remove(chip);
        nomes.getChildren().add(new HBox(chip));
    }

    public void setStatus(String texto, Color cor) {
        chip.definir(texto, cor);
    }

    public void setValores(String... valores) {
        for (int i = 0; i < valores.length && i < tiles.length; i++) {
            tiles[i].setValor(valores[i]);
        }
    }
}
