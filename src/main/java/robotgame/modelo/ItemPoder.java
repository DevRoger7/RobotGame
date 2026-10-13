package robotgame.modelo;

public class ItemPoder {

    public static final int VIDA = 8;

    private final int x;
    private final int y;
    private int rodadasRestantes = VIDA;

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
        return rodadasRestantes;
    }

    public void passarRodada() {
        if (rodadasRestantes > 0) {
            rodadasRestantes--;
        }
    }

    public boolean expirou() {
        return rodadasRestantes == 0;
    }
}
