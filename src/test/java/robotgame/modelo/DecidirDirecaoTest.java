package robotgame.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import robotgame.excecao.MovimentoInvalidoException;

class DecidirDirecaoTest {

    private static final List<String> DIRECOES = List.of("up", "down", "right", "left");

    private Tabuleiro tabuleiro;
    private Robo robo;

    @BeforeEach
    void criarCenario() {
        tabuleiro = new Tabuleiro(3, 3);
        robo = new Robo("amarelo");
    }

    @Test
    void semPoderRetornaUmaDasQuatroDirecoes() {
        Random random = new Random(42);
        for (int i = 0; i < 50; i++) {
            assertTrue(DIRECOES.contains(robo.decidirDirecao(tabuleiro, random)));
        }
    }

    @Test
    void semPoderIgnoraOFantasmaESorteia() {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(2, 0));

        assertSorteia(robo);
    }

    @Test
    void comPoderSegueOMenorCaminhoAteOFantasma() throws MovimentoInvalidoException {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(1, 2));
        robo.setX(1);
        robo.setY(0);
        robo.darPoder();

        assertEquals("up", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void comPoderDesviaDeRocha() throws MovimentoInvalidoException {
        tabuleiro.adicionarObstaculo(new Rocha(), 2, 1);
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(3, 1));
        robo.setX(1);
        robo.setY(1);
        robo.darPoder();

        assertEquals("up", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void comPoderAtravessaFantasmaBurro() {
        tabuleiro.adicionarObstaculo(new Fantasma("vermelho"), 1, 0);
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(2, 0));
        robo.darPoder();

        assertEquals("right", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void comPoderESemFantasmaSorteia() {
        robo.darPoder();

        assertSorteia(robo);
    }

    @Test
    void comPoderEFantasmaInalcancavelSorteia() {
        tabuleiro.adicionarObstaculo(new Rocha(), 2, 0);
        tabuleiro.adicionarObstaculo(new Rocha(), 3, 1);
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(3, 0));
        robo.darPoder();

        assertSorteia(robo);
    }

    @Test
    void roboInteligenteHerdaADecisao() {
        Robo inteligente = new RoboInteligente("verde");
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(0, 2));
        inteligente.darPoder();

        assertEquals("up", inteligente.decidirDirecao(tabuleiro, new Random(1)));
    }

    // Mesma semente tem que dar a mesma sequência de direcaoAleatoria.
    private void assertSorteia(Robo r) {
        Random esperado = new Random(7);
        Random usado = new Random(7);
        for (int i = 0; i < 20; i++) {
            assertEquals(r.direcaoAleatoria(esperado), r.decidirDirecao(tabuleiro, usado));
        }
    }
}
