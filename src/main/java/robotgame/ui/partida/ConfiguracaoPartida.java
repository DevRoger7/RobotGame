package robotgame.ui.partida;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import robotgame.modelo.Fantasma;
import robotgame.modelo.FantasmaAleatorio;
import robotgame.modelo.FantasmaInteligente;
import robotgame.modelo.ModoJogo;
import robotgame.modelo.Robo;
import robotgame.modelo.RoboInteligente;
import robotgame.modelo.RoboSuperInteligente;
import robotgame.modelo.Rocha;
import robotgame.modelo.Tabuleiro;
import robotgame.ui.components.Sprites;

public class ConfiguracaoPartida {

    public enum TipoObstaculo { FANTASMA, ROCHA }

    public record ObstaculoColocado(TipoObstaculo tipo, String cor, int x, int y) {
    }

    private ModoJogo modo = ModoJogo.MANUAL;
    private String cor1 = "amarelo";
    private String cor2 = "vermelho";
    private String cor3 = "verde";
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

    public String getCor3() {
        return cor3;
    }

    public void setCor3(String cor3) {
        this.cor3 = cor3;
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

    public Tabuleiro criarTabuleiro() {
        if (modo.isPvp()) {
            Tabuleiro semFruta = Tabuleiro.semAlimento();
            semFruta.setFantasmaAleatorio(new FantasmaAleatorio());
            // No PvP só há rochas: fantasmas colocados em outro modo são ignorados.
            for (ObstaculoColocado o : obstaculos) {
                if (o.tipo() == TipoObstaculo.ROCHA) {
                    semFruta.adicionarObstaculo(new Rocha(), o.x(), o.y());
                }
            }
            return semFruta;
        }
        Tabuleiro tabuleiro = new Tabuleiro(xFruta, yFruta);
        if (modo.isComObstaculos()) {
            for (ObstaculoColocado o : obstaculos) {
                tabuleiro.adicionarObstaculo(
                        o.tipo() == TipoObstaculo.FANTASMA ? new Fantasma(o.cor()) : new Rocha(), o.x(), o.y());
            }
        }
        if (modo == ModoJogo.PACMAN_PODER) {
            tabuleiro.setFantasmaInteligente(new FantasmaInteligente());
        }
        return tabuleiro;
    }

    public Robo[] criarRobos() {
        if (modo.isPvp()) {
            // Jogador 1 nasce no canto superior direito, jogador 2 em (0,0).
            int canto = Tabuleiro.TAMANHO - 1;
            return new Robo[]{new Robo(cor1, canto, canto), new Robo(cor2)};
        }
        if (modo.getQuantidadeRobos() == 1) {
            return new Robo[]{new Robo(cor1)};
        }
        if (modo == ModoJogo.PACMAN_PODER) {
            return new Robo[]{new Robo(cor1), new RoboInteligente(cor2), new RoboSuperInteligente(cor3)};
        }
        return new Robo[]{
                new Robo(cor1),
                modo.isComRoboInteligente() ? new RoboInteligente(cor2) : new Robo(cor2)
        };
    }
}
