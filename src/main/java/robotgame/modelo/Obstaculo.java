package robotgame.modelo;

public abstract class Obstaculo {

    private static int geradorId = 1;

    private final int id;

    protected Obstaculo() {
        this.id = geradorId++;
    }

    public int getId() {
        return id;
    }

    public abstract void bater(Robo robo, Tabuleiro tabuleiro);

    /** Se o obstáculo fica no tabuleiro depois de bater (o robô só volta), ele pode fechar caminhos. */
    public boolean bloqueiaPassagem() {
        return false;
    }

    public abstract String getNome();

    public abstract char getSimbolo();
}
