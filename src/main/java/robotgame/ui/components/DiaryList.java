package robotgame.ui.components;

import java.util.List;
import java.util.function.Predicate;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import robotgame.ui.partida.DiarioPartida.Entrada;

/** Últimas entradas do diário, a mais nova em cima e destacada. Lê só o modelo do diário. */
public class DiaryList extends VBox {

    private final ObservableList<Entrada> entradas;
    private final int maximo;
    private final Predicate<Entrada> filtro;
    private final ListChangeListener<Entrada> ouvinte = mudanca -> redesenhar();

    public DiaryList(ObservableList<Entrada> entradas, int maximo, Predicate<Entrada> filtro) {
        this.entradas = entradas;
        this.maximo = maximo;
        this.filtro = filtro;
        setSpacing(1);
        setMinHeight(0);
        VBox.setVgrow(this, javafx.scene.layout.Priority.ALWAYS);
        entradas.addListener(ouvinte);
        redesenhar();
    }

    // Linha que não cabe inteira some (em vez de aparecer cortada pela borda do cartão).
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double limite = getHeight() - snappedBottomInset();
        for (javafx.scene.Node linha : getChildren()) {
            linha.setVisible(linha.getLayoutY() + linha.getLayoutBounds().getHeight() <= limite + 0.5);
        }
    }

    /** Solta o ouvinte do modelo (ao sair da tela). */
    public void desligar() {
        entradas.removeListener(ouvinte);
    }

    private void redesenhar() {
        List<Entrada> visiveis = entradas.stream().filter(filtro).toList();
        getChildren().clear();
        for (int i = visiveis.size() - 1, n = 0; i >= 0 && n < maximo; i--, n++) {
            Entrada e = visiveis.get(i);
            Label numero = Ui.label("#" + e.numero(), "diary-num");
            numero.setMinWidth(34);
            Label texto = Ui.label(e.texto(), "diary-text");
            texto.setMaxWidth(Double.MAX_VALUE);
            HBox linha = Ui.linha(8, numero, new Circle(4, e.cor()), texto);
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.getStyleClass().add("diary-row");
            if (n == 0) {
                linha.getStyleClass().add("novo");
            }
            getChildren().add(linha);
        }
    }
}
