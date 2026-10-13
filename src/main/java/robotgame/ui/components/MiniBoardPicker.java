package robotgame.ui.components;

import java.util.function.BiConsumer;

import robotgame.modelo.Robo;

public class MiniBoardPicker extends BoardView {

    public MiniBoardPicker(BiConsumer<Integer, Integer> aoEscolherCasa) {
        super(68, true);
        setAoClicar(aoEscolherCasa);
    }

    public void mostrarPacmans(String... cores) {
        Robo[] robos = new Robo[cores.length];
        for (int i = 0; i < cores.length; i++) {
            robos[i] = new Robo(cores[i]);
        }
        setRobos(robos);
    }
}
