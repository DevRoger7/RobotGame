package robotgame.ui.screens;

import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;

public interface Tela {

    Parent getRaiz();

    default void aoMostrar() {
    }

    default void aoTecla(KeyEvent evento) {
    }

    default void aoSair() {
    }
}
