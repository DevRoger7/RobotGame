package robotgame.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Cartão de modo do menu: número, título, descrição e etiqueta. Selecionado = brilho amarelo. */
public class ModeCard extends HBox {

    private final Label numero;
    private final Label titulo;
    private final Label etiqueta;

    public ModeCard(int n, String titulo, String descricao, String etiqueta, Runnable aoEscolher) {
        getStyleClass().add("inner-card");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(18);
        setPadding(new Insets(0, 20, 0, 20));

        numero = Ui.label(String.valueOf(n), "mode-number");
        Ui.tamanhoFixo(numero, 44, 44);
        this.titulo = Ui.label(titulo, "mode-title");
        Label desc = Ui.label(descricao, "mode-desc");
        desc.setWrapText(true);
        VBox textos = new VBox(8, this.titulo, desc);
        textos.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textos, Priority.ALWAYS);
        textos.setMaxWidth(Double.MAX_VALUE);
        textos.setPrefWidth(0);
        this.etiqueta = Ui.label(etiqueta, "pill-tag");
        this.etiqueta.setMinWidth(USE_PREF_SIZE);

        getChildren().addAll(numero, textos, this.etiqueta);
        setOnMouseClicked(e -> aoEscolher.run());
    }

    public void setSelecionado(boolean selecionado) {
        for (var no : new javafx.scene.Node[]{this, numero, titulo, etiqueta}) {
            no.getStyleClass().removeAll("selected-glow", "ativo");
        }
        if (selecionado) {
            getStyleClass().add("selected-glow");
            numero.getStyleClass().add("ativo");
            titulo.getStyleClass().add("ativo");
            etiqueta.getStyleClass().add("ativo");
        }
    }
}
