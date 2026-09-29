package robotgame.modelo;

import java.util.ArrayDeque;
import java.util.Deque;

public class Tabuleiro {

    public static final int TAMANHO = 4;

    private final int xAlimento;
    private final int yAlimento;
    private final Obstaculo[][] obstaculos = new Obstaculo[TAMANHO][TAMANHO];

    public Tabuleiro(int xAlimento, int yAlimento) {
        if (!dentroDosLimites(xAlimento, yAlimento)) {
            throw new IllegalArgumentException("Posição do alimento fora do tabuleiro (use 0 a " + (TAMANHO - 1) + ").");
        }
        if (xAlimento == 0 && yAlimento == 0) {
            throw new IllegalArgumentException("O alimento não pode ficar em (0,0), posição inicial dos robôs.");
        }
        this.xAlimento = xAlimento;
        this.yAlimento = yAlimento;
    }

    public static boolean dentroDosLimites(int x, int y) {
        return x >= 0 && x < TAMANHO && y >= 0 && y < TAMANHO;
    }

    public int getXAlimento() {
        return xAlimento;
    }

    public int getYAlimento() {
        return yAlimento;
    }

    public boolean alimentoEncontradoPor(Robo robo) {
        return robo.encontrouAlimento(xAlimento, yAlimento);
    }

    public void adicionarObstaculo(Obstaculo o, int x, int y) {
        if (!dentroDosLimites(x, y)) {
            throw new IllegalArgumentException("Posição (" + x + "," + y + ") fora do tabuleiro (use 0 a " + (TAMANHO - 1) + ").");
        }
        if (x == 0 && y == 0) {
            throw new IllegalArgumentException("Não é possível colocar obstáculo em (0,0), posição inicial dos robôs.");
        }
        if (x == xAlimento && y == yAlimento) {
            throw new IllegalArgumentException("Não é possível colocar obstáculo na posição do alimento.");
        }
        if (obstaculos[x][y] != null) {
            throw new IllegalArgumentException("A posição (" + x + "," + y + ") já está ocupada por outro obstáculo.");
        }
        if (o.bloqueiaPassagem() && fechariaCaminhoAoAlimento(x, y)) {
            throw new IllegalArgumentException("Uma rocha em (" + x + "," + y + ") fecharia o caminho até o alimento: "
                    + "os robôs ficariam presos e a partida nunca terminaria.");
        }
        obstaculos[x][y] = o;
    }

    /**
     * Indica se uma rocha (ou qualquer obstáculo que bloqueie a passagem) em (x, y) impediria os robôs de
     * ir de (0,0) até o alimento. Só obstáculos que bloqueiam contam: o fantasma elimina o robô e some,
     * então nunca prende ninguém para sempre.
     */
    public boolean fechariaCaminhoAoAlimento(int x, int y) {
        boolean[][] visitada = new boolean[TAMANHO][TAMANHO];
        Deque<int[]> fila = new ArrayDeque<>();
        visitada[0][0] = true;
        fila.add(new int[]{0, 0});
        int[][] passos = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!fila.isEmpty()) {
            int[] atual = fila.poll();
            if (atual[0] == xAlimento && atual[1] == yAlimento) {
                return false;
            }
            for (int[] passo : passos) {
                int nx = atual[0] + passo[0];
                int ny = atual[1] + passo[1];
                if (!dentroDosLimites(nx, ny) || visitada[nx][ny] || (nx == x && ny == y)) {
                    continue;
                }
                if (obstaculos[nx][ny] != null && obstaculos[nx][ny].bloqueiaPassagem()) {
                    continue;
                }
                visitada[nx][ny] = true;
                fila.add(new int[]{nx, ny});
            }
        }
        return true;
    }

    public Obstaculo getObstaculo(int x, int y) {
        return obstaculos[x][y];
    }

    public void removerObstaculo(int x, int y) {
        obstaculos[x][y] = null;
    }
}
