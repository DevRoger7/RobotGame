package robotgame.modelo;

public class Fantasma extends Bomba {

    private final String cor;

    public Fantasma(String cor) {
        this.cor = cor;
    }

    public String getCor() {
        return cor;
    }

    @Override
    public String getNome() {
        return "Fantasma";
    }

    @Override
    public char getSimbolo() {
        return 'F';
    }
}
