package robotgame.console;

import java.util.Scanner;

import robotgame.excecao.MovimentoInvalidoException;
import robotgame.modelo.Robo;
import robotgame.modelo.Tabuleiro;

public class Main1 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String cor = Entrada.lerTexto(sc, "Cor do robô: ");
        Robo robo = new Robo(cor);
        Tabuleiro tabuleiro = Entrada.lerTabuleiro(sc);

        System.out.println("Formato dos movimentos:");
        System.out.println("  1 - Palavras em inglês (up, down, right, left)");
        System.out.println("  2 - Números (1 = up, 2 = down, 3 = right, 4 = left)");
        int formato = Entrada.lerInt(sc, "Escolha o formato (1 ou 2): ", 1, 2);
        boolean modoPalavras = formato == 1;

        while (!tabuleiro.alimentoEncontradoPor(robo)) {
            VisualizacaoConsole.exibir(tabuleiro, robo);
            System.out.print(modoPalavras
                    ? "Movimento (up, down, right, left): "
                    : "Movimento (1 = up, 2 = down, 3 = right, 4 = left): ");
            String comando = sc.nextLine().trim();
            try {
                if (modoPalavras) {
                    robo.mover(comando);
                } else {
                    robo.mover(Integer.parseInt(comando));
                }
                System.out.println("Robô " + robo.getCor() + " agora em (" + robo.getX() + "," + robo.getY() + ")");
            } catch (MovimentoInvalidoException e) {
                System.out.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas o número do movimento.");
            }
        }

        VisualizacaoConsole.exibir(tabuleiro, robo);
        System.out.println("Robô " + robo.getCor() + " encontrou o alimento em ("
                + robo.getX() + "," + robo.getY() + ")!");
        VisualizacaoConsole.mostrarEstatisticas(robo);
    }
}
