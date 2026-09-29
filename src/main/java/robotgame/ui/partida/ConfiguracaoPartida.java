package robotgame.ui.partida;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import robotgame.modelo.Fantasma;
import robotgame.modelo.ModoJogo;
import robotgame.modelo.Robo;
import robotgame.modelo.RoboInteligente;
import robotgame.modelo.Rocha;
import robotgame.modelo.Tabuleiro;
import robotgame.ui.components.Sprites;

/**
 * O que o jogador montou no menu e no cenário. Guarda os obstáculos colocados para que
 * "Jogar de novo" recrie exatamente o mesmo tabuleiro.
 */
public class ConfiguracaoPartida {

    public enum TipoObstaculo { FANTASMA, ROCHA }

    public record ObstaculoColocado(TipoObstaculo tipo, String cor, int x, int y) {
    }

    private ModoJogo modo = ModoJogo.MANUAL;
    private String cor1 = "amarelo";
    private String cor2 = "vermelho";
    private int xFruta = 0;
    private int yFruta = Tabuleiro.TAMANHO - 1;
    private final int indiceFruta = new Random().nextInt(Sprites.FRUTAS.length);
    private final List<ObstaculoColocado> obstaculos = new ArrayList<>();

    public ModoJogo getModo() {
        return modo;
    }

    public void setModo(ModoJogo modo) {
        this.modo = modo;
    }

    public String getCor1() {
        return cor1;
    }

    public void setCor1(String cor1) {
        this.cor1 = cor1;
    }

    public String getCor2() {
        return cor2;
    }

    public void setCor2(String cor2) {
        this.cor2 = cor2;
    }

    public int getXFruta() {
        return xFruta;
    }

    public int getYFruta() {
        return yFruta;
    }

    public void setFruta(int x, int y) {
        this.xFruta = x;
        this.yFruta = y;
    }

    public int getIndiceFruta() {
        return indiceFruta;
    }

    public List<ObstaculoColocado> getObstaculos() {
        return obstaculos;
    }

    /** Tabuleiro com a fruta e os obstáculos colocados (validados pelo próprio {@link Tabuleiro}). */
    public Tabuleiro criarTabuleiro() {
        Tabuleiro tabuleiro = new Tabuleiro(xFruta, yFruta);
        if (modo.isComObstaculos()) {
            for (ObstaculoColocado o : obstaculos) {
                tabuleiro.adicionarObstaculo(
                        o.tipo() == TipoObstaculo.FANTASMA ? new Fantasma(o.cor()) : new Rocha(), o.x(), o.y());
            }
        }
        return tabuleiro;
    }

    /** Mesmos robôs que a interface anterior criava para cada modo. */
    public Robo[] criarRobos() {
        if (modo.getQuantidadeRobos() == 1) {
            return new Robo[]{new Robo(cor1)};
        }
        return new Robo[]{
                new Robo(cor1),
                modo.isComRoboInteligente() ? new RoboInteligente(cor2) : new Robo(cor2)
        };
    }
}
