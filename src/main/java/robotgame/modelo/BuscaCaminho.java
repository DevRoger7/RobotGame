package robotgame.modelo;

import java.util.ArrayDeque;
import java.util.Deque;

public final class BuscaCaminho {

    private static final String[] DIRECOES = {"up", "down", "right", "left"};
    private static final int[][] PASSOS = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

    private BuscaCaminho() {
    }

    // Rocha é sempre parede; fantasma burro só é parede quando atravessaFantasmaBurro é false.
    // Retorna -1 quando não há caminho.
    public static int distancia(Tabuleiro t, int xOrigem, int yOrigem, int xDestino, int yDestino,
                                boolean atravessaFantasmaBurro) {
        int[][] distancias = buscar(t, xOrigem, yOrigem, xDestino, yDestino, atravessaFantasmaBurro)[0];
        return distancias == null ? -1 : distancias[xDestino][yDestino];
    }

    // Retorna null quando já está no destino ou não há caminho.
    public static String proximaDirecao(Tabuleiro t, int xOrigem, int yOrigem, int xDestino, int yDestino,
                                        boolean atravessaFantasmaBurro) {
        int[][][] busca = buscar(t, xOrigem, yOrigem, xDestino, yDestino, atravessaFantasmaBurro);
        if (busca[0] == null || busca[1][xDestino][yDestino] < 0) {
            return null;
        }
        return DIRECOES[busca[1][xDestino][yDestino]];
    }

    // Retorna {distâncias, primeiraDireção} por casa, ou {null, null} se o destino é inalcançável.
    // primeiraDireção é o índice em DIRECOES do primeiro passo do menor caminho (-1 na origem).
    private static int[][][] buscar(Tabuleiro t, int xOrigem, int yOrigem, int xDestino, int yDestino,
                                    boolean atravessaFantasmaBurro) {
        int tamanho = t.getTamanho();
        int[][][] inalcancavel = {null, null};
        if (!dentro(tamanho, xOrigem, yOrigem) || !dentro(tamanho, xDestino, yDestino)) {
            return inalcancavel;
        }

        int[][] distancias = new int[tamanho][tamanho];
        int[][] primeiraDirecao = new int[tamanho][tamanho];
        for (int x = 0; x < tamanho; x++) {
            for (int y = 0; y < tamanho; y++) {
                distancias[x][y] = -1;
                primeiraDirecao[x][y] = -1;
            }
        }
        distancias[xOrigem][yOrigem] = 0;

        Deque<int[]> fila = new ArrayDeque<>();
        fila.add(new int[]{xOrigem, yOrigem});
        while (!fila.isEmpty()) {
            int[] atual = fila.poll();
            if (atual[0] == xDestino && atual[1] == yDestino) {
                return new int[][][]{distancias, primeiraDirecao};
            }
            for (int i = 0; i < PASSOS.length; i++) {
                int nx = atual[0] + PASSOS[i][0];
                int ny = atual[1] + PASSOS[i][1];
                if (!dentro(tamanho, nx, ny) || distancias[nx][ny] >= 0 || ehParede(t, nx, ny, atravessaFantasmaBurro)) {
                    continue;
                }
                distancias[nx][ny] = distancias[atual[0]][atual[1]] + 1;
                primeiraDirecao[nx][ny] = primeiraDirecao[atual[0]][atual[1]] < 0
                        ? i
                        : primeiraDirecao[atual[0]][atual[1]];
                fila.add(new int[]{nx, ny});
            }
        }
        return inalcancavel;
    }

    private static boolean dentro(int tamanho, int x, int y) {
        return x >= 0 && x < tamanho && y >= 0 && y < tamanho;
    }

    private static boolean ehParede(Tabuleiro t, int x, int y, boolean atravessaFantasmaBurro) {
        Obstaculo o = t.getObstaculo(x, y);
        return o != null && (o.bloqueiaPassagem() || !atravessaFantasmaBurro);
    }
}
