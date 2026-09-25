package robotgame.console;

import java.util.Random;
import java.util.Scanner;

import robotgame.excecao.MovimentoInvalidoException;
import robotgame.modelo.Bomba;
import robotgame.modelo.Obstaculo;
import robotgame.modelo.Robo;
import robotgame.modelo.RoboInteligente;
import robotgame.modelo.Rocha;
import robotgame.modelo.Tabuleiro;

public class Main4 {

    private static final long PAUSA_MS = 700;
    private static final int MAX_RODADAS = 1000;
    private static final String[] DIRECOES = {"up", "down", "right", "left"};

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random random = new Random();

        Robo[] robos = new Robo[2];
        robos[0] = new Robo(Entrada.lerTexto(sc, "Cor do robô normal: "));
        robos[1] = new RoboInteligente(Entrada.lerTexto(sc, "Cor do robô inteligente: "));
        Tabuleiro tabuleiro = Entrada.lerTabuleiro(sc);

        int maxObstaculos = Tabuleiro.TAMANHO * Tabuleiro.TAMANHO - 2;
        int bombas = Entrada.lerInt(sc, "Quantidade de bombas (0 a " + maxObstaculos + "): ", 0, maxObstaculos);
        int maxRochas = maxObstaculos - bombas;
        int rochas = Entrada.lerInt(sc, "Quantidade de rochas (0 a " + maxRochas + "): ", 0, maxRochas);

        for (int i = 0; i < bombas; i++) {
            posicionar(sc, tabuleiro, new Bomba());
        }
        for (int i = 0; i < rochas; i++) {
            posicionar(sc, tabuleiro, new Rocha());
        }

        VisualizacaoConsole.exibir(tabuleiro, robos);

        Robo vencedor = null;
        int rodadas = 0;
        while (vencedor == null && algumAtivo(robos) && rodadas < MAX_RODADAS) {
            for (Robo robo : robos) {
                if (!robo.isAtivo()) {
                    continue;
                }

                int direcao = random.nextInt(4) + 1;
                String nome = DIRECOES[direcao - 1];
                try {
                    robo.mover(direcao);
                    System.out.println("Robô " + robo.getCor() + " tenta " + nome
                            + ": agora em (" + robo.getX() + "," + robo.getY() + ")");

                    Obstaculo obstaculo = tabuleiro.getObstaculo(robo.getX(), robo.getY());
                    if (obstaculo != null) {
                        System.out.println("Bateu em " + obstaculo.getNome() + " #" + obstaculo.getId() + "!");
                        obstaculo.bater(robo, tabuleiro);
                        if (robo.isAtivo()) {
                            System.out.println("Voltou para (" + robo.getX() + "," + robo.getY() + ")");
                        } else {
                            System.out.println("O robô " + robo.getCor() + " explodiu!");
                        }
                    }
                } catch (MovimentoInvalidoException e) {
                    System.out.println("Robô " + robo.getCor() + " tenta " + nome + ": " + e.getMessage());
                }
                VisualizacaoConsole.exibir(tabuleiro, robos);

                if (robo.isAtivo() && tabuleiro.alimentoEncontradoPor(robo)) {
                    vencedor = robo;
                    break;
                }
                VisualizacaoConsole.pausar(PAUSA_MS);
            }
            rodadas++;
        }

        if (vencedor != null) {
            System.out.println("Robô " + vencedor.getCor() + " encontrou o alimento!");
        } else if (!algumAtivo(robos)) {
            System.out.println("Os dois robôs explodiram!");
        } else {
            System.out.println("Limite de " + MAX_RODADAS + " rodadas atingido: nenhum robô encontrou o alimento.");
        }
        for (Robo r : robos) {
            VisualizacaoConsole.mostrarEstatisticas(r);
        }
    }

    private static void posicionar(Scanner sc, Tabuleiro tabuleiro, Obstaculo obstaculo) {
        int max = Tabuleiro.TAMANHO - 1;
        while (true) {
            System.out.println("Posição da " + obstaculo.getNome() + " #" + obstaculo.getId() + ":");
            int x = Entrada.lerInt(sc, "  X (0 a " + max + "): ", 0, max);
            int y = Entrada.lerInt(sc, "  Y (0 a " + max + "): ", 0, max);
            try {
                tabuleiro.adicionarObstaculo(obstaculo, x, y);
                return;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static boolean algumAtivo(Robo[] robos) {
        for (Robo r : robos) {
            if (r.isAtivo()) {
                return true;
            }
        }
        return false;
    }
}
