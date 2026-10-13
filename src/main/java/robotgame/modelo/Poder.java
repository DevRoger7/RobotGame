package robotgame.modelo;

public class Poder {

    public static final int DURACAO = 8;

    private int turnosRestantes = DURACAO;

    public Poder() {
    }

    public int getTurnosRestantes() {
        return turnosRestantes;
    }

    public void gastarTurno() {
        if (turnosRestantes > 0) {
            turnosRestantes--;
        }
    }

    public boolean acabou() {
        return turnosRestantes == 0;
    }
}
