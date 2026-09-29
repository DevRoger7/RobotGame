package robotgame.ui.screens;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import robotgame.excecao.MovimentoInvalidoException;
import robotgame.modelo.EventoPartida;
import robotgame.modelo.ModoJogo;
import robotgame.modelo.Robo;
import robotgame.modelo.RoboInteligente;
import robotgame.modelo.Simulacao;
import robotgame.modelo.Tabuleiro;
import robotgame.ui.Sons;
import robotgame.ui.components.BoardView;
import robotgame.ui.components.ControlsPanel;
import robotgame.ui.components.DPad;
import robotgame.ui.components.DiaryList;
import robotgame.ui.components.KeyCap;
import robotgame.ui.components.LegendGrid;
import robotgame.ui.components.MessageBar;
import robotgame.ui.components.NeonCard;
import robotgame.ui.components.ScoreCard;
import robotgame.ui.components.Sprites;
import robotgame.ui.components.StepperBar;
import robotgame.ui.components.Ui;
import robotgame.ui.partida.ConfiguracaoPartida;
import robotgame.ui.partida.CoresPacman;
import robotgame.ui.partida.DiarioPartida;
import robotgame.ui.partida.ResultadoPartida;
import robotgame.ui.theme.Tema;

/**
 * Tela de jogo. Modos 2, 3 e 4: simulação automática com placar, diário, legenda, controles e velocidade.
 * Modo 1 (variante manual): o jogador move com as setas ou com o D-pad.
 *
 * <p>A condução da partida (início com música, liberação, passos, morte, pausa, fim) é a mesma da
 * interface anterior; a tela só observa os eventos para desenhar.
 */
public class GameScreen implements Tela {

    // Intervalo "Normal" da interface anterior; o slider de velocidade muda só o ritmo da Timeline.
    private static final Duration INTERVALO_SIMULACAO = Duration.millis(700);
    private static final double[] INTERVALOS_MS = {1400, 1000, 700, 450, 250};
    private static final Duration LIMITE_MUSICA_INICIO = Duration.seconds(6);

    private final Navegador navegador;
    private final ConfiguracaoPartida config;
    private final ModoJogo modo;
    private final Sons sons;
    private final Tabuleiro tabuleiro;
    private final Robo[] robos;
    private final DiarioPartida diario;

    private final BoardView board = new BoardView(136, false);
    private final MessageBar mensagem = new MessageBar(592);
    private final ScoreCard[] placares;
    private final DiaryList diaryList;
    private final ControlsPanel controles;
    private final DPad dpad;
    private final Label startLabel = Ui.label("START", 44, "pixel", "amarelo");
    private final Label pausaLabel = Ui.label("PAUSADO", 30, "pixel");
    private final VBox raiz;

    private Simulacao simulacao;
    private Timeline animacaoBoca;
    private Timeline animacaoSimulacao;
    private FadeTransition animacaoStart;
    private PauseTransition limiteMusicaInicio;
    private PauseTransition esperaFim;
    private boolean bocaAberta = true;
    private boolean congelado;
    private boolean pausado;
    private boolean terminada;
    private boolean saiu;
    private boolean indoParaFim;
    private double intervaloMs = INTERVALO_SIMULACAO.toMillis();

