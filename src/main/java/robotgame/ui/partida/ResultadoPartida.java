package robotgame.ui.partida;

import java.util.Set;

import robotgame.modelo.Robo;
import robotgame.modelo.Tabuleiro;

public record ResultadoPartida(ConfiguracaoPartida configuracao, Tabuleiro tabuleiro, Robo[] robos,
                               DiarioPartida diario, Set<Integer> explosoes, String mensagem) {

    public boolean achou(Robo robo) {
        return robo.isAtivo() && tabuleiro.alimentoEncontradoPor(robo);
    }
}
