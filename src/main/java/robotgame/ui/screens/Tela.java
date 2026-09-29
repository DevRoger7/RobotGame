package robotgame.ui.screens;

import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;

/** Uma tela do jogo, exibida pelo {@link Navegador} na área de design de 1280×800. */
public interface Tela {

    Parent getRaiz();

    /** Chamado quando a tela passa a ser exibida. */
    default void aoMostrar() {
    }

    /** Teclas recebidas pelo filtro da Scene. */
    default void aoTecla(KeyEvent evento) {
    }

    /** Chamado antes de a tela ser trocada: parar timers, animações e sons que forem dela. */
    default void aoSair() {
    }
}
