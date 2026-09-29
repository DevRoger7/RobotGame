package robotgame.modelo;

public class Rocha extends Obstaculo {

    @Override
    public void bater(Robo robo, Tabuleiro tabuleiro) {
        robo.voltarPosicaoAnterior();
    }

    @Override
    public boolean bloqueiaPassagem() {
        return true;
    }

    @Override
    public String getNome() {
        return "Rocha";
    }

    @Override
    public char getSimbolo() {
        return 'P';
    }
}
