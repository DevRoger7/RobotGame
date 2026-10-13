package robotgame.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import robotgame.excecao.MovimentoInvalidoException;

class GeradorPoderTest {

    private Tabuleiro tabuleiro;
    private Robo[] robos;
    private GeradorPoder gerador;

    @BeforeEach
    void criarCenario() {
        tabuleiro = new Tabuleiro(3, 3);
        robos = new Robo[]{new Robo("amarelo"), new RoboInteligente("verde")};
        gerador = new GeradorPoder(new Random(42));
    }

    @Test
    void itemPoderComecaComAVidaCompletaEExpiraEmZero() {
        ItemPoder item = new ItemPoder(1, 1);
        assertEquals(ItemPoder.VIDA, item.getRodadasRestantes());

        for (int i = 1; i < ItemPoder.VIDA; i++) {
            item.passarRodada();
            assertFalse(item.expirou());
        }
        item.passarRodada();

        assertTrue(item.expirou());
        assertEquals(0, item.getRodadasRestantes());
    }

    @Test
    void bolinhaApareceSoDepoisDeCincoRodadas() {
        for (int i = 1; i < GeradorPoder.ESPERA_INICIAL; i++) {
            assertTrue(gerador.fimDaRodada(tabuleiro, robos).isEmpty());
            assertNull(tabuleiro.getItemPoder());
        }

        List<EventoPartida> eventos = gerador.fimDaRodada(tabuleiro, robos);

        assertEquals(1, eventos.size());
        EventoPartida apareceu = eventos.get(0);
        assertEquals(EventoPartida.Tipo.PODER_APARECEU, apareceu.tipo());
        assertNull(apareceu.robo());
        ItemPoder item = tabuleiro.getItemPoder();
        assertNotNull(item);
        assertEquals(item.getX(), apareceu.x());
        assertEquals(item.getY(), apareceu.y());
    }

    @Test
    void bolinhaSomeDepoisDeOitoRodadasEReapareceEmOutraCasa() {
        rodarAteAparecer();
        ItemPoder primeira = tabuleiro.getItemPoder();

        for (int i = 1; i < ItemPoder.VIDA; i++) {
            assertTrue(gerador.fimDaRodada(tabuleiro, robos).isEmpty());
            assertEquals(primeira, tabuleiro.getItemPoder());
        }
        List<EventoPartida> eventos = gerador.fimDaRodada(tabuleiro, robos);

        assertEquals(2, eventos.size());
        assertEquals(EventoPartida.Tipo.PODER_SUMIU, eventos.get(0).tipo());
        assertEquals(primeira.getX(), eventos.get(0).x());
        assertEquals(primeira.getY(), eventos.get(0).y());
        assertEquals(EventoPartida.Tipo.PODER_APARECEU, eventos.get(1).tipo());

        ItemPoder segunda = tabuleiro.getItemPoder();
        assertNotNull(segunda);
        assertFalse(segunda.getX() == primeira.getX() && segunda.getY() == primeira.getY());
        assertEquals(ItemPoder.VIDA, segunda.getRodadasRestantes());
    }

    @Test
    void reapareceSempreEmCasaDiferenteDaAnterior() {
        rodarAteAparecer();
        for (int i = 0; i < 30; i++) {
            ItemPoder anterior = tabuleiro.getItemPoder();
            for (int r = 0; r < ItemPoder.VIDA; r++) {
                gerador.fimDaRodada(tabuleiro, robos);
            }
            ItemPoder nova = tabuleiro.getItemPoder();
            assertNotEquals(anterior.getX() * 10 + anterior.getY(), nova.getX() * 10 + nova.getY());
        }
    }

    @Test
    void depoisDeComidaEsperaCincoRodadas() {
        rodarAteAparecer();
        tabuleiro.removerItemPoder();
        gerador.itemFoiComido();

        for (int i = 1; i < GeradorPoder.ESPERA_APOS_COMIDO; i++) {
            assertTrue(gerador.fimDaRodada(tabuleiro, robos).isEmpty());
            assertNull(tabuleiro.getItemPoder());
        }
        List<EventoPartida> eventos = gerador.fimDaRodada(tabuleiro, robos);

        assertEquals(1, eventos.size());
        assertEquals(EventoPartida.Tipo.PODER_APARECEU, eventos.get(0).tipo());
        assertNotNull(tabuleiro.getItemPoder());
    }

    @Test
    void nuncaNasceEmObstaculoFrutaFantasmaOuPacman() throws MovimentoInvalidoException {
        tabuleiro.adicionarObstaculo(new Rocha(), 1, 1);
        tabuleiro.adicionarObstaculo(new Fantasma("vermelho"), 2, 1);
        tabuleiro.adicionarObstaculo(new Bomba(), 1, 2);
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(0, 3));
        robos[1].setX(2);
        robos[1].setY(0);

