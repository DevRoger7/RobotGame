package robotgame.modelo;

public class ItemPoder {

    public static final int VIDA = 8;

    private final int x;
    private final int y;

    public ItemPoder(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRodadasRestantes() {
        throw new UnsupportedOperationException("TODO");
    }

    public void passarRodada() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean expirou() {
        throw new UnsupportedOperationException("TODO");
    }
}
