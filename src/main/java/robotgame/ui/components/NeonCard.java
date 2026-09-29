package robotgame.ui.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Cartão com borda azul-neon e título opcional em pixel-font amarela. */
public class NeonCard extends VBox {

    private final Label titulo;

    public NeonCard(String titulo) {
        getStyleClass().add("neon-card");
        setPadding(new Insets(14, 18, 14, 18));
        setSpacing(10);
        setFillWidth(true);
        if (titulo != null) {
            this.titulo = Ui.label(titulo, "card-title");
            getChildren().add(this.titulo);
        } else {
            this.titulo = null;
        }
    }

    public NeonCard(String titulo, Node... conteudo) {
        this(titulo);
        getChildren().addAll(conteudo);
    }

    public Label getTitulo() {
        return titulo;
    }
}