    public GameScreen(Navegador navegador, ConfiguracaoPartida config) {
        this.navegador = navegador;
        this.config = config;
        this.modo = config.getModo();
        this.sons = navegador.getSons();
        this.tabuleiro = config.criarTabuleiro();
        this.robos = config.criarRobos();
        this.diario = new DiarioPartida(robos, modo.isManual());
        for (Robo r : robos) {
            r.setOuvinteMovimentoInvalido(this::aoEvento);
        }

        board.setFruta(tabuleiro.getXAlimento(), tabuleiro.getYAlimento(), Sprites.fruta(config.getIndiceFruta(), 80));
        board.carregarObstaculos(tabuleiro);
        board.setRobos(robos);
        montarSobreposicoes();

        controles = new ControlsPanel(!modo.isManual(), this::alternarPausa, this::alternarSom,
                () -> navegador.irParaMenu(config));
        controles.aoMudarVelocidade((obs, antes, agora) -> mudarVelocidade(agora.intValue()));

        VBox painel;
        if (modo.isManual()) {
            placares = new ScoreCard[]{new ScoreCard(cor(robos[0]), "PACMAN 1", "você controla", 124,
                    "POSIÇÃO", "MOVIMENTOS", "INVÁLIDOS")};
            dpad = new DPad(this::mover);
            diaryList = new DiaryList(diario.getEntradas(), 3, e -> true);
            painel = LayoutJogo.painel(12, placares[0], comoMover(), cartaoDiario(), legenda(), controles);
        } else {
            placares = new ScoreCard[robos.length];
            for (int i = 0; i < robos.length; i++) {
                placares[i] = new ScoreCard(cor(robos[i]), "PACMAN " + (i + 1), tipo(robos[i]), 132,
                        "POSIÇÃO", "VÁLIDOS", "INVÁLIDOS");
            }
            dpad = null;
            diaryList = new DiaryList(diario.getEntradas(), 6, e -> true);
            painel = LayoutJogo.painel(12, Ui.linhaIgual(12, placares), cartaoDiario(), legenda(), controles);
        }
        raiz = LayoutJogo.montar(modo, StepperBar.Passo.JOGO, board, mensagem, painel);
        atualizarPainel();
        atualizarBotoes();
    }

    @Override
    public Parent getRaiz() {
        return raiz;
    }

    @Override
    public void aoMostrar() {
        comecarPartida();
    }

    // ------------------------------------------------------------------ montagem

    private void montarSobreposicoes() {
        startLabel.setStyle(startLabel.getStyle()
                + "-fx-effect: dropshadow(gaussian, black, 14, 0.7, 0, 0);");
        startLabel.setVisible(false);
        startLabel.setMouseTransparent(true);
        pausaLabel.setStyle(pausaLabel.getStyle() + "-fx-background-color: rgba(5,5,20,0.85);"
                + "-fx-padding: 18 28 16 28; -fx-background-radius: 12; -fx-border-color: #ffa63d;"
                + "-fx-border-width: 3; -fx-border-radius: 12; -fx-text-fill: #ffa63d;");
        pausaLabel.setVisible(false);
        pausaLabel.setMouseTransparent(true);
        board.getSobreposicao().getChildren().addAll(startLabel, pausaLabel);
    }

    private NeonCard cartaoDiario() {
        NeonCard cartao = new NeonCard("DIÁRIO DA PARTIDA", diaryList);
        LayoutJogo.crescer(cartao);
        LayoutJogo.recortar(cartao);
        return cartao;
    }

    private NeonCard comoMover() {
        HBox teclas = Ui.linha(4, Ui.label("Use as setas do teclado", 14, "semi"),
                new KeyCap("←", true), new KeyCap("↑", true), new KeyCap("↓", true), new KeyCap("→", true));
        Label aviso = Ui.label("Sair da área do tabuleiro conta como movimento inválido.", 14, "muted");
        aviso.setWrapText(true);
        VBox textos = new VBox(6, teclas, Ui.label("ou os botões ao lado.", 14, "semi"), aviso);
        textos.setAlignment(Pos.CENTER_LEFT);
        HBox linha = new HBox(20, dpad, textos);
        linha.setAlignment(Pos.CENTER_LEFT);
        NeonCard cartao = new NeonCard("COMO MOVER", linha);
        Ui.tamanhoFixo(cartao, LayoutJogo.LARGURA_PAINEL, 164);
        cartao.setSpacing(8);
        return cartao;
    }

    private NeonCard legenda() {
        int itens = (modo.isManual() ? 1 : robos.length) + 2 + (modo.isComObstaculos() ? 2 : 0);
        LegendGrid grade = new LegendGrid(LegendGrid.colunasPara(itens));
        if (modo.isManual()) {
            grade.item(Sprites.pacman(cor(robos[0]), 22), "Pacman 1", "você");
        } else {
            for (int i = 0; i < robos.length; i++) {
                grade.item(Sprites.pacman(cor(robos[i]), 22), "Pacman " + (i + 1), tipo(robos[i]));
            }
        }
        grade.item(Sprites.view(Sprites.fruta(config.getIndiceFruta(), 22), 22), "Fruta", "objetivo");
        if (modo.isComObstaculos()) {
            grade.item(Sprites.view(Sprites.fantasma("azul", 22), 22), "Fantasma", "elimina");
            grade.item(Sprites.view(Sprites.rocha(22), 22), "Rocha", "volta 1 casa");
        }
        grade.item(LayoutJogo.iconeProibido(), "Proibido", "inválido");
        return new NeonCard("LEGENDA", grade);
    }

