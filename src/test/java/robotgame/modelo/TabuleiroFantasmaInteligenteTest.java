package robotgame.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TabuleiroFantasmaInteligenteTest {

    private Tabuleiro tabuleiro;

    @BeforeEach
    void criarTabuleiro() {
        tabuleiro = new Tabuleiro(2, 2);
    }

    @Test
    void getTamanhoRetornaConstante() {
        assertEquals(Tabuleiro.TAMANHO, tabuleiro.getTamanho());
    }

    @Test
    void aceitaFantasmaEmCasaLivre() {
        FantasmaInteligente f = new FantasmaInteligente();
        tabuleiro.setFantasmaInteligente(f);
        assertSame(f, tabuleiro.getFantasmaInteligente());
    }

    @Test
    void recusaFantasmaForaDoTabuleiro() {
        assertThrows(IllegalArgumentException.class,
                () -> tabuleiro.setFantasmaInteligente(new FantasmaInteligente(4, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> tabuleiro.setFantasmaInteligente(new FantasmaInteligente(0, -1)));
        assertNull(tabuleiro.getFantasmaInteligente());
    }

    @Test
    void recusaFantasmaEmCasaComObstaculo() {
        tabuleiro.adicionarObstaculo(new Bomba(), 3, 3);
        assertThrows(IllegalArgumentException.class,
                () -> tabuleiro.setFantasmaInteligente(new FantasmaInteligente(3, 3)));
        assertNull(tabuleiro.getFantasmaInteligente());
    }

    @Test
    void recusaFantasmaNaCasaDaFruta() {
        assertThrows(IllegalArgumentException.class,
                () -> tabuleiro.setFantasmaInteligente(new FantasmaInteligente(2, 2)));
        assertNull(tabuleiro.getFantasmaInteligente());
    }

    @Test
    void adicionarObstaculoRecusaCasaDoFantasma() {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente());
        assertThrows(IllegalArgumentException.class,
                () -> tabuleiro.adicionarObstaculo(new Bomba(), 3, 3));
        assertNull(tabuleiro.getObstaculo(3, 3));
    }

    @Test
    void adicionarObstaculoSemFantasmaContinuaFuncionando() {
        tabuleiro.adicionarObstaculo(new Bomba(), 3, 3);
        assertNotNull(tabuleiro.getObstaculo(3, 3));
    }

    @Test
    void adicionarObstaculoEmOutraCasaComFantasmaPresente() {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente());
        tabuleiro.adicionarObstaculo(new Bomba(), 1, 3);
        assertNotNull(tabuleiro.getObstaculo(1, 3));
    }

    @Test
    void removerFantasmaLiberaACasa() {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente());
        tabuleiro.removerFantasmaInteligente();
        assertNull(tabuleiro.getFantasmaInteligente());
        tabuleiro.adicionarObstaculo(new Bomba(), 3, 3);
        assertNotNull(tabuleiro.getObstaculo(3, 3));
    }
}
