package robotgame.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BuscaCaminhoTest {

    private Tabuleiro tabuleiro;

    @BeforeEach
    void criarTabuleiro() {
        tabuleiro = new Tabuleiro(3, 3);
    }

    @Test
    void caminhoLivreTemDistanciaDeManhattan() {
        assertEquals(6, BuscaCaminho.distancia(tabuleiro, 0, 0, 3, 3, false));
        assertEquals(4, BuscaCaminho.distancia(tabuleiro, 1, 2, 3, 0, true));
        assertEquals(1, BuscaCaminho.distancia(tabuleiro, 2, 2, 2, 3, false));
    }

    @Test
    void direcoesSeguemAConvencaoDoRobo() {
        assertEquals("up", BuscaCaminho.proximaDirecao(tabuleiro, 1, 1, 1, 3, false));
        assertEquals("down", BuscaCaminho.proximaDirecao(tabuleiro, 1, 1, 1, 0, false));
        assertEquals("right", BuscaCaminho.proximaDirecao(tabuleiro, 1, 1, 3, 1, false));
        assertEquals("left", BuscaCaminho.proximaDirecao(tabuleiro, 1, 1, 0, 1, false));
    }

    @Test
    void empateSegueAOrdemUpDownRightLeft() {
        assertEquals("up", BuscaCaminho.proximaDirecao(tabuleiro, 1, 1, 2, 2, false));
        assertEquals("down", BuscaCaminho.proximaDirecao(tabuleiro, 1, 1, 2, 0, false));
        assertEquals("right", BuscaCaminho.proximaDirecao(tabuleiro, 1, 1, 2, 1, false));
        assertEquals("up", BuscaCaminho.proximaDirecao(tabuleiro, 0, 0, 3, 3, false));
    }

    @Test
    void contornaRocha() {
        tabuleiro.adicionarObstaculo(new Rocha(), 1, 0);

        assertEquals(4, BuscaCaminho.distancia(tabuleiro, 0, 0, 2, 0, false));
        assertEquals("up", BuscaCaminho.proximaDirecao(tabuleiro, 0, 0, 2, 0, false));
    }

    @Test
    void destinoCercadoPorRochasNaoTemCaminho() {
        tabuleiro.adicionarObstaculo(new Rocha(), 2, 0);
        tabuleiro.adicionarObstaculo(new Rocha(), 3, 1);

        assertEquals(-1, BuscaCaminho.distancia(tabuleiro, 0, 0, 3, 0, false));
        assertEquals(-1, BuscaCaminho.distancia(tabuleiro, 0, 0, 3, 0, true));
        assertNull(BuscaCaminho.proximaDirecao(tabuleiro, 0, 0, 3, 0, false));
        assertNull(BuscaCaminho.proximaDirecao(tabuleiro, 0, 0, 3, 0, true));
    }

    @Test
    void origemIgualDestino() {
        assertEquals(0, BuscaCaminho.distancia(tabuleiro, 2, 1, 2, 1, false));
        assertNull(BuscaCaminho.proximaDirecao(tabuleiro, 2, 1, 2, 1, false));
    }

    @Test
    void destinoForaDoTabuleiroNaoTemCaminho() {
        assertEquals(-1, BuscaCaminho.distancia(tabuleiro, 0, 0, 4, 0, true));
        assertNull(BuscaCaminho.proximaDirecao(tabuleiro, 0, 0, 0, -1, true));
    }

    @Test
    void fantasmaBurroEParedeQuandoNaoPodeAtravessar() {
        tabuleiro.adicionarObstaculo(new Fantasma("vermelho"), 1, 0);

        assertEquals(4, BuscaCaminho.distancia(tabuleiro, 0, 0, 2, 0, false));
        assertEquals("up", BuscaCaminho.proximaDirecao(tabuleiro, 0, 0, 2, 0, false));
    }

    @Test
    void fantasmaBurroPodeSerAtravessadoQuandoPermitido() {
        tabuleiro.adicionarObstaculo(new Fantasma("vermelho"), 1, 0);

        assertEquals(2, BuscaCaminho.distancia(tabuleiro, 0, 0, 2, 0, true));
        assertEquals("right", BuscaCaminho.proximaDirecao(tabuleiro, 0, 0, 2, 0, true));
    }

    @Test
    void fantasmaBurroPodeFecharOCaminhoSoQuandoNaoAtravessa() {
        tabuleiro.adicionarObstaculo(new Rocha(), 0, 1);
        tabuleiro.adicionarObstaculo(new Fantasma("vermelho"), 1, 0);

        assertEquals(-1, BuscaCaminho.distancia(tabuleiro, 0, 0, 2, 0, false));
        assertNull(BuscaCaminho.proximaDirecao(tabuleiro, 0, 0, 2, 0, false));
        assertEquals(2, BuscaCaminho.distancia(tabuleiro, 0, 0, 2, 0, true));
    }

    @Test
    void rochaEParedeComOuSemPermissaoParaFantasma() {
        tabuleiro.adicionarObstaculo(new Rocha(), 1, 0);

        assertEquals(4, BuscaCaminho.distancia(tabuleiro, 0, 0, 2, 0, false));
        assertEquals(4, BuscaCaminho.distancia(tabuleiro, 0, 0, 2, 0, true));
        assertEquals("up", BuscaCaminho.proximaDirecao(tabuleiro, 0, 0, 2, 0, true));
    }

    @Test
    void origemNuncaEParede() {
        tabuleiro.adicionarObstaculo(new Fantasma("azul"), 1, 1);

        assertEquals(1, BuscaCaminho.distancia(tabuleiro, 1, 1, 1, 2, false));
        assertEquals("up", BuscaCaminho.proximaDirecao(tabuleiro, 1, 1, 1, 2, false));
    }

    @Test
    void fantasmaInteligenteNaoEParede() {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(1, 0));

        assertEquals(2, BuscaCaminho.distancia(tabuleiro, 0, 0, 2, 0, false));
    }
}
