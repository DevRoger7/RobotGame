package robotgame.ui.components;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import robotgame.ui.theme.Tema;

/** Placar de um pacman: sprite, nome na cor do pacman, subtítulo, chip de status e três {@link StatTile}. */
public class ScoreCard extends NeonCard {

    private final Chip chip;
    private final StatTile[] tiles;

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
        VBox nomes = new VBox(5, nome, Ui.label(subtitulo, 12.5, "muted"));
        chip.setMinWidth(USE_PREF_SIZE);
        HBox topo = Ui.linha(10, Sprites.pacman(cor, 30), nomes, Ui.espaco(), chip);
        topo.setAlignment(Pos.TOP_LEFT);

        tiles = new StatTile[rotulos.length];
        for (int i = 0; i < rotulos.length; i++) {
            tiles[i] = new StatTile(rotulos[i], "0", 13);
        }
        HBox linha = Ui.linhaIgual(8, tiles);
        getChildren().addAll(topo, Ui.espaco(), linha);
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