    // ------------------------------------------------------------------ início

    private void comecarPartida() {
        congelado = true;
        mensagem.mostrar("STATUS", "Prepare-se...", Tema.CIANO);
        sons.tocarInicio(this::liberarPartida);
        limiteMusicaInicio = new PauseTransition(LIMITE_MUSICA_INICIO);
        limiteMusicaInicio.setOnFinished(e -> liberarPartida());
        limiteMusicaInicio.play();
        atualizarBotoes();
    }

    private void liberarPartida() {
        if (!congelado || saiu) {
            return;
        }
        congelado = false;
        limiteMusicaInicio.stop();
        sons.pararInicio();
        mostrarStart();
        iniciarAnimacaoBoca();
        sons.iniciarWakawaka();
        diario.registrarInicio(tabuleiro.getXAlimento(), tabuleiro.getYAlimento());
        if (modo.isManual()) {
            mostrarDica();
        } else {
            iniciarSimulacao();
            mostrarUltimo();
        }
        atualizarBotoes();
    }

    private void mostrarStart() {
        startLabel.setOpacity(1);
        startLabel.setVisible(true);
        animacaoStart = new FadeTransition(Duration.millis(600), startLabel);
        animacaoStart.setDelay(Duration.millis(900));
        animacaoStart.setToValue(0);
        animacaoStart.setOnFinished(e -> startLabel.setVisible(false));
        animacaoStart.play();
    }

    private void iniciarAnimacaoBoca() {
        bocaAberta = true;
        animacaoBoca = new Timeline(new KeyFrame(Duration.millis(150), evento -> {
            bocaAberta = !bocaAberta;
            board.setBoca(bocaAberta);
        }));
        animacaoBoca.setCycleCount(Timeline.INDEFINITE);
        animacaoBoca.play();
    }

    // ------------------------------------------------------------------ simulação (modos 2–4)

    private void iniciarSimulacao() {
        simulacao = new Simulacao(modo, tabuleiro, robos);
        simulacao.setOuvinte(this::aoEvento);
        animacaoSimulacao = new Timeline(new KeyFrame(INTERVALO_SIMULACAO, e -> executarPasso()));
        animacaoSimulacao.setCycleCount(Timeline.INDEFINITE);
        animacaoSimulacao.setRate(INTERVALO_SIMULACAO.toMillis() / intervaloMs);
        animacaoSimulacao.play();
    }

    private void executarPasso() {
        Simulacao.Passo passo = simulacao.proximoPasso();
        Robo robo = passo.robo();
        if (!robo.isAtivo()) {
            iniciarMorte();
        } else if (tabuleiro.alimentoEncontradoPor(robo)) {
            sons.tocarFruta();
        }
        atualizarPainel();
        mostrarUltimo();

        if (simulacao.isTerminada()) {
            finalizar(simulacao.getResultado());
        }
    }

    private void mudarVelocidade(int nivel) {
        intervaloMs = INTERVALOS_MS[nivel - 1];
        if (animacaoSimulacao != null) {
            animacaoSimulacao.setRate(INTERVALO_SIMULACAO.toMillis() / intervaloMs);
        }
    }

    private Duration duracaoDeslize() {
        return Duration.millis(Math.min(150, intervaloMs * 0.4));
    }

    private void iniciarMorte() {
        sons.pararWakawaka();
        sons.tocarMorte(this::retomarWakawakaSeAlguemVivo);
    }

    private void retomarWakawakaSeAlguemVivo() {
        if (terminada || saiu) {
            return;
        }
        for (Robo r : robos) {
            if (r.isAtivo()) {
                sons.iniciarWakawaka();
                return;
            }
        }
    }

    // ------------------------------------------------------------------ modo manual

