package robotgame.console;

import robotgame.modelo.Obstaculo;
import robotgame.modelo.Robo;
import robotgame.modelo.Tabuleiro;

public class VisualizacaoConsole {

    private static final String RESET = "\u001B[0m";

    private VisualizacaoConsole() {
    }

    public static void exibir(Tabuleiro t, Robo... robos) {
        int n = Tabuleiro.TAMANHO;
        String[][] textos = new String[n][n];
        String[][] coloridos = new String[n][n];
        int largura = String.valueOf(n - 1).length();

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                StringBuilder texto = new StringBuilder();
                StringBuilder colorido = new StringBuilder();
                for (int i = 0; i < robos.length; i++) {
                    Robo r = robos[i];
                    if (r.isAtivo() && r.getX() == x && r.getY() == y) {
                        String numero = String.valueOf(i + 1);
                        texto.append(numero);
                        colorido.append(colorir(numero, r.getCor()));
                    }
                }
                if (texto.isEmpty()) {
                    Obstaculo o = t.getObstaculo(x, y);
                    String simbolo;
                    if (x == t.getXAlimento() && y == t.getYAlimento()) {
                        simbolo = "A";
                    } else if (o != null) {
                        simbolo = String.valueOf(o.getSimbolo());
                    } else {
                        simbolo = ".";
                    }
                    texto.append(simbolo);
                    colorido.append(simbolo);
                }
                textos[x][y] = texto.toString();
                coloridos[x][y] = colorido.toString();
                largura = Math.max(largura, texto.length());
            }
        }

        int larguraLinha = String.valueOf(n - 1).length();
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(larguraLinha)).append(" |");
        for (int x = 0; x < n; x++) {
            sb.append(' ').append(preencher(String.valueOf(x), String.valueOf(x), largura));
        }
        sb.append('\n');
        sb.append("-".repeat(larguraLinha)).append("-+").append("-".repeat(n * (largura + 1))).append('\n');
        for (int y = n - 1; y >= 0; y--) {
            sb.append(preencher(String.valueOf(y), String.valueOf(y), larguraLinha)).append(" |");
            for (int x = 0; x < n; x++) {
                sb.append(' ').append(preencher(coloridos[x][y], textos[x][y], largura));
            }
            sb.append('\n');
        }
        System.out.print(sb);

        for (int i = 0; i < robos.length; i++) {
            Robo r = robos[i];
            System.out.println(colorir(String.valueOf(i + 1), r.getCor()) + " = "
                    + r.getClass().getSimpleName() + " (" + r.getCor() + ")"
                    + (r.isAtivo() ? "" : " (explodiu)"));
        }
        System.out.println("A = Alimento   B = Bomba   P = Rocha");
        System.out.println();
    }

    public static void pausar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void mostrarEstatisticas(Robo r) {
        System.out.println("Robô " + r.getCor() + " (" + r.getClass().getSimpleName() + "): "
                + r.getTotalMovimentos() + " movimentos — "
                + r.getMovimentosValidos() + " válidos, "
                + r.getMovimentosInvalidos() + " inválidos");
    }

    private static String preencher(String conteudo, String textoPuro, int largura) {
        return conteudo + " ".repeat(largura - textoPuro.length());
    }

    private static String colorir(String texto, String cor) {
        String codigo = switch (cor.trim().toLowerCase()) {
            case "vermelho" -> "\u001B[31m";
            case "verde" -> "\u001B[32m";
            case "amarelo" -> "\u001B[33m";
            case "azul" -> "\u001B[34m";
            case "roxo", "magenta" -> "\u001B[35m";
            case "ciano" -> "\u001B[36m";
            default -> null;
        };
        return codigo == null ? texto : codigo + texto + RESET;
    }
}
