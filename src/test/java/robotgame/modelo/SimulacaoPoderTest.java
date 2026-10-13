package robotgame.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import robotgame.excecao.MovimentoInvalidoException;

class SimulacaoPoderTest {

    // Direção inválida: o pacman fica onde está (e, se tiver poder, gasta o turno).
    private static final String PARADO = "parado";

    // Segue um roteiro fixo de direções; quando acaba, fica parado.
    private static class RoboRoteirizado extends Robo {

        private final Deque<String> direcoes = new ArrayDeque<>();

        RoboRoteirizado(String cor) {
            super(cor);
        }

        void roteiro(String... novas) {
            direcoes.addAll(List.of(novas));
        }

        @Override
        public String decidirDirecao(Tabuleiro t, Random random) {
            return direcoes.isEmpty() ? PARADO : direcoes.poll();
        }
    }

    private Tabuleiro tabuleiro;
    private RoboRoteirizado azul;
    private RoboRoteirizado verde;
    private RoboRoteirizado rosa;
    private SimulacaoPoder simulacao;
    private List<EventoPartida> eventos;

    @BeforeEach
    void criarCenario() {
        tabuleiro = new Tabuleiro(3, 0);
        azul = new RoboRoteirizado("azul");
        verde = new RoboRoteirizado("verde");
        rosa = new RoboRoteirizado("rosa");
    }

    // Chamado depois de montar o cenário (tabuleiro, posições, roteiros).
    private void iniciar() throws ReflectiveOperationException {
        simulacao = new SimulacaoPoder(tabuleiro, azul, verde, rosa);
        eventos = new ArrayList<>();
        simulacao.setOuvinte(eventos::add);
        // A semente fixa vale para o gerador de bolinhas, que usa o mesmo Random da simulação.
        Field campo = SimulacaoPoder.class.getDeclaredField("random");
        campo.setAccessible(true);
        ((Random) campo.get(simulacao)).setSeed(42);
    }

    private static void posicionar(Robo robo, int x, int y) throws MovimentoInvalidoException {
        robo.setX(x);
        robo.setY(y);
    }

    // Executa passos até o turno de 'ator' (null = fantasma) e devolve esse passo.
    private Simulacao.Passo turnoDe(Robo ator) {
        for (int i = 0; i < 20; i++) {
            Simulacao.Passo passo = simulacao.proximoPasso();
            if (passo.robo() == ator) {
                return passo;
            }
        }
        throw new AssertionError("O turno esperado não aconteceu.");
    }

    private EventoPartida primeiroEvento(EventoPartida.Tipo tipo) {
        return eventos.stream().filter(e -> e.tipo() == tipo).findFirst().orElse(null);
    }

    private long contar(EventoPartida.Tipo tipo) {
        return eventos.stream().filter(e -> e.tipo() == tipo).count();
    }

    private List<EventoPartida.Tipo> tipos() {
        return eventos.stream().map(EventoPartida::tipo).toList();
    }

    @Test
    void semPoderEntrarNoFantasmaEspertoEEliminado() throws Exception {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(1, 0));
        azul.roteiro("right");
        iniciar();

        Simulacao.Passo passo = simulacao.proximoPasso();

