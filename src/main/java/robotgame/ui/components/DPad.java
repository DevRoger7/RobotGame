package robotgame.ui.components;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import robotgame.ui.theme.Tema;

/** Botões de direção (▲ em cima; ◀ ▼ ▶ embaixo). Chamam o mesmo movimento das setas do teclado. */
public class DPad extends GridPane {

    private final Map<String, Button> botoes = new LinkedHashMap<>();

    public DPad(Consumer<String> aoMover) {
        setHgap(10);
        setVgap(10);
        adicionar("up", 1, 0, aoMover);
        adicionar("left", 0, 1, aoMover);
        adicionar("down", 1, 1, aoMover);
        adicionar("right", 2, 1, aoMover);
    }

    private void adicionar(String direcao, int coluna, int linha, Consumer<String> aoMover) {
        Button b = Ui.secundario(null, 56);
        b.setGraphic(Icones.seta(direcao, Tema.TEXTO, 14));
        Ui.tamanhoFixo(b, 64, 56);
        b.setPadding(javafx.geometry.Insets.EMPTY);
        b.setOnAction(e -> aoMover.accept(direcao));
        botoes.put(direcao, b);
        add(b, coluna, linha);
    }

    /** Destaca em amarelo o botão da última direção. */
    public void destacar(String direcao) {
        botoes.forEach((d, b) -> {
            b.getStyleClass().remove("destaque");
            ((javafx.scene.shape.Shape) b.getGraphic()).setFill(d.equals(direcao) ? Tema.AMARELO : Tema.TEXTO);
            if (d.equals(direcao)) {
                b.getStyleClass().add("destaque");
            }
        });
    }
}
