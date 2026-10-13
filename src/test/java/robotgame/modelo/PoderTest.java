package robotgame.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PoderTest {

    @Test
    void poderComecaComADuracaoCompleta() {
        Poder poder = new Poder();

        assertEquals(Poder.DURACAO, poder.getTurnosRestantes());
        assertFalse(poder.acabou());
    }

    @Test
    void poderNuncaFicaAbaixoDeZero() {
        Poder poder = new Poder();
        for (int i = 0; i < Poder.DURACAO + 3; i++) {
            poder.gastarTurno();
        }

        assertEquals(0, poder.getTurnosRestantes());
        assertTrue(poder.acabou());
    }

    @Test
    void roboSemPoderNaoTemPoder() {
        Robo robo = new Robo("amarelo");

        assertFalse(robo.isComPoder());
        robo.gastarTurnoDePoder();
        assertFalse(robo.isComPoder());
    }

    @Test
    void poderDuraExatamenteOitoChamadas() {
        Robo robo = new Robo("amarelo");
        robo.darPoder();

        for (int i = 1; i < Poder.DURACAO; i++) {
            robo.gastarTurnoDePoder();
            assertTrue(robo.isComPoder(), "ainda deveria ter poder após " + i + " turnos");
        }
        robo.gastarTurnoDePoder();

        assertFalse(robo.isComPoder());
    }

    @Test
    void darPoderComPoderAtivoRenovaADuracao() {
        Robo robo = new Robo("amarelo");
        robo.darPoder();
        for (int i = 0; i < 5; i++) {
            robo.gastarTurnoDePoder();
        }

        robo.darPoder();

        for (int i = 1; i < Poder.DURACAO; i++) {
            robo.gastarTurnoDePoder();
            assertTrue(robo.isComPoder());
        }
        robo.gastarTurnoDePoder();
        assertFalse(robo.isComPoder());
    }

    @Test
    void perderPoderRemoveOPoder() {
        Robo robo = new Robo("amarelo");
        robo.darPoder();

        robo.perderPoder();

        assertFalse(robo.isComPoder());
    }

    @Test
    void poderNaoAfetaOsMovimentos() throws Exception {
        Robo robo = new Robo("amarelo");
        robo.darPoder();
        robo.mover("up");

        assertEquals(1, robo.getY());
        assertEquals(1, robo.getMovimentosValidos());
        assertTrue(robo.isComPoder());
    }
}
