package robotgame.console;

import java.util.Random;
import java.util.Scanner;

import robotgame.excecao.MovimentoInvalidoException;
import robotgame.modelo.Robo;
import robotgame.modelo.RoboInteligente;
import robotgame.modelo.Tabuleiro;

public class Main3 {

    private static final long PAUSA_MS = 700;
    private static final String[] DIRECOES = {"up", "down", "right", "left"};

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random random = new Random();

        Robo[] robos = new Robo[2];
        robos[0] = new Robo(Entrada.lerTexto(sc, "Cor do robô normal: "));
        robos[1] = new RoboInteligente(Entrada.lerTexto(sc, "Cor do robô inteligente: "));
        Tabuleiro tabuleiro = Entrada.lerTabuleiro(sc);

        VisualizacaoConsole.exibir(tabuleiro, robos);

        int vez = 0;
        while (!tabuleiro.alimentoEncontradoPor(robos[0]) || !tabuleiro.alimentoEncontradoPor(robos[1])) {
            Robo robo = robos[vez];
            vez = (vez + 1) % robos.length;
            if (tabuleiro.alimentoEncontradoPor(robo)) {
                continue;
            }

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
                System.out.println(">>> Robô " + robo.getCor() + " encontrou o alimento!");
            }
            VisualizacaoConsole.pausar(PAUSA_MS);
        }

        System.out.println("Os dois robôs encontraram o alimento!");
        for (Robo r : robos) {
            VisualizacaoConsole.mostrarEstatisticas(r);
        }
    }
}
