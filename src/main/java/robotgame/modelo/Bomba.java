package robotgame.modelo;

public class Bomba extends Obstaculo {

    @Override
    public void bater(Robo robo, Tabuleiro tabuleiro) {
        robo.explodir();
        tabuleiro.removerObstaculo(robo.getX(), robo.getY());
    }

    @Override
    public String getNome() {
        return "Bomba";
    }

    @Override
    public char getSimbolo() {
        return 'B';
    }
}
