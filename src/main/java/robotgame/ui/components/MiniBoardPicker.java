package robotgame.ui.components;

import java.util.function.BiConsumer;

import robotgame.modelo.Robo;

/**
 * Mini-tabuleiro do menu (células de 68 px): mostra o(s) pacman(s) em (0,0) e a fruta; clicar numa casa
 * pede para mudar a fruta de lugar.
 */
public class MiniBoardPicker extends BoardView {

    public MiniBoardPicker(BiConsumer<Integer, Integer> aoEscolherCasa) {
        super(68, true);
        setAoClicar(aoEscolherCasa);
    }

    /** Pacmans só para exibição (não participam de partida nenhuma). */
    public void mostrarPacmans(String... cores) {
        Robo[] robos = new Robo[cores.length];
        for (int i = 0; i < cores.length; i++) {
            robos[i] = new Robo(cores[i]);
        }
        setRobos(robos);
    }
}
