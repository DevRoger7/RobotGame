package robotgame.console;

import java.util.Random;
import java.util.Scanner;

import robotgame.excecao.MovimentoInvalidoException;
import robotgame.modelo.Robo;
import robotgame.modelo.Tabuleiro;

public class Main2 {

    private static final long PAUSA_MS = 700;
    private static final String[] DIRECOES = {"up", "down", "right", "left"};

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random random = new Random();

        Robo[] robos = {
                new Robo(Entrada.lerTexto(sc, "Cor do robô 1: ")),
                new Robo(Entrada.lerTexto(sc, "Cor do robô 2: "))
        };
        Tabuleiro tabuleiro = Entrada.lerTabuleiro(sc);

        VisualizacaoConsole.exibir(tabuleiro, robos);

        Robo vencedor = null;
        int vez = 0;
        while (vencedor == null) {
            Robo robo = robos[vez];
            int direcao = random.nextInt(4) + 1;
            String nome = DIRECOES[direcao - 1];
            try {
                robo.mover(direcao);
                System.out.println("Robô " + robo.getCor() + " tenta " + nome
                        + ": agora em (" + robo.getX() + "," + robo.getY() + ")");
            } catch (MovimentoInvalidoException e) {
                System.out.println("Robô " + robo.getCor() + " tenta " + nome + ": " + e.getMessage());
            }
            VisualizacaoConsole.exibir(tabuleiro, robos);

            if (tabuleiro.alimentoEncontradoPor(robo)) {
                vencedor = robo;
            } else {
                vez = (vez + 1) % robos.length;
                VisualizacaoConsole.pausar(PAUSA_MS);
            }
        }

        System.out.println("Robô " + vencedor.getCor() + " encontrou o alimento!");
        for (Robo r : robos) {
            VisualizacaoConsole.mostrarEstatisticas(r);
        }
    }
}