    private void mover(String direcao) {
        if (congelado || pausado || terminada) {
            return;
        }
        Robo robo = robos[0];
        dpad.destacar(direcao);
        board.setDirecao(robo, direcao);
        try {
            robo.mover(direcao);
            boolean achou = tabuleiro.alimentoEncontradoPor(robo);
            aoEvento(new EventoPartida(achou ? EventoPartida.Tipo.ACHOU_ALIMENTO : EventoPartida.Tipo.MOVEU,
                    robo, direcao, robo.getX(), robo.getY(), null));
        } catch (MovimentoInvalidoException e) {
            // O robô já publicou o evento INVALIDO (contado em movimentosInvalidos).
        }
        atualizarPainel();
        mostrarDica();

        if (tabuleiro.alimentoEncontradoPor(robo)) {
            sons.tocarFruta();
            finalizar("pacman " + robo.getCor() + " encontrou o alimento!");
        }
    }

    private void mostrarDica() {
        Robo r = robos[0];
        int dx = tabuleiro.getXAlimento() - r.getX();
        int dy = tabuleiro.getYAlimento() - r.getY();
        String onde = null;
        if (Math.abs(dx) + Math.abs(dy) == 1) {
            onde = dy == 1 ? "logo acima de você" : dy == -1 ? "logo abaixo de você"
                    : dx == 1 ? "logo à sua direita" : "logo à sua esquerda";
        }
        mensagem.mostrar("DICA", onde != null ? "Quase lá! A fruta está " + onde + "." : "Leve o pacman até a fruta.");
    }

    // ------------------------------------------------------------------ eventos → desenho

    private void aoEvento(EventoPartida e) {
        diario.registrar(e);
        Duration deslize = duracaoDeslize();
        switch (e.tipo()) {
            case MOVEU, ACHOU_ALIMENTO -> {
                board.setDirecao(e.robo(), e.direcao());
                board.sincronizar(deslize);
            }
            case INVALIDO -> {
                board.setDirecao(e.robo(), e.direcao());
                board.tremer(e.robo());
            }
            case ROCHA -> {
                board.setDirecao(e.robo(), e.direcao());
                board.baterNaRocha(e.robo(), e.direcao(), deslize.multiply(2));
            }
            case EXPLODIU -> {
                board.setDirecao(e.robo(), e.direcao());
                board.explodir(e.robo(), deslize, null);
            }
            case FANTASMA_SUMIU -> board.animarFantasmaSumindo(e.x(), e.y());
        }
    }

    private void mostrarUltimo() {
        DiarioPartida.Entrada ultima = diario.ultima();
        if (ultima != null) {
            mensagem.mostrar("ÚLTIMO", ultima.texto());
        }
    }

    private void atualizarPainel() {
        for (int i = 0; i < placares.length; i++) {
            Robo r = robos[i];
            String posicao = "(" + r.getX() + "," + r.getY() + ")";
            if (modo.isManual()) {
                placares[i].setValores(posicao, String.valueOf(r.getMovimentosValidos()),
                        String.valueOf(r.getMovimentosInvalidos()));
                boolean achou = tabuleiro.alimentoEncontradoPor(r);
                placares[i].setStatus(achou ? "ACHOU" : "JOGANDO", Tema.VERDE);
            } else {
                placares[i].setValores(r.isAtivo() ? posicao : "—", String.valueOf(r.getMovimentosValidos()),
                        String.valueOf(r.getMovimentosInvalidos()));
                if (!r.isAtivo()) {
                    placares[i].setStatus("EXPLODIU", Tema.VERMELHO);
                } else if (tabuleiro.alimentoEncontradoPor(r)) {
                    placares[i].setStatus("ACHOU", Tema.VERDE);
                } else {
                    placares[i].setStatus("ATIVO", Tema.VERDE);
                }
            }
        }
    }

    // ------------------------------------------------------------------ pausa, som, teclado

    private boolean podePausar() {
        return !congelado && !terminada;
    }

