package robotgame.modelo;

import java.util.Random;
import java.util.function.Consumer;

import robotgame.excecao.MovimentoInvalidoException;

public class Robo {

    private int x;
    private int y;
    private int xAnterior;
    private int yAnterior;
    private final String cor;
    private boolean ativo;
    private int movimentosValidos;
    private int movimentosInvalidos;
    private Consumer<EventoPartida> ouvinteMovimentoInvalido;
    private Poder poder;

    public Robo(String cor) {
        this(cor, 0, 0);
    }

    // Posição inicial diferente de (0,0), usada no modo PvP.
    public Robo(String cor, int x, int y) {
        if (!Tabuleiro.dentroDosLimites(x, y)) {
            throw new IllegalArgumentException("Posição inicial (" + x + "," + y + ") fora do tabuleiro.");
        }
        this.cor = cor;
        this.x = x;
        this.y = y;
        this.xAnterior = x;
        this.yAnterior = y;
        this.ativo = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getCor() {
        return cor;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getMovimentosValidos() {
        return movimentosValidos;
    }

    public int getMovimentosInvalidos() {
        return movimentosInvalidos;
    }

    public int getTotalMovimentos() {
        return movimentosValidos + movimentosInvalidos;
    }

    public boolean isComPoder() {
        return poder != null;
    }

    public int getTurnosDePoder() {
        return poder == null ? 0 : poder.getTurnosRestantes();
    }

    public void darPoder() {
        poder = new Poder();
    }

    public void gastarTurnoDePoder() {
        if (poder == null) {
            return;
        }
        poder.gastarTurno();
        if (poder.acabou()) {
            poder = null;
        }
    }

    public void perderPoder() {
        poder = null;
    }

    public String decidirDirecao(Tabuleiro t, Random random) {
        FantasmaInteligente fantasma = t.getFantasmaInteligente();
        if (isComPoder() && fantasma != null) {
            // Com poder, o pacman caça o fantasma: desvia de rochas, mas passa por cima de fantasmas burros.
            String direcao = BuscaCaminho.proximaDirecao(t, x, y, fantasma.getX(), fantasma.getY(), true);
            if (direcao != null) {
                return direcao;
            }
        }
        return direcaoAleatoria(random);
    }

    protected String direcaoAleatoria(Random random) {
        String[] direcoes = {"up", "down", "right", "left"};
        return direcoes[random.nextInt(direcoes.length)];
    }

    public void setOuvinteMovimentoInvalido(Consumer<EventoPartida> ouvinte) {
        this.ouvinteMovimentoInvalido = ouvinte;
    }

    public void setX(int x) throws MovimentoInvalidoException {
        if (x < 0 || x > Tabuleiro.TAMANHO - 1) {
            throw new MovimentoInvalidoException("x=" + x);
        }
        this.x = x;
    }

    public void setY(int y) throws MovimentoInvalidoException {
        if (y < 0 || y > Tabuleiro.TAMANHO - 1) {
            throw new MovimentoInvalidoException("y=" + y);
        }
        this.y = y;
    }

    public void mover(String direcao) throws MovimentoInvalidoException {
        if (!"up".equals(direcao) && !"down".equals(direcao)
                && !"right".equals(direcao) && !"left".equals(direcao)) {
            movimentosInvalidos++;
            avisarMovimentoInvalido(direcao);
            throw new MovimentoInvalidoException(direcao);
        }

        int xAntes = x;
        int yAntes = y;

        try {
            switch (direcao) {
                case "up" -> setY(y + 1);
                case "down" -> setY(y - 1);
                case "right" -> setX(x + 1);
                case "left" -> setX(x - 1);
            }
        } catch (MovimentoInvalidoException e) {
            movimentosInvalidos++;
            avisarMovimentoInvalido(direcao);
            throw new MovimentoInvalidoException(direcao);
        }

        xAnterior = xAntes;
        yAnterior = yAntes;
        movimentosValidos++;
    }

    public void mover(int direcao) throws MovimentoInvalidoException {
        switch (direcao) {
            case 1 -> mover("up");
            case 2 -> mover("down");
            case 3 -> mover("right");
            case 4 -> mover("left");
            default -> {
                movimentosInvalidos++;
                avisarMovimentoInvalido(String.valueOf(direcao));
                throw new MovimentoInvalidoException(String.valueOf(direcao));
            }
        }
    }

    public boolean encontrouAlimento(int xAlimento, int yAlimento) {
        return x == xAlimento && y == yAlimento;
    }

    void voltarPosicaoAnterior() {
        x = xAnterior;
        y = yAnterior;
    }

    void explodir() {
        ativo = false;
    }

    private void avisarMovimentoInvalido(String direcao) {
        if (ouvinteMovimentoInvalido != null) {
            ouvinteMovimentoInvalido.accept(
                    new EventoPartida(EventoPartida.Tipo.INVALIDO, this, direcao, x, y, null));
        }
    }
}
