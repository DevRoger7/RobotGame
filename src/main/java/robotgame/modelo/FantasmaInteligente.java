package robotgame.modelo;

public class FantasmaInteligente {

    public static final int X_INICIAL = 3;
    public static final int Y_INICIAL = 3;

    private int x;
    private int y;

    public FantasmaInteligente() {
        this(X_INICIAL, Y_INICIAL);
    }

    public FantasmaInteligente(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Retorna null quando o fantasma fica parado.
    public String decidirDirecao(Tabuleiro t, Robo[] robos) {
        throw new UnsupportedOperationException("TODO");
    }

    // null não move.
    public void mover(String direcao) {
        throw new UnsupportedOperationException("TODO");
    }
}