        assertSame(azul, passo.robo());
        assertEquals("right", passo.direcao());
        assertTrue(passo.mensagem().contains("eliminado"));
        assertFalse(azul.isAtivo());
        assertNotNull(tabuleiro.getFantasmaInteligente());
        assertEquals(List.of(EventoPartida.Tipo.ELIMINADO_PELO_FANTASMA), tipos());
        EventoPartida evento = eventos.get(0);
        assertSame(azul, evento.robo());
        assertEquals(1, evento.x());
        assertEquals(0, evento.y());
        assertFalse(simulacao.isTerminada());
    }

    @Test
    void comPoderEntrarNoFantasmaEspertoComeEPerdeOPoder() throws Exception {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(1, 0));
        azul.darPoder();
        azul.roteiro("right");
        iniciar();

        Simulacao.Passo passo = simulacao.proximoPasso();

        assertSame(azul, passo.robo());
        assertTrue(azul.isAtivo());
        assertFalse(azul.isComPoder());
        assertNull(tabuleiro.getFantasmaInteligente());
        assertEquals(List.of(EventoPartida.Tipo.COMEU_FANTASMA), tipos());
        assertSame(azul, eventos.get(0).robo());
        assertEquals(1, eventos.get(0).x());
        assertEquals(0, eventos.get(0).y());
    }

    @Test
    void comPoderEntrarNoFantasmaBurroComeEPerdeOPoder() throws Exception {
        Fantasma burro = new Fantasma("vermelho");
        tabuleiro.adicionarObstaculo(burro, 1, 0);
        azul.darPoder();
        azul.roteiro("right");
        iniciar();

        simulacao.proximoPasso();

        assertTrue(azul.isAtivo());
        assertFalse(azul.isComPoder());
        assertNull(tabuleiro.getObstaculo(1, 0));
        assertEquals(List.of(EventoPartida.Tipo.COMEU_FANTASMA_BURRO), tipos());
        assertSame(burro, eventos.get(0).obstaculo());
        assertSame(azul, eventos.get(0).robo());
    }

    @Test
    void semPoderEntrarNoFantasmaBurroExplode() throws Exception {
        Fantasma burro = new Fantasma("vermelho");
        tabuleiro.adicionarObstaculo(burro, 1, 0);
        azul.roteiro("right");
        iniciar();

        Simulacao.Passo passo = simulacao.proximoPasso();

        assertTrue(passo.mensagem().contains("eliminado"));
        assertFalse(azul.isAtivo());
        assertNull(tabuleiro.getObstaculo(1, 0));
        assertEquals(List.of(EventoPartida.Tipo.EXPLODIU, EventoPartida.Tipo.FANTASMA_SUMIU), tipos());
        assertSame(burro, eventos.get(0).obstaculo());
        assertEquals(1, eventos.get(1).x());
        assertEquals(0, eventos.get(1).y());
    }

    @Test
    void pegarABolinhaDaPoderEARemoveDoTabuleiro() throws Exception {
        tabuleiro.setItemPoder(new ItemPoder(1, 0));
        azul.roteiro("right");
        iniciar();

        simulacao.proximoPasso();

        assertTrue(azul.isComPoder());
        assertNull(tabuleiro.getItemPoder());
        assertEquals(List.of(EventoPartida.Tipo.PEGOU_PODER), tipos());
        assertSame(azul, eventos.get(0).robo());
        assertEquals(1, eventos.get(0).x());
        assertEquals(0, eventos.get(0).y());
    }

    @Test
    void poderAcabaAposOitoTurnos() throws Exception {
        tabuleiro.setItemPoder(new ItemPoder(0, 1));
        azul.roteiro("up");
        iniciar();

        turnoDe(azul);
        assertTrue(azul.isComPoder());
        assertEquals(1, contar(EventoPartida.Tipo.PEGOU_PODER));

        // A partir daqui o azul fica parado em (0,1): cada turno dele gasta um turno de poder.
        for (int turno = 1; turno < Poder.DURACAO; turno++) {
            turnoDe(azul);
            assertTrue(azul.isComPoder(), "O poder acabou cedo demais, no turno " + turno);
            assertEquals(0, contar(EventoPartida.Tipo.PODER_ACABOU));
        }

        Simulacao.Passo ultimo = turnoDe(azul);

        assertFalse(azul.isComPoder());
        assertEquals(1, contar(EventoPartida.Tipo.PODER_ACABOU));
        assertSame(azul, primeiroEvento(EventoPartida.Tipo.PODER_ACABOU).robo());
        assertTrue(ultimo.mensagem().contains("poder"));
        assertTrue(azul.isAtivo());
    }

    @Test
    void fantasmaEmCimaDaFrutaPegaQuemChega() throws Exception {
        tabuleiro = new Tabuleiro(1, 0);
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(1, 1));
        azul.roteiro(PARADO, "right");
        iniciar();

        // Rodada 1: o fantasma desce e para em cima da fruta (1,0).
        turnoDe(null);
        EventoPartida fantasmaMoveu = primeiroEvento(EventoPartida.Tipo.FANTASMA_MOVEU);
        assertNotNull(fantasmaMoveu);
        assertEquals(1, fantasmaMoveu.x());
        assertEquals(0, fantasmaMoveu.y());

        // Rodada 2: o azul chega na fruta e o fantasma pega antes de ele ganhar.
        Simulacao.Passo passo = turnoDe(azul);

        assertEquals("right", passo.direcao());
        assertFalse(azul.isAtivo());
        assertEquals(1, contar(EventoPartida.Tipo.ELIMINADO_PELO_FANTASMA));
        assertEquals(0, contar(EventoPartida.Tipo.ACHOU_ALIMENTO));
        assertFalse(simulacao.isTerminada());
    }

    @Test
    void fantasmaAndaParaCimaDePacmanSemPoderEEliminaEle() throws Exception {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(2, 0));
        posicionar(azul, 1, 0);
        iniciar();

        Simulacao.Passo passo = turnoDe(null);

        assertNull(passo.robo());
        assertEquals("left", passo.direcao());
        assertFalse(azul.isAtivo());
        assertTrue(verde.isAtivo());
        assertTrue(rosa.isAtivo());
        assertNotNull(tabuleiro.getFantasmaInteligente());
        assertEquals(EventoPartida.Tipo.FANTASMA_MOVEU, tipos().get(0));
        EventoPartida moveu = eventos.get(0);
        assertNull(moveu.robo());
        assertEquals(1, moveu.x());
        assertEquals(0, moveu.y());
        assertEquals("left", moveu.direcao());
        assertEquals(EventoPartida.Tipo.ELIMINADO_PELO_FANTASMA, tipos().get(1));
        assertSame(azul, eventos.get(1).robo());
    }

    @Test
    void fantasmaAndaParaCimaDePacmanComPoderEOFantasmaECome() throws Exception {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(2, 0));
        posicionar(azul, 1, 0);
        azul.darPoder();
        iniciar();

        turnoDe(null);

        assertTrue(azul.isAtivo());
        assertFalse(azul.isComPoder());
        assertNull(tabuleiro.getFantasmaInteligente());
        assertEquals(0, contar(EventoPartida.Tipo.ELIMINADO_PELO_FANTASMA));
        assertEquals(1, contar(EventoPartida.Tipo.COMEU_FANTASMA));
        assertSame(azul, primeiroEvento(EventoPartida.Tipo.COMEU_FANTASMA).robo());
    }

    @Test
    void tresPacmansEliminadosTerminamAPartida() throws Exception {
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(1, 0));
        azul.roteiro("right");
        verde.roteiro("right");
        rosa.roteiro("right");
        iniciar();

        simulacao.proximoPasso();
        assertFalse(simulacao.isTerminada());
        simulacao.proximoPasso();
        assertFalse(simulacao.isTerminada());
        simulacao.proximoPasso();

        assertTrue(simulacao.isTerminada());
        assertEquals("Os três pacmans foram eliminados!", simulacao.getResultado());
        assertEquals(3, contar(EventoPartida.Tipo.ELIMINADO_PELO_FANTASMA));
        assertEquals(List.of(azul, verde, rosa), eventos.stream().map(EventoPartida::robo).toList());
        assertThrows(IllegalStateException.class, simulacao::proximoPasso);
    }

    @Test
    void pacmanQueAchaAFrutaVence() throws Exception {
        tabuleiro = new Tabuleiro(1, 0);
        azul.roteiro("right");
        iniciar();

        simulacao.proximoPasso();

        assertTrue(simulacao.isTerminada());
        assertEquals("Pacman azul encontrou o alimento e venceu!", simulacao.getResultado());
        assertEquals(List.of(EventoPartida.Tipo.ACHOU_ALIMENTO), tipos());
    }

    @Test
    void turnoPulaPacmansEliminados() throws Exception {
        tabuleiro.adicionarObstaculo(new Fantasma("vermelho"), 1, 0);
        tabuleiro.setFantasmaInteligente(new FantasmaInteligente(3, 3));
        azul.roteiro("right");
        iniciar();

        List<Robo> atores = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            atores.add(simulacao.proximoPasso().robo());
        }

        assertFalse(azul.isAtivo());
        assertEquals(Arrays.asList(azul, verde, rosa, null, verde, rosa, null), atores);
    }
}
