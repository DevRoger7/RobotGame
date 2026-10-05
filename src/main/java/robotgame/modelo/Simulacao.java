package robotgame.modelo;

import java.util.Random;
import java.util.function.Consumer;

import robotgame.excecao.MovimentoInvalidoException;

public class Simulacao {

    public static final int MAX_RODADAS = 1000;
    private static final String[] DIRECOES = {"up", "down", "right", "left"};

    public record Passo(Robo robo, String direcao, String mensagem) {
    }

    private final ModoJogo modo;
    private final Tabuleiro tabuleiro;
    private final Robo[] robos;
    private final Random random = new Random();
    private Consumer<EventoPartida> ouvinte = evento -> {};

    private int vez;
    private int rodadas;
    private String resultado;

    public Simulacao(ModoJogo modo, Tabuleiro tabuleiro, Robo... robos) {
        if (modo.isManual()) {
            throw new IllegalArgumentException("O modo manual não é simulado.");
        }
        this.modo = modo;
        this.tabuleiro = tabuleiro;
        this.robos = robos;
    }

    public void setOuvinte(Consumer<EventoPartida> ouvinte) {
        this.ouvinte = ouvinte;
    }

    public boolean isTerminada() {
        return resultado != null;
    }

    public String getResultado() {
        return resultado;
    }

    public Passo proximoPasso() {
        if (isTerminada()) {
            throw new IllegalStateException("A simulação já terminou.");
        }

        Robo robo = proximoRoboQuePodeJogar();
        int xAntes = robo.getX();
        int yAntes = robo.getY();
        String tentativa = DIRECOES[random.nextInt(DIRECOES.length)];
        String direcao = tentativa;
        StringBuilder mensagem = new StringBuilder("Pacman " + robo.getCor() + " tenta " + tentativa);

        try {
            robo.mover(tentativa);
            direcao = direcaoDoDeslocamento(xAntes, yAntes, robo.getX(), robo.getY());
            mensagem.append(": agora em ").append(posicao(robo));

            int xObstaculo = robo.getX();
            int yObstaculo = robo.getY();
            Obstaculo obstaculo = tabuleiro.getObstaculo(xObstaculo, yObstaculo);
            if (obstaculo != null) {
                mensagem.append(". Bateu em ").append(obstaculo.getNome()).append(" #").append(obstaculo.getId());
                obstaculo.bater(robo, tabuleiro);
                mensagem.append(robo.isAtivo() ? " e voltou para " + posicao(robo) : " e foi eliminado!");
                publicar(robo.isAtivo() ? EventoPartida.Tipo.ROCHA : EventoPartida.Tipo.EXPLODIU, robo, direcao, obstaculo);
                if (tabuleiro.getObstaculo(xObstaculo, yObstaculo) == null) {
                    ouvinte.accept(new EventoPartida(EventoPartida.Tipo.FANTASMA_SUMIU, robo, direcao,
                            xObstaculo, yObstaculo, obstaculo));
                }
            } else if (tabuleiro.alimentoEncontradoPor(robo)) {
                publicar(EventoPartida.Tipo.ACHOU_ALIMENTO, robo, direcao, null);
            } else {
                publicar(EventoPartida.Tipo.MOVEU, robo, direcao, null);
            }
        } catch (MovimentoInvalidoException e) {
            mensagem.append(": ").append(e.getMessage());
        }

        if (modo == ModoJogo.NORMAL_X_INTELIGENTE && tabuleiro.alimentoEncontradoPor(robo)) {
            mensagem.append(" — encontrou o alimento!");
        }
        verificarFim(robo);
        return new Passo(robo, direcao, mensagem.toString());
    }

    private Robo proximoRoboQuePodeJogar() {
        while (true) {
            Robo robo = robos[vez];
            vez = (vez + 1) % robos.length;
            if (vez == 0) {
                rodadas++;
            }
            if (podeJogar(robo)) {
                return robo;
            }
        }
    }

    private boolean podeJogar(Robo robo) {
        if (!robo.isAtivo()) {
            return false;
        }
        return modo != ModoJogo.NORMAL_X_INTELIGENTE || !tabuleiro.alimentoEncontradoPor(robo);
    }

    private void verificarFim(Robo robo) {
        if (modo == ModoJogo.NORMAL_X_INTELIGENTE) {
            boolean todosAcharam = true;
            for (Robo r : robos) {
                todosAcharam &= tabuleiro.alimentoEncontradoPor(r);
            }
            if (todosAcharam) {
                resultado = "Os dois pacmans encontraram o alimento!";
            }
            return;
        }

        if (robo.isAtivo() && tabuleiro.alimentoEncontradoPor(robo)) {
            resultado = "Pacman " + robo.getCor() + " encontrou o alimento e venceu!";
        } else if (!algumAtivo()) {
            resultado = "Os dois pacmans foram eliminados!";
        } else if (rodadas >= MAX_RODADAS) {
            resultado = "Limite de " + MAX_RODADAS + " rodadas atingido: nenhum robô encontrou o alimento.";
        }
    }

    private boolean algumAtivo() {
        for (Robo r : robos) {
            if (r.isAtivo()) {
                return true;
            }
        }
        return false;
    }

    private void publicar(EventoPartida.Tipo tipo, Robo robo, String direcao, Obstaculo obstaculo) {
        ouvinte.accept(new EventoPartida(tipo, robo, direcao, robo.getX(), robo.getY(), obstaculo));
    }

    private static String direcaoDoDeslocamento(int xAntes, int yAntes, int x, int y) {
        if (y > yAntes) {
            return "up";
        }
        if (y < yAntes) {
            return "down";
        }
        return x < xAntes ? "left" : "right";
    }

    private static String posicao(Robo robo) {
        return "(" + robo.getX() + "," + robo.getY() + ")";
    }
}
