package robotgame.modelo;

import java.util.Random;

public class RoboSuperInteligente extends RoboInteligente {

    public RoboSuperInteligente(String cor) {
        super(cor);
    }

    @Override
    public String decidirDirecao(Tabuleiro t, Random random) {
        throw new UnsupportedOperationException("TODO");
    }
}