        for (long semente = 0; semente < 50; semente++) {
            GeradorPoder g = new GeradorPoder(new Random(semente));
            tabuleiro.removerItemPoder();
            for (int rodada = 0; rodada < 60; rodada++) {
                for (EventoPartida e : g.fimDaRodada(tabuleiro, robos)) {
                    if (e.tipo() == EventoPartida.Tipo.PODER_APARECEU) {
                        assertCasaValida(e.x(), e.y());
                    }
                }
                if (rodada % 7 == 6 && tabuleiro.getItemPoder() != null) {
                    tabuleiro.removerItemPoder();
                    g.itemFoiComido();
                }
            }
        }
    }

    @Test
    void nuncaNasceEmCasaInalcancavel() {
        tabuleiro.adicionarObstaculo(new Fantasma("vermelho"), 2, 0);
        tabuleiro.adicionarObstaculo(new Fantasma("azul"), 3, 1);

        for (long semente = 0; semente < 50; semente++) {
            GeradorPoder g = new GeradorPoder(new Random(semente));
            tabuleiro.removerItemPoder();
            for (int rodada = 0; rodada < 40; rodada++) {
                g.fimDaRodada(tabuleiro, robos);
                ItemPoder item = tabuleiro.getItemPoder();
                if (item != null) {
                    assertFalse(item.getX() == 3 && item.getY() == 0, "nasceu em (3,0), que é inalcançável");
                }
            }
        }
    }

    @Test
    void pacmanInativoNaoContaComoOcupadoNemComoOrigem() {
        robos[0].explodir();
        robos[1].explodir();

        for (int i = 0; i < 20; i++) {
            assertTrue(gerador.fimDaRodada(tabuleiro, robos).isEmpty());
        }
        assertNull(tabuleiro.getItemPoder());
    }

    @Test
    void semCasaLivreNaoCriaETentaNaRodadaSeguinte() {
        for (int x = 0; x < Tabuleiro.TAMANHO; x++) {
            for (int y = 0; y < Tabuleiro.TAMANHO; y++) {
                if ((x == 0 && y == 0) || (x == 3 && y == 3)) {
                    continue;
                }
                tabuleiro.adicionarObstaculo(new Fantasma("vermelho"), x, y);
            }
        }

        for (int i = 0; i < GeradorPoder.ESPERA_INICIAL + 3; i++) {
            assertTrue(gerador.fimDaRodada(tabuleiro, robos).isEmpty());
        }
        assertNull(tabuleiro.getItemPoder());

        tabuleiro.removerObstaculo(1, 0);
        List<EventoPartida> eventos = gerador.fimDaRodada(tabuleiro, robos);

        assertEquals(1, eventos.size());
        assertEquals(1, tabuleiro.getItemPoder().getX());
        assertEquals(0, tabuleiro.getItemPoder().getY());
    }

    @Test
    void nuncaHaDuasBolinhasAoMesmoTempo() {
        for (int rodada = 0; rodada < 200; rodada++) {
            boolean tinhaBolinha = tabuleiro.getItemPoder() != null;
            List<EventoPartida> eventos = gerador.fimDaRodada(tabuleiro, robos);
            long apareceu = eventos.stream().filter(e -> e.tipo() == EventoPartida.Tipo.PODER_APARECEU).count();
            long sumiu = eventos.stream().filter(e -> e.tipo() == EventoPartida.Tipo.PODER_SUMIU).count();

            assertTrue(apareceu <= 1);
            if (tinhaBolinha && apareceu == 1) {
                assertEquals(1, sumiu, "apareceu outra bolinha sem a anterior sumir");
            }
        }
    }

    private void rodarAteAparecer() {
        for (int i = 0; i < GeradorPoder.ESPERA_INICIAL; i++) {
            gerador.fimDaRodada(tabuleiro, robos);
        }
        assertNotNull(tabuleiro.getItemPoder());
    }

    private void assertCasaValida(int x, int y) {
        assertNull(tabuleiro.getObstaculo(x, y), "nasceu em obstáculo em (" + x + "," + y + ")");
        assertFalse(x == tabuleiro.getXAlimento() && y == tabuleiro.getYAlimento(), "nasceu na fruta");
        FantasmaInteligente f = tabuleiro.getFantasmaInteligente();
        assertFalse(x == f.getX() && y == f.getY(), "nasceu no fantasma inteligente");
        for (Robo r : robos) {
            assertFalse(x == r.getX() && y == r.getY(), "nasceu em cima de um pacman");
        }
    }
}