    private void alternarPausa() {
        if (!podePausar()) {
            return;
        }
        pausado = !pausado;
        for (Animation animacao : new Animation[]{animacaoBoca, animacaoSimulacao, animacaoStart}) {
            if (animacao == null) {
                continue;
            }
            if (pausado && animacao.getStatus() == Animation.Status.RUNNING) {
                animacao.pause();
            } else if (!pausado && animacao.getStatus() == Animation.Status.PAUSED) {
                animacao.play();
            }
        }
        if (pausado) {
            board.pausar();
            sons.pausarTudo();
            mensagem.mostrar("PAUSA", "PAUSADO — aperte P para continuar.", Tema.LARANJA);
        } else {
            board.retomar();
            sons.retomarTudo();
            if (modo.isManual()) {
                mostrarDica();
            } else {
                mostrarUltimo();
            }
        }
        pausaLabel.setVisible(pausado);
        atualizarBotoes();
    }

    private void alternarSom() {
        sons.setMudo(!sons.isMudo());
        atualizarBotoes();
    }

    private void atualizarBotoes() {
        controles.setPausado(pausado);
        controles.setPodePausar(podePausar());
        controles.setMudo(sons.isMudo());
    }

    @Override
    public void aoTecla(KeyEvent evento) {
        switch (evento.getCode()) {
            case M -> {
                alternarSom();
                evento.consume();
                return;
            }
            case P, SPACE -> {
                if (podePausar()) {
                    alternarPausa();
                }
                evento.consume();
                return;
            }
            default -> {
            }
        }
        if (!modo.isManual()) {
            return;
        }
        String direcao = switch (evento.getCode()) {
            case UP -> "up";
            case DOWN -> "down";
            case RIGHT -> "right";
            case LEFT -> "left";
            default -> null;
        };
        if (direcao != null) {
            evento.consume();
            mover(direcao);
        }
    }

    // ------------------------------------------------------------------ fim e saída

    private void finalizar(String resultado) {
        terminada = true;
        pararAnimacoes();
        sons.pararWakawaka();
        atualizarBotoes();
        mensagem.mostrar("FIM", textoFim(resultado));
        for (Robo r : robos) {
            if (r.isAtivo() && tabuleiro.alimentoEncontradoPor(r)) {
                board.setVencedor(r.getX(), r.getY());
            }
        }
        esperaFim = new PauseTransition(Duration.millis(board.temAnimacaoDeMorte() ? 2300 : 1300));
        esperaFim.setOnFinished(e -> {
            indoParaFim = true;
            navegador.irParaFim(new ResultadoPartida(config, tabuleiro, robos, diario, board.getExplosoes(), resultado));
        });
        esperaFim.play();
    }

    private String textoFim(String resultado) {
        if (modo.isManual()) {
            return "Você achou a fruta em " + robos[0].getTotalMovimentos() + " movimentos.";
        }
        for (Robo r : robos) {
            if (modo != ModoJogo.NORMAL_X_INTELIGENTE && r.isAtivo() && tabuleiro.alimentoEncontradoPor(r)) {
                return diario.nome(r) + " achou a fruta em " + r.getTotalMovimentos() + " movimentos.";
            }
        }
        return modo == ModoJogo.NORMAL_X_INTELIGENTE ? "Os dois pacmans acharam a fruta!" : resultado;
    }

    private void pararAnimacoes() {
        if (animacaoBoca != null) {
            animacaoBoca.stop();
        }
        if (animacaoSimulacao != null) {
            animacaoSimulacao.stop();
        }
    }

    @Override
    public void aoSair() {
        saiu = true;
        pararAnimacoes();
        for (Animation a : new Animation[]{limiteMusicaInicio, animacaoStart, esperaFim}) {
            if (a != null) {
                a.stop();
            }
        }
        board.pararAnimacoes();
        diaryList.desligar();
        for (Robo r : robos) {
            r.setOuvinteMovimentoInvalido(null);
        }
        if (simulacao != null) {
            simulacao.setOuvinte(e -> {
            });
        }
        if (indoParaFim) {
            // Deixa terminar o som de fruta/morte do último lance; o resto para.
            sons.pararInicio();
            sons.pararWakawaka();
        } else {
            sons.pararTudo();
        }
    }

    private static javafx.scene.paint.Color cor(Robo r) {
        return CoresPacman.pacman(r.getCor());
    }

    private static String tipo(Robo r) {
        return r instanceof RoboInteligente ? "inteligente" : "normal";
    }
}
