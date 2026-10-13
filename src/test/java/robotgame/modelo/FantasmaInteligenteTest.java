package robotgame.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import robotgame.excecao.MovimentoInvalidoException;

class FantasmaInteligenteTest {

    private Tabuleiro tabuleiro;
    private Robo normal;
    private RoboInteligente inteligente;
    private RoboSuperInteligente superRobo;

    @BeforeEach
    void preparar() {
        tabuleiro = new Tabuleiro(1, 1);
        normal = new Robo("azul");
        inteligente = new RoboInteligente("verde");
        superRobo = new RoboSuperInteligente("rosa");
    }

    // Ordem do array propositalmente contra a ordem de inteligência.
    private Robo[] robos() {
        return new Robo[]{normal, inteligente, superRobo};
    }

    private static void posicionar(Robo robo, int x, int y) throws MovimentoInvalidoException {
        robo.setX(x);
        robo.setY(y);
    }

    @Test
    void perseguePrimeiroOSuperMesmoComONormalMaisPerto() throws MovimentoInvalidoException {
        posicionar(superRobo, 0, 3);
        posicionar(inteligente, 3, 1);
        posicionar(normal, 3, 2);

        assertEquals("left", new FantasmaInteligente(3, 3).decidirDirecao(tabuleiro, robos()));
    }

    @Test
    void superEliminadoPersegueOInteligente() throws MovimentoInvalidoException {
        posicionar(superRobo, 0, 3);
        posicionar(inteligente, 3, 0);
        posicionar(normal, 2, 3);
        superRobo.explodir();

        assertEquals("down", new FantasmaInteligente(3, 3).decidirDirecao(tabuleiro, robos()));
    }

    @Test
    void superCercadoPorRochasPassaParaOInteligente() throws MovimentoInvalidoException {
        tabuleiro.adicionarObstaculo(new Rocha(), 0, 2);
        tabuleiro.adicionarObstaculo(new Rocha(), 1, 3);
        posicionar(superRobo, 0, 3);
        posicionar(inteligente, 3, 1);
        posicionar(normal, 2, 2);

        assertEquals("down", new FantasmaInteligente(3, 3).decidirDirecao(tabuleiro, robos()));
    }

    @Test
    void perseguePrimeiroOSuperMesmoComPoder() throws MovimentoInvalidoException {
        posicionar(superRobo, 0, 3);
        posicionar(inteligente, 3, 1);
        posicionar(normal, 3, 2);
        superRobo.darPoder();

        assertEquals("left", new FantasmaInteligente(3, 3).decidirDirecao(tabuleiro, robos()));
    }

    @Test
    void desviaDeRocha() throws MovimentoInvalidoException {
        tabuleiro.adicionarObstaculo(new Rocha(), 3, 2);
        posicionar(superRobo, 3, 1);
        posicionar(inteligente, 3, 1);
        posicionar(normal, 3, 1);

        assertEquals("left", new FantasmaInteligente(3, 3).decidirDirecao(tabuleiro, robos()));
    }

    @Test
    void ninguemAlcancavelFicaParado() throws MovimentoInvalidoException {
        tabuleiro.adicionarObstaculo(new Rocha(), 0, 2);
        tabuleiro.adicionarObstaculo(new Rocha(), 1, 3);
        posicionar(superRobo, 0, 3);
        posicionar(inteligente, 0, 3);
        posicionar(normal, 0, 3);

        assertNull(new FantasmaInteligente(3, 3).decidirDirecao(tabuleiro, robos()));
    }

    @Test
    void todosEliminadosFicaParado() {
        normal.explodir();
        inteligente.explodir();
        superRobo.explodir();

        assertNull(new FantasmaInteligente(3, 3).decidirDirecao(tabuleiro, robos()));
    }

    @Test
    void moverAplicaADirecaoEIgnoraNull() {
        FantasmaInteligente fantasma = new FantasmaInteligente(2, 2);

        fantasma.mover("up");
        assertEquals(2, fantasma.getX());
        assertEquals(3, fantasma.getY());

        fantasma.mover("left");
        assertEquals(1, fantasma.getX());

        fantasma.mover("down");
        fantasma.mover("down");
        assertEquals(1, fantasma.getY());

        fantasma.mover("right");
        assertEquals(2, fantasma.getX());

        fantasma.mover(null);
        assertEquals(2, fantasma.getX());
        assertEquals(1, fantasma.getY());
    }
}
