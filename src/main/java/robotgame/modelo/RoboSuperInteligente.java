package robotgame.modelo;

import java.util.Random;

public class RoboSuperInteligente extends RoboInteligente {

    public RoboSuperInteligente(String cor) {
        super(cor);
    }

    // O super não sabe onde está a bolinha de poder: só caça o fantasma quando já tem poder.
    @Override
    public String decidirDirecao(Tabuleiro t, Random random) {
        if (isComPoder() && t.getFantasmaInteligente() != null) {
            return super.decidirDirecao(t, random);
        }
        String direcao = BuscaCaminho.proximaDirecao(t, getX(), getY(), t.getXAlimento(), t.getYAlimento(), false);
        return direcao != null ? direcao : direcaoAleatoria(random);
    }
}
