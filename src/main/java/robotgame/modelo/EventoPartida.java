package robotgame.modelo;

/**
 * Algo que aconteceu durante a partida, publicado nos pontos em que o jogo já detecta movimento,
 * movimento inválido, batida em obstáculo e chegada ao alimento. Serve só para quem observa a partida
 * (a interface); não altera as regras.
 *
 * @param direcao direção tentada ou percorrida ("up", "down", "right", "left"); pode ser nula
 * @param x       posição do robô depois do evento (ou do obstáculo, em {@code FANTASMA_SUMIU})
 * @param obstaculo obstáculo envolvido, quando houver
 */
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
