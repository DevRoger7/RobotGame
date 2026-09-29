package robotgame.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Bloco com rótulo pequeno e valor em pixel-font (POSIÇÃO, VÁLIDOS, X (0–3)...). */
public class StatTile extends VBox {

    private final Label valor;

    public StatTile(String rotulo, String valorInicial, double tamanhoValor) {
        getStyleClass().add("tile");
        setAlignment(Pos.CENTER);
        setSpacing(5);
        setPadding(new Insets(7, 3, 7, 3));
        valor = Ui.label(valorInicial, "stat-value");
        valor.setStyle("-fx-font-size: " + tamanhoValor + "px;");
        getChildren().addAll(Ui.label(rotulo, "stat-label"), valor);
    }

    public void setValor(String texto) {
        valor.setText(texto);
    }

    public Label getValor() {
        return valor;
    }
}
