package robotgame.modelo;

import java.util.List;
import java.util.Random;

public class GeradorPoder {

    public static final int ESPERA_INICIAL = 5;
    public static final int ESPERA_APOS_COMIDO = 5;

    private final Random random;

    public GeradorPoder(Random random) {
        this.random = random;
    }

    public List<EventoPartida> fimDaRodada(Tabuleiro t, Robo[] robos) {
        throw new UnsupportedOperationException("TODO");
    }

    public void itemFoiComido() {
        throw new UnsupportedOperationException("TODO");
    }
}
