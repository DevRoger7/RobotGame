package robotgame.modelo;

public record EventoPartida(Tipo tipo, Robo robo, String direcao, int x, int y, Obstaculo obstaculo) {

    public enum Tipo {
        MOVEU,
        INVALIDO,
        ROCHA,
        EXPLODIU,
        FANTASMA_SUMIU,
        ACHOU_ALIMENTO
    }
}
