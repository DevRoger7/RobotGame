package robotgame.ui.screens;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import robotgame.excecao.MovimentoInvalidoException;
import robotgame.modelo.EventoPartida;
import robotgame.modelo.ModoJogo;
import robotgame.modelo.PartidaPvp;
import robotgame.modelo.PlacarPvp;
import robotgame.modelo.Robo;
import robotgame.modelo.FantasmaAleatorio;
import robotgame.modelo.FantasmaInteligente;
import robotgame.modelo.RoboInteligente;
import robotgame.modelo.RoboSuperInteligente;
import robotgame.modelo.Simulacao;
import robotgame.modelo.SimulacaoPoder;
import robotgame.modelo.SimulacaoPoderConcorrente;
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

public class GameScreen implements Tela {

    private static final Duration INTERVALO_SIMULACAO = Duration.millis(700);
    private static final double[] INTERVALOS_MS = {1400, 1000, 700, 450, 250};
    private static final Duration LIMITE_MUSICA_INICIO = Duration.seconds(6);
    // No PvP o tempo corre em rodadas de 1 s: o fantasma anda e o poder dos pacmans gasta um turno.
    private static final Duration INTERVALO_PVP = Duration.seconds(1);
    private static final String[] TECLAS_PVP = {"setas", "W A S D"};

    private record ComandoPvp(int jogador, String direcao) {
    }

    private static final Map<KeyCode, ComandoPvp> COMANDOS_PVP = Map.of(
            KeyCode.UP, new ComandoPvp(0, "up"),
            KeyCode.DOWN, new ComandoPvp(0, "down"),
            KeyCode.RIGHT, new ComandoPvp(0, "right"),
            KeyCode.LEFT, new ComandoPvp(0, "left"),
            KeyCode.W, new ComandoPvp(1, "up"),
            KeyCode.S, new ComandoPvp(1, "down"),
            KeyCode.D, new ComandoPvp(1, "right"),
            KeyCode.A, new ComandoPvp(1, "left"));

    private final Navegador navegador;
    private final ConfiguracaoPartida config;
    private final ModoJogo modo;
    private final Sons sons;
    private final Tabuleiro tabuleiro;
    private final Robo[] robos;
    private final DiarioPartida diario;
    private final boolean pvp;
    private final Set<KeyCode> teclasPressionadas = new HashSet<>();

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
    // Usada só no modo PACMAN_PODER, no lugar de simulacao.
    private SimulacaoPoder simulacaoPoder;
    private SimulacaoPoderConcorrente simulacaoPoderConcorrente;
    // Usada só no modo PVP.
    private PartidaPvp partidaPvp;
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
        this.pvp = modo.isPvp();
        this.diario = new DiarioPartida(robos, modo);
        for (Robo r : robos) {
            r.setOuvinteMovimentoInvalido(this::receberEvento);
        }

        if (tabuleiro.temAlimento()) {
            board.setFruta(tabuleiro.getXAlimento(), tabuleiro.getYAlimento(),
                    Sprites.fruta(config.getIndiceFruta(), 80));
        }
        board.carregarObstaculos(tabuleiro);
        board.setRobos(robos);
        FantasmaInteligente fantasma = tabuleiro.getFantasmaInteligente();
        if (fantasma != null) {
            board.setFantasmaInteligente(fantasma.getX(), fantasma.getY());
        }
        FantasmaAleatorio fantasmaAleatorio = tabuleiro.getFantasmaAleatorio();
        if (fantasmaAleatorio != null) {
            board.setFantasmaAleatorio(fantasmaAleatorio.getX(), fantasmaAleatorio.getY());
        }
        montarSobreposicoes();

        controles = new ControlsPanel(!modo.isManual() && !pvp, this::alternarPausa, this::alternarSom,
                () -> navegador.irParaMenu(config));
        controles.aoMudarVelocidade((obs, antes, agora) -> mudarVelocidade(agora.intValue()));

