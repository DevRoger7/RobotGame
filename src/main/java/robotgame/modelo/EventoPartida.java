package robotgame.modelo;

public record EventoPartida(Tipo tipo, Robo robo, String direcao, int x, int y, Obstaculo obstaculo) {

    public enum Tipo {
        MOVEU,
        INVALIDO,
        ROCHA,
        EXPLODIU,
        FANTASMA_SUMIU,
        ACHOU_ALIMENTO,
        // Nos eventos de bolinha e de fantasma, robo pode ser null.
        PODER_APARECEU,
        PODER_SUMIU,
        PEGOU_PODER,
        PODER_ACABOU,
        FANTASMA_MOVEU,
        COMEU_FANTASMA,
        COMEU_FANTASMA_BURRO,
        ELIMINADO_PELO_FANTASMA,
        // Modo PvP: robo é a vítima, eliminada por encostar no outro jogador que estava com poder.
        PACMAN_ELIMINADO
    }
}
