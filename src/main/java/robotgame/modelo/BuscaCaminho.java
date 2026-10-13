package robotgame.modelo;

public final class BuscaCaminho {

    private BuscaCaminho() {
    }

    // Rocha é sempre parede; fantasma burro só é parede quando atravessaFantasmaBurro é false.
    // Retorna -1 quando não há caminho.
    public static int distancia(Tabuleiro t, int xOrigem, int yOrigem, int xDestino, int yDestino,
                                boolean atravessaFantasmaBurro) {
        throw new UnsupportedOperationException("TODO");
    }

    // Retorna null quando já está no destino ou não há caminho.
    public static String proximaDirecao(Tabuleiro t, int xOrigem, int yOrigem, int xDestino, int yDestino,
                                        boolean atravessaFantasmaBurro) {
        throw new UnsupportedOperationException("TODO");
    }
}
