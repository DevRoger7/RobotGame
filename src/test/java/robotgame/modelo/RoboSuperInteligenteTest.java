package robotgame.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import robotgame.excecao.MovimentoInvalidoException;

class RoboSuperInteligenteTest {

    private Tabuleiro tabuleiro;
    private RoboSuperInteligente robo;

    @BeforeEach
    void criarCenario() {
        tabuleiro = new Tabuleiro(3, 3);
        robo = new RoboSuperInteligente("roxo");
    }

    @Test
    void semPoderVaiParaAFruta() throws MovimentoInvalidoException {
        robo.setX(1);
        robo.setY(3);

        assertEquals("right", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void semPoderNoEmpateSegueAOrdemFixa() {
        assertEquals("up", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void bolinhaNaCasaVizinhaNaoDesviaDaFruta() throws MovimentoInvalidoException {
        robo.setX(3);
        robo.setY(0);
        tabuleiro.setItemPoder(new ItemPoder(2, 0));

        assertEquals("up", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void comPoderEFantasmaVivoPersegueOFantasma() {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(2, 0));
        robo.darPoder();

        assertEquals("right", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void comPoderESemFantasmaVaiParaAFruta() throws MovimentoInvalidoException {
        robo.setX(3);
        robo.setY(0);
        robo.darPoder();

        assertEquals("up", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void semPoderIgnoraOFantasmaEVaiParaAFruta() throws MovimentoInvalidoException {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(0, 2));
        robo.setX(3);
        robo.setY(0);

        assertEquals("up", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void desviaDeRochaNoCaminhoDaFruta() throws MovimentoInvalidoException {
        tabuleiro.adicionarObstaculo(new Rocha(), 3, 2);
        robo.setX(3);
        robo.setY(1);

        assertEquals("left", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void desviaDeFantasmaBurroNoCaminhoDaFruta() throws MovimentoInvalidoException {
        tabuleiro.adicionarObstaculo(new Fantasma("vermelho"), 3, 2);
        robo.setX(3);
        robo.setY(1);

        assertEquals("left", robo.decidirDirecao(tabuleiro, new Random(1)));
    }

    @Test
    void frutaCercadaPorFantasmasBurrosSorteia() {
        tabuleiro.adicionarObstaculo(new Fantasma("vermelho"), 2, 3);
        tabuleiro.adicionarObstaculo(new Fantasma("azul"), 3, 2);

        Random esperado = new Random(7);
        Random usado = new Random(7);
        for (int i = 0; i < 20; i++) {
            assertEquals(robo.direcaoAleatoria(esperado), robo.decidirDirecao(tabuleiro, usado));
        }
    }
}
