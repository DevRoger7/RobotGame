package robotgame.modelo;

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
        obstaculos[x][y] = o;
    }

    public Obstaculo getObstaculo(int x, int y) {
        return obstaculos[x][y];
    }

    void removerObstaculo(int x, int y) {
        obstaculos[x][y] = null;
    }
}