        VBox painel;
        if (modo.isManual()) {
            placares = new ScoreCard[]{new ScoreCard(cor(robos[0]), "PACMAN 1", "você controla", 124,
                    "POSIÇÃO", "MOVIMENTOS", "INVÁLIDOS")};
            dpad = new DPad(this::mover);
            diaryList = new DiaryList(diario.getEntradas(), 3, e -> true);
            painel = LayoutJogo.painel(12, placares[0], comoMover(), cartaoDiario(), legenda(), controles);
        } else if (pvp) {
            placares = new ScoreCard[robos.length];
            for (int i = 0; i < robos.length; i++) {
                placares[i] = new ScoreCard(cor(robos[i]), "JOGADOR " + (i + 1), TECLAS_PVP[i], 156,
                        "VITÓRIAS", "VÁLIDOS", "INVÁLIDOS");
                placares[i].statusAbaixoDoNome();
            }
            dpad = null;
            diaryList = new DiaryList(diario.getEntradas(), 6, e -> true);
            painel = LayoutJogo.painel(12, Ui.linhaIgual(12, placares), comoMoverPvp(), cartaoDiario(), legenda(),
                    controles);
        } else {
            placares = new ScoreCard[robos.length];
            for (int i = 0; i < robos.length; i++) {
                if (modo == ModoJogo.PACMAN_PODER) {
                    // 3 cartões lado a lado: sem a coluna POSIÇÃO (já aparece no tabuleiro) e status embaixo do nome.
                    placares[i] = new ScoreCard(cor(robos[i]), "PACMAN " + (i + 1), tipo(robos[i]), 156,
                            "VÁLIDOS", "INVÁLIDOS");
                    placares[i].statusAbaixoDoNome();
                } else {
                    placares[i] = new ScoreCard(cor(robos[i]), "PACMAN " + (i + 1), tipo(robos[i]), 132,
                            "POSIÇÃO", "VÁLIDOS", "INVÁLIDOS");
                }
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

    private NeonCard comoMoverPvp() {
        VBox linhas = new VBox(8,
                linhaTeclas(0, new KeyCap("←", true), new KeyCap("↑", true), new KeyCap("↓", true),
                        new KeyCap("→", true)),
                linhaTeclas(1, new KeyCap("A", true), new KeyCap("W", true), new KeyCap("S", true),
                        new KeyCap("D", true)));
        Label aviso = Ui.label("Com a bolinha de poder, encoste no rival para eliminá-lo.", 14, "muted");
        aviso.setWrapText(true);
        NeonCard cartao = new NeonCard("COMO MOVER", linhas, aviso);
        Ui.tamanhoFixo(cartao, LayoutJogo.LARGURA_PAINEL, 130);
        cartao.setSpacing(8);
        return cartao;
    }

    private HBox linhaTeclas(int jogador, KeyCap... teclas) {
        Label nome = Ui.label("Jogador " + (jogador + 1), 14, "semi");
        Ui.tamanhoFixo(nome, 96, 24);
        HBox linha = Ui.linha(6, Sprites.pacman(cor(robos[jogador]), 22), nome);
        linha.getChildren().addAll(teclas);
        return linha;
    }

    private NeonCard legendaPvp() {
        boolean comRochas = !config.getObstaculos().isEmpty();
        LegendGrid grade = new LegendGrid(comRochas ? 3 : 2);
        for (int i = 0; i < robos.length; i++) {
            grade.item(Sprites.pacman(cor(robos[i]), 22), "Jogador " + (i + 1), TECLAS_PVP[i]);
        }
        grade.item(Sprites.fantasmaAleatorio(22), "Fantasma", "elimina sem poder");
        // Mesma cor da bolinha desenhada no CellView.
        grade.item(new Circle(7, Color.web("#ffb897")), "Bolinha de poder", "elimina o rival");
        if (comRochas) {
            grade.item(Sprites.view(Sprites.rocha(22), 22), "Rocha", "volta 1 casa");
        }
        return new NeonCard("LEGENDA", grade);
    }

    private NeonCard legenda() {
        if (pvp) {
            return legendaPvp();
        }
        if (modo == ModoJogo.PACMAN_PODER) {
            return legendaPoder();
        }
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

    private NeonCard legendaPoder() {
        LegendGrid grade = new LegendGrid(LegendGrid.colunasPara(robos.length + 5));
        for (int i = 0; i < robos.length; i++) {
            grade.item(Sprites.pacman(cor(robos[i]), 22), "Pacman " + (i + 1), tipo(robos[i]));
        }
        grade.item(Sprites.fantasmaInteligente(22), "Fantasma esperto", "caça os pacmans");
        grade.item(Sprites.view(Sprites.fantasma("azul", 22), 22), "Fantasma burro", "elimina");
        grade.item(Sprites.view(Sprites.rocha(22), 22), "Rocha", "volta 1 casa");
        // Mesma cor da bolinha desenhada no CellView.
        grade.item(new Circle(7, Color.web("#ffb897")), "Bolinha de poder", "come fantasmas");
        grade.item(Sprites.view(Sprites.fruta(config.getIndiceFruta(), 22), 22), "Fruta", "objetivo");
        return new NeonCard("LEGENDA", grade);
    }

    private void comecarPartida() {
        congelado = true;
        mensagem.mostrar("STATUS", textoPreparo(), Tema.CIANO);
        sons.tocarInicio(this::liberarPartida);
        limiteMusicaInicio = new PauseTransition(LIMITE_MUSICA_INICIO);
        limiteMusicaInicio.setOnFinished(e -> liberarPartida());
        limiteMusicaInicio.play();
        atualizarBotoes();
    }

    private String textoPreparo() {
        if (!pvp) {
            return "Prepare-se...";
        }
        PlacarPvp placar = config.getPlacarPvp();
        String partida = "Partida " + placar.getPartidaAtual();
        if (placar.getFormato().isLivre()) {
            return partida + " — prepare-se...";
        }
        return partida + " · " + placar.getFormato().toString().toLowerCase() + " — prepare-se...";
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
        if (pvp) {
            diario.registrarInicioPvp();
        } else {
            diario.registrarInicio(tabuleiro.getXAlimento(), tabuleiro.getYAlimento());
        }
        if (modo.isManual()) {
            mostrarDica();
        } else {
            iniciarSimulacao();
            if (pvp) {
                mensagem.mostrar("DICA", "Pegue a bolinha de poder e elimine o outro jogador!");
            } else {
                mostrarUltimo();
            }
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

    private void iniciarSimulacao() {
        if (pvp) {
            partidaPvp = new PartidaPvp(tabuleiro, robos[0], robos[1]);
            partidaPvp.setOuvinte(this::aoEvento);
            animacaoSimulacao = new Timeline(new KeyFrame(INTERVALO_PVP, e -> executarRodadaPvp()));
            animacaoSimulacao.setCycleCount(Timeline.INDEFINITE);
            animacaoSimulacao.play();
            return;
        }
        if (modo == ModoJogo.PACMAN_PODER) {
            simulacaoPoder = new SimulacaoPoder(tabuleiro, robos);
            simulacaoPoder.setOuvinte(this::receberEvento);
            simulacaoPoderConcorrente = new SimulacaoPoderConcorrente(simulacaoPoder, tabuleiro,
                    robos, Math.round(intervaloMs), this::receberAtualizacaoPoder);
            simulacaoPoderConcorrente.iniciar();
            return;
        } else {
            simulacao = new Simulacao(modo, tabuleiro, robos);
            simulacao.setOuvinte(this::aoEvento);
        }
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

    private void executarRodadaPvp() {
        int ativosAntes = ativos();
        partidaPvp.passarRodada();
        if (ativos() < ativosAntes) {
            iniciarMorte();
        }
        atualizarPainel();
        mostrarUltimo();
        if (partidaPvp.isTerminada()) {
            finalizar(partidaPvp.getResultado());
        }
    }

    private void moverPvp(int jogador, String direcao) {
        if (congelado || pausado || terminada || partidaPvp == null) {
            return;
        }
        Robo robo = robos[jogador];
        if (!robo.isAtivo()) {
            return;
        }
        board.setDirecao(robo, direcao);
        int ativosAntes = ativos();
        partidaPvp.mover(robo, direcao);
        if (ativos() < ativosAntes) {
            iniciarMorte();
        }
        atualizarPainel();
        mostrarUltimo();
        if (partidaPvp.isTerminada()) {
            finalizar(partidaPvp.getResultado());
        }
    }

    private void receberAtualizacaoPoder(SimulacaoPoderConcorrente.Atualizacao atualizacao) {
        Platform.runLater(() -> {
            if (saiu || terminada) {
                return;
            }
            synchronized (simulacaoPoder) {
                if (atualizacao.houveEliminacao()) {
                    iniciarMorte();
                } else if (atualizacao.encontrouAlimento()) {
                    sons.tocarFruta();
                }
                atualizarPainel();
                mostrarUltimo();
                if (atualizacao.resultado() != null) {
                    finalizar(atualizacao.resultado());
                }
            }
        });
    }

    private int ativos() {
        int n = 0;
        for (Robo r : robos) {
            if (r.isAtivo()) {
                n++;
            }
        }
        return n;
    }

    private void mudarVelocidade(int nivel) {
        intervaloMs = INTERVALOS_MS[nivel - 1];
        if (simulacaoPoderConcorrente != null) {
            simulacaoPoderConcorrente.mudarIntervalo(Math.round(intervaloMs));
        }
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
            case PODER_APARECEU -> board.setItemPoder(e.x(), e.y());
            case PODER_SUMIU -> board.removerItemPoder();
            case PEGOU_PODER -> {
                board.setDirecao(e.robo(), e.direcao());
                board.removerItemPoder();
                board.sincronizar(deslize);
                sons.tocarFruta();
            }
            case PODER_ACABOU -> board.sincronizar(deslize);
            case FANTASMA_MOVEU -> board.moverFantasmaInteligente(e.x(), e.y(), deslize);
            // Nestes dois a direção pode ser a do fantasma (turno dele), então o pacman não é virado.
            case COMEU_FANTASMA -> {
                board.sincronizar(deslize);
                board.removerFantasmaInteligente(true);
                sons.tocarFruta();
            }
            case ELIMINADO_PELO_FANTASMA, PACMAN_ELIMINADO -> board.explodir(e.robo(), deslize, null);
            case COMEU_FANTASMA_BURRO -> {
                board.setDirecao(e.robo(), e.direcao());
                board.carregarObstaculos(tabuleiro);
                board.sincronizar(deslize);
                sons.tocarFruta();
            }
        }
    }

    private void receberEvento(EventoPartida evento) {
        if (Platform.isFxApplicationThread()) {
            aoEvento(evento);
            return;
        }
        Platform.runLater(() -> {
            if (saiu || terminada) {
                return;
            }
            synchronized (simulacaoPoder) {
                aoEvento(evento);
            }
        });
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
            } else if (pvp) {
                placares[i].setValores(config.getPlacarPvp().textoVitorias(i),
                        String.valueOf(r.getMovimentosValidos()), String.valueOf(r.getMovimentosInvalidos()));
                if (!r.isAtivo()) {
                    placares[i].setStatus("ELIMINADO", Tema.VERMELHO);
                } else if (r.isComPoder()) {
                    placares[i].setStatus("PODER " + r.getTurnosDePoder(), Tema.AMARELO);
                } else {
                    placares[i].setStatus("ATIVO", Tema.VERDE);
                }
            } else if (modo == ModoJogo.PACMAN_PODER) {
                placares[i].setValores(String.valueOf(r.getMovimentosValidos()),
                        String.valueOf(r.getMovimentosInvalidos()));
                if (!r.isAtivo()) {
                    placares[i].setStatus("ELIMINADO", Tema.VERMELHO);
                } else if (tabuleiro.alimentoEncontradoPor(r)) {
                    placares[i].setStatus("ACHOU", Tema.VERDE);
                } else {
                    placares[i].setStatus("ATIVO", Tema.VERDE);
                }
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

    private boolean podePausar() {
        return !congelado && !terminada;
    }

    private void alternarPausa() {
        if (!podePausar()) {
            return;
        }
        pausado = !pausado;
        if (simulacaoPoderConcorrente != null) {
            if (pausado) {
                simulacaoPoderConcorrente.pausar();
            } else {
                simulacaoPoderConcorrente.retomar();
            }
        }
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
        if (pvp) {
            aoTeclaPvp(evento);
            return;
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

    private void aoTeclaPvp(KeyEvent evento) {
        ComandoPvp comando = COMANDOS_PVP.get(evento.getCode());
        if (comando == null) {
            return;
        }
        evento.consume();
        // Só a primeira batida conta: o auto-repeat do teclado só repete a última tecla segurada,
        // o que daria vantagem a quem segura a tecla.
        if (teclasPressionadas.add(evento.getCode())) {
            moverPvp(comando.jogador(), comando.direcao());
        }
    }

    @Override
    public void aoTeclaSolta(KeyEvent evento) {
        teclasPressionadas.remove(evento.getCode());
    }

    private void finalizar(String resultado) {
        terminada = true;
        if (pvp) {
            registrarNoPlacar();
            atualizarPainel(); // o tile VITÓRIAS mostra o ponto novo antes da tela de fim
        }
        pararAnimacoes();
        sons.pararWakawaka();
        atualizarBotoes();
        mensagem.mostrar("FIM", textoFim(resultado));
        for (Robo r : robos) {
            if (r.isAtivo() && tabuleiro.alimentoEncontradoPor(r)) {
                board.setVencedor(r.getX(), r.getY());
            }
        }
        if (pvp && partidaPvp.getVencedor() != null) {
            board.setVencedor(partidaPvp.getVencedor().getX(), partidaPvp.getVencedor().getY());
        }
        esperaFim = new PauseTransition(Duration.millis(board.temAnimacaoDeMorte() ? 2300 : 1300));
        esperaFim.setOnFinished(e -> {
            indoParaFim = true;
            navegador.irParaFim(new ResultadoPartida(config, tabuleiro, robos, diario, board.getExplosoes(), resultado));
        });
        esperaFim.play();
    }

    private void registrarNoPlacar() {
        Robo vencedor = partidaPvp.getVencedor();
        if (vencedor == null) {
            config.getPlacarPvp().registrarEmpate();
        } else {
            config.getPlacarPvp().registrarVitoria(vencedor == robos[0] ? 0 : 1);
        }
    }

    private String textoFim(String resultado) {
        if (modo.isManual()) {
            return "Você achou a fruta em " + robos[0].getTotalMovimentos() + " movimentos.";
        }
        if (pvp) {
            return resultado;
        }
        if (modo == ModoJogo.PACMAN_PODER) {
            // Vários podem estar na fruta; o texto da SimulacaoPoder diz quem chegou primeiro.
            return resultado;
        }
        for (Robo r : robos) {
            if (modo != ModoJogo.NORMAL_X_INTELIGENTE && r.isAtivo() && tabuleiro.alimentoEncontradoPor(r)) {
                return diario.nome(r) + " achou a fruta em " + r.getTotalMovimentos() + " movimentos.";
            }
        }
        return modo == ModoJogo.NORMAL_X_INTELIGENTE ? "Os dois pacmans acharam a fruta!" : resultado;
    }

    private void pararAnimacoes() {
        if (simulacaoPoderConcorrente != null) {
            simulacaoPoderConcorrente.parar();
        }
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
        if (simulacaoPoder != null) {
            simulacaoPoder.setOuvinte(e -> {
            });
        }
        if (partidaPvp != null) {
            partidaPvp.setOuvinte(e -> {
            });
        }
        if (indoParaFim) {
            sons.pararInicio();
            sons.pararWakawaka();
        } else {
            sons.pararTudo();
        }
    }

    private static Color cor(Robo r) {
        return CoresPacman.pacman(r.getCor());
    }

    // RoboSuperInteligente é subclasse de RoboInteligente: precisa ser testado antes.
    private static String tipo(Robo r) {
        if (r instanceof RoboSuperInteligente) {
            return "super";
        }
        return r instanceof RoboInteligente ? "inteligente" : "normal";
    }
}
