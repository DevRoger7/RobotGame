package robotgame.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlacarPvpTest {

    @Test
    void livreNuncaFicaDecidido() {
        PlacarPvp placar = new PlacarPvp(PlacarPvp.Formato.LIVRE);
        for (int i = 0; i < 10; i++) {
            placar.registrarVitoria(0);
        }

        assertFalse(placar.isDecidida());
        assertEquals(10, placar.getVitorias(0));
    }

    @Test
    void melhorDe3TerminaCom2x0() {
        PlacarPvp placar = new PlacarPvp(PlacarPvp.Formato.MELHOR_DE_3);
        placar.registrarVitoria(0);
        assertFalse(placar.isDecidida());

        placar.registrarVitoria(0);

        assertTrue(placar.isDecidida());
    }

    @Test
    void melhorDe3Com1x1AindaNaoTerminou() {
        PlacarPvp placar = new PlacarPvp(PlacarPvp.Formato.MELHOR_DE_3);
        placar.registrarVitoria(0);
        placar.registrarVitoria(1);
        assertFalse(placar.isDecidida());

        placar.registrarVitoria(1);

        assertTrue(placar.isDecidida());
    }

    @Test
    void melhorDe5Precisa3Vitorias() {
        PlacarPvp placar = new PlacarPvp(PlacarPvp.Formato.MELHOR_DE_5);
        placar.registrarVitoria(0);
        placar.registrarVitoria(1);
        placar.registrarVitoria(0);
        placar.registrarVitoria(1);
        assertFalse(placar.isDecidida());

        placar.registrarVitoria(0);

        assertTrue(placar.isDecidida());
    }

    @Test
    void empateNaoContaParaASerie() {
        PlacarPvp placar = new PlacarPvp(PlacarPvp.Formato.MELHOR_DE_3);
        for (int i = 0; i < 3; i++) {
            placar.registrarEmpate();
        }

        assertFalse(placar.isDecidida());
        assertEquals(3, placar.getEmpates());
        assertEquals(4, placar.getPartidaAtual());
    }

    @Test
    void registrarDepoisDeDecididaLancaExcecao() {
        PlacarPvp placar = new PlacarPvp(PlacarPvp.Formato.MELHOR_DE_3);
        placar.registrarVitoria(1);
        placar.registrarVitoria(1);

        assertThrows(IllegalStateException.class, () -> placar.registrarVitoria(0));
        assertThrows(IllegalStateException.class, placar::registrarEmpate);
    }

    @Test
    void textoVitorias() {
        PlacarPvp md3 = new PlacarPvp(PlacarPvp.Formato.MELHOR_DE_3);
        md3.registrarVitoria(0);
        PlacarPvp md5 = new PlacarPvp(PlacarPvp.Formato.MELHOR_DE_5);
        md5.registrarVitoria(0);
        PlacarPvp livre = new PlacarPvp(PlacarPvp.Formato.LIVRE);
        livre.registrarVitoria(0);

        assertEquals("1/2", md3.textoVitorias(0));
        assertEquals("1/3", md5.textoVitorias(0));
        assertEquals("1", livre.textoVitorias(0));
    }
}
