package robotgame.ui.partida;

import java.util.Set;

import robotgame.modelo.Robo;
import robotgame.modelo.Tabuleiro;

/**
 * Situação final de uma partida, entregue à tela de fim.
 *
 * @param explosoes casas (codificadas como {@code x * TAMANHO + y}) onde houve explosão
 * @param mensagem  resultado textual devolvido pelo jogo (ex.: limite de rodadas); pode ser nulo
 */
public record ResultadoPartida(ConfiguracaoPartida configuracao, Tabuleiro tabuleiro, Robo[] robos,
                               DiarioPartida diario, Set<Integer> explosoes, String mensagem) {

    public boolean achou(Robo robo) {
        return robo.isAtivo() && tabuleiro.alimentoEncontradoPor(robo);
    }
}
