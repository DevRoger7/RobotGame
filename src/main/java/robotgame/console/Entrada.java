package robotgame.console;

import java.util.Scanner;

import robotgame.modelo.Tabuleiro;

public class Entrada {

    private Entrada() {
    }

    public static int lerInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String linha = sc.nextLine().trim();
            try {
                int valor = Integer.parseInt(linha);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("Valor fora da faixa. Digite um número de " + min + " a " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número de " + min + " a " + max + ".");
            }
        }
    }

    public static String lerTexto(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String linha = sc.nextLine().trim();
            if (!linha.isEmpty()) {
                return linha;
            }
            System.out.println("O texto não pode ser vazio.");
        }
    }

    public static Tabuleiro lerTabuleiro(Scanner sc) {
        int max = Tabuleiro.TAMANHO - 1;
        while (true) {
            int x = lerInt(sc, "Posição X do alimento (0 a " + max + "): ", 0, max);
            int y = lerInt(sc, "Posição Y do alimento (0 a " + max + "): ", 0, max);
            try {
                return new Tabuleiro(x, y);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
