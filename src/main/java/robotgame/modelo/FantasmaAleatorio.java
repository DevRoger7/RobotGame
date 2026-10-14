package robotgame.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FantasmaAleatorio extends FantasmaMovel {

    public static final int X_INICIAL = 0;
    public static final int Y_INICIAL = 3;

    private static final String[] DIRECOES = {"up", "down", "right", "left"};
    private static final int[][] PASSOS = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

    public FantasmaAleatorio() {
        this(X_INICIAL, Y_INICIAL);
    }

    public FantasmaAleatorio(int x, int y) {
        super(x, y);
    }

    // Sorteia uma casa vizinha dentro do tabuleiro e sem rocha. Retorna null quando o fantasma fica parado.
    public String decidirDirecao(Tabuleiro t, Random random) {
        List<String> possiveis = new ArrayList<>();
        for (int i = 0; i < DIRECOES.length; i++) {
            int nx = getX() + PASSOS[i][0];
            int ny = getY() + PASSOS[i][1];
            if (Tabuleiro.dentroDosLimites(nx, ny) && t.getObstaculo(nx, ny) == null) {
                possiveis.add(DIRECOES[i]);
            }
        }
        if (possiveis.isEmpty()) {
            return null;
        }
        return possiveis.get(random.nextInt(possiveis.size()));
    }
}
