package robotgame.modelo;

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

    public Robo(String cor) {
        this.cor = cor;
        this.x = 0;
        this.y = 0;
        this.xAnterior = 0;
        this.yAnterior = 0;
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
}
