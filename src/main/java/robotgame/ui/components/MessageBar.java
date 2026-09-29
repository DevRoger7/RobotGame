package robotgame.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import robotgame.ui.theme.Tema;

/** Faixa abaixo do tabuleiro: etiqueta (ÚLTIMO, DICA, STATUS, FIM...) + frase. */
public class MessageBar extends HBox {

    private final Label etiqueta = Ui.label("", "tag");
    private final Label texto = Ui.label("", 15, "semi");

    public MessageBar(double largura) {
        getStyleClass().add("neon-card");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(14);
        setPadding(new Insets(0, 16, 0, 16));
        Ui.tamanhoFixo(this, largura, 46);
        etiqueta.setMinWidth(USE_PREF_SIZE);
        texto.setMaxWidth(Double.MAX_VALUE);
        getChildren().addAll(etiqueta, texto);
    }

    public void mostrar(String tag, String mensagem) {
        mostrar(tag, mensagem, Tema.AMARELO);
    }

    public void mostrar(String tag, String mensagem, Color corEtiqueta) {
        etiqueta.setText(Ui.pixel(tag));
        etiqueta.setStyle("-fx-background-color: " + Tema.hex(corEtiqueta) + ";");
        texto.setText(mensagem);
    }
}
