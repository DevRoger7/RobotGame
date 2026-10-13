package robotgame.ui.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import robotgame.modelo.ModoJogo;
import robotgame.ui.components.AppHeader;
import robotgame.ui.components.BoardView;
import robotgame.ui.components.LegendGrid;
import robotgame.ui.components.MessageBar;
import robotgame.ui.components.StepperBar;
import robotgame.ui.components.Ui;

final class LayoutJogo {

    static final double LARGURA_PAINEL = 548;
    static final double ALTURA_AREA = 660;

    private LayoutJogo() {
    }

    static VBox montar(ModoJogo modo, StepperBar.Passo passo, BoardView tabuleiro, MessageBar mensagem,
                       Node painelDireito) {
        VBox esquerda = new VBox(14, tabuleiro, mensagem);
        esquerda.setAlignment(Pos.TOP_LEFT);
        Ui.tamanhoFixo(esquerda, 660, ALTURA_AREA);

        VBox direita = painelDireito instanceof VBox v ? v : new VBox(painelDireito);
        Ui.tamanhoFixo(direita, LARGURA_PAINEL, ALTURA_AREA);

        HBox principal = new HBox(24, esquerda, direita);
        VBox raiz = new VBox(16, new AppHeader(modo, passo), principal);
        raiz.setPadding(new Insets(20, 24, 20, 24));
        return raiz;
    }

    static VBox painel(double espaco, Node... blocos) {
        VBox v = new VBox(espaco, blocos);
        v.setFillWidth(true);
        return v;
    }

    static void crescer(Region r) {
        VBox.setVgrow(r, Priority.ALWAYS);
        r.setMaxHeight(Double.MAX_VALUE);
    }

    static void recortar(Region r) {
        r.setMinHeight(0);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(r.widthProperty());
        clip.heightProperty().bind(r.heightProperty());
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        r.setClip(clip);
    }

    static Node iconeProibido() {
        return LegendGrid.iconeProibido();
    }
}
