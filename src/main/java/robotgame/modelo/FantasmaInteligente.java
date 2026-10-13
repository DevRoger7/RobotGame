package robotgame.modelo;

public class FantasmaInteligente extends FantasmaMovel {

    public static final int X_INICIAL = 3;
    public static final int Y_INICIAL = 3;

    public FantasmaInteligente() {
        this(X_INICIAL, Y_INICIAL);
    }

    public FantasmaInteligente(int x, int y) {
        super(x, y);
    }

    // Retorna null quando o fantasma fica parado.
    public String decidirDirecao(Tabuleiro t, Robo[] robos) {
        Robo alvo = escolherAlvo(t, robos);
        if (alvo == null) {
            return null;
        }
        return BuscaCaminho.proximaDirecao(t, getX(), getY(), alvo.getX(), alvo.getY(), false);
    }

    // Ordem de inteligência: super, inteligente, normal. O primeiro ativo e alcançável é o alvo.
    private Robo escolherAlvo(Tabuleiro t, Robo[] robos) {
        for (int nivel = 0; nivel < 3; nivel++) {
            for (Robo robo : robos) {
                // Quem já chegou à fruta está a salvo: não é mais caçado.
                if (nivelDe(robo) == nivel && robo.isAtivo() && !t.alimentoEncontradoPor(robo)
                        && BuscaCaminho.distancia(t, getX(), getY(), robo.getX(), robo.getY(), false) != -1) {
                    return robo;
                }
            }
        }
        return null;
    }

    private static int nivelDe(Robo robo) {
        if (robo instanceof RoboSuperInteligente) {
            return 0;
        }
        if (robo instanceof RoboInteligente) {
            return 1;
        }
        return 2;
    }
}
