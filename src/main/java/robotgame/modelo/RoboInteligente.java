package robotgame.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import robotgame.excecao.MovimentoInvalidoException;

public class RoboInteligente extends Robo {

    private static final String[] DIRECOES = {"up", "down", "right", "left"};

    private final Random random = new Random();

    public RoboInteligente(String cor) {
        super(cor);
    }

    @Override
    public void mover(String direcao) throws MovimentoInvalidoException {
        List<String> naoTentadas = new ArrayList<>(List.of(DIRECOES));
        String atual = direcao;

        while (true) {
            try {
                super.mover(atual);
                return;
            } catch (MovimentoInvalidoException e) {
                naoTentadas.remove(atual);
                if (naoTentadas.isEmpty()) {
                    throw e;
                }
                atual = naoTentadas.get(random.nextInt(naoTentadas.size()));
            }
        }
    }
}
