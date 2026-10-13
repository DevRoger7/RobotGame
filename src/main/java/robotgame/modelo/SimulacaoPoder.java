package robotgame.modelo;

import java.util.function.Consumer;

public class SimulacaoPoder {

    public static final int MAX_RODADAS = 1000;

    private final Tabuleiro tabuleiro;
    private final Robo[] robos;
    private Consumer<EventoPartida> ouvinte = evento -> {};

    public SimulacaoPoder(Tabuleiro tabuleiro, Robo... robos) {
        this.tabuleiro = tabuleiro;
        this.robos = robos;
    }

    public void setOuvinte(Consumer<EventoPartida> ouvinte) {
        this.ouvinte = ouvinte;
    }

    public boolean isTerminada() {
        throw new UnsupportedOperationException("TODO");
    }

    public String getResultado() {
        throw new UnsupportedOperationException("TODO");
    }

    // No turno do fantasma, Passo.robo() é null.
    public Simulacao.Passo proximoPasso() {
        throw new UnsupportedOperationException("TODO");
    }
}
