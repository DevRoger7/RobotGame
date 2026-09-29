package robotgame.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import robotgame.ui.theme.Tema;

/** Etiqueta arredondada de status: "● ATIVO", "● EXPLODIU"... */
public class Chip extends HBox {

    private final Circle ponto = new Circle(3.5);
    private final Label texto = new Label();

    public Chip(String texto, Color cor) {
        getStyleClass().add("chip");
        setAlignment(Pos.CENTER);
        setSpacing(5);
        this.texto.getStyleClass().add("chip-text");
        getChildren().addAll(ponto, this.texto);
        setMaxWidth(USE_PREF_SIZE);
        setMaxHeight(USE_PREF_SIZE);
        definir(texto, cor);
    }

    public void definir(String texto, Color cor) {
        this.texto.setText(texto);
        this.texto.setTextFill(cor);
        ponto.setFill(cor);
        setStyle("-fx-border-color: " + Tema.hex(cor) + "; -fx-background-color: " + Tema.rgba(cor, 0.10) + ";");
    }
}
