package robotgame.ui.components;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import robotgame.modelo.ModoJogo;
import robotgame.ui.theme.Tema;

/** Barra de passos "1 MENU › 2 CENÁRIO › 3 JOGO › 4 FIM" (modos sem obstáculos omitem CENÁRIO). */
public class StepperBar extends HBox {

    public enum Passo {
        MENU("MENU"), CENARIO("CENÁRIO"), JOGO("JOGO"), FIM("FIM");

        private final String rotulo;

        Passo(String rotulo) {
            this.rotulo = rotulo;
        }
    }

    public StepperBar(ModoJogo modo, Passo atual) {
        setAlignment(Pos.CENTER);
        setSpacing(12);
        List<Passo> passos = new ArrayList<>(List.of(Passo.values()));
        if (!modo.isComObstaculos()) {
            passos.remove(Passo.CENARIO);
        }
        int indiceAtual = passos.indexOf(atual);
        for (int i = 0; i < passos.size(); i++) {
            if (i > 0) {
                getChildren().add(Icones.chevronDireita(Tema.BORDA_FORTE));
            }
            Label pilula = Ui.label((i + 1) + " " + passos.get(i).rotulo, "step-pill");
            if (i < indiceAtual) {
                pilula.getStyleClass().add("feito");
                pilula.setGraphic(Icones.check(Tema.VERDE));
            } else if (i == indiceAtual) {
                pilula.getStyleClass().add("atual");
            } else {
                pilula.getStyleClass().add("futuro");
            }
            getChildren().add(pilula);
        }
    }
}
