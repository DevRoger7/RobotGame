package robotgame.modelo;

// Base dos fantasmas que andam pelo tabuleiro (inteligente e aleatório).
public abstract class FantasmaMovel {

    private int x;
    private int y;

    protected FantasmaMovel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // null não move.
    public void mover(String direcao) {
        if (direcao == null) {
            return;
        }
        switch (direcao) {
            case "up" -> y++;
            case "down" -> y--;
            case "right" -> x++;
            case "left" -> x--;
        }
    }
}
