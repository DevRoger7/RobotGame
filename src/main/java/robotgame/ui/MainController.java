package robotgame.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import robotgame.excecao.MovimentoInvalidoException;
import robotgame.modelo.Fantasma;
import robotgame.modelo.ModoJogo;
import robotgame.modelo.Obstaculo;
import robotgame.modelo.Robo;
import robotgame.modelo.RoboInteligente;
import robotgame.modelo.Rocha;
import robotgame.modelo.Simulacao;
import robotgame.modelo.Tabuleiro;

public class MainController {

    private static final int TAMANHO_CELULA = 80;
    private static final Duration INTERVALO_SIMULACAO = Duration.millis(700);
    private static final Duration DURACAO_MORTE = Duration.millis(2000);
    private static final Duration LIMITE_MUSICA_INICIO = Duration.seconds(6);
    private static final Duration PASSO_ANIMACAO_MORTE = Duration.millis(30);

    @FXML
    private VBox setupPane;
    @FXML
    private VBox gamePane;
    @FXML
    private ComboBox<ModoJogo> modoCombo;
    @FXML
    private Label modoExplicacaoLabel;
    @FXML
    private Label cor1Label;
    @FXML
    private ComboBox<String> corCombo;
    @FXML
    private VBox cor2Box;
    @FXML
    private Label cor2Label;
    @FXML
    private ComboBox<String> cor2Combo;
    @FXML
    private Spinner<Integer> xSpinner;
    @FXML
    private Spinner<Integer> ySpinner;
    @FXML
    private Label setupErrorLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label statsLabel;
    @FXML
    private GridPane boardGrid;
    @FXML
    private TextArea logArea;
    @FXML
    private HBox obstaculosBar;
    @FXML
    private RadioButton fantasmaRadio;
    @FXML
    private RadioButton rochaRadio;
    @FXML
    private HBox legendaBox;
    @FXML
    private Label startLabel;
    @FXML
    private Label pausaLabel;
    @FXML
    private Button pausaButton;
    @FXML
    private Button somButton;

    private static final String[] ORDEM_FANTASMAS = {"azul", "vermelho", "rosa", "ciano"};
    private static final Map<String, Color> CORES = new LinkedHashMap<>();

    static {
        CORES.put("amarelo", Color.web("#ffd800"));
        CORES.put("vermelho", Color.web("#ff4444"));
        CORES.put("azul", Color.web("#4488ff"));
        CORES.put("verde", Color.web("#44ff66"));
        CORES.put("rosa", Color.web("#ff88cc"));
        CORES.put("laranja", Color.web("#ffa033"));
        CORES.put("roxo", Color.web("#cc55ff"));
        CORES.put("ciano", Color.web("#44eaff"));
        CORES.put("branco", Color.web("#f0f0f0"));
    }

    private final Image pacmanAberto = Imagens.carregar("pacman_aberto.png");
    private final Image pacmanFechado = Imagens.carregar("pacman_fechado.png");
    private final Image[] frutas = {
            Imagens.carregar("fruta_cereja.png"),
            Imagens.carregarSemFundo("fruta_maca.png"),
            Imagens.carregarSemFundo("fruta_morango.png")
    };
    private final Map<String, Image> imagensFantasmas = new HashMap<>();
    private final Image pedra = Imagens.carregarRecortada("obstaculo_pedra.png");
    private final Random random = new Random();

    private Image fruta;
    private int fantasmasColocados;
    private ModoJogo modo;
    private Tabuleiro tabuleiro;
    private Robo[] robos;
    private final Map<Robo, String> direcoes = new HashMap<>();
    private Simulacao simulacao;
    private boolean posicionandoObstaculos;

    private final List<ImageView> spritesPacman = new ArrayList<>();
    private boolean bocaAberta = true;
    private Timeline animacaoBoca;
    private Timeline animacaoSimulacao;
    private FadeTransition animacaoStart;
    private PauseTransition limiteMusicaInicio;
    private Timeline animacaoMorte;
    private final Map<Robo, Double> progressoMorte = new HashMap<>();
    private final Map<Robo, Node> nosMorrendo = new HashMap<>();
    private final Sons sons = new Sons();
    private boolean congelado;
    private boolean pausado;
    private boolean terminada;

    @FXML
    private void initialize() {
        int max = Tabuleiro.TAMANHO - 1;
        xSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, 0));
        ySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, max));

        modoCombo.getItems().setAll(ModoJogo.values());
        modoCombo.valueProperty().addListener((obs, antigo, novo) -> atualizarCamposDoModo());
        modoCombo.getSelectionModel().select(ModoJogo.MANUAL);

        for (ComboBox<String> combo : List.of(corCombo, cor2Combo)) {
            combo.getItems().setAll(CORES.keySet());
            combo.setCellFactory(lista -> celulaDeCor());
            combo.setButtonCell(celulaDeCor());
            combo.valueProperty().addListener((obs, antiga, nova) -> setupErrorLabel.setText(""));
        }
        corCombo.setValue("amarelo");
        cor2Combo.setValue("vermelho");

        ToggleGroup tipoObstaculo = new ToggleGroup();
        fantasmaRadio.setToggleGroup(tipoObstaculo);
        rochaRadio.setToggleGroup(tipoObstaculo);
        fantasmaRadio.setSelected(true);

        for (String cor : ORDEM_FANTASMAS) {
            imagensFantasmas.put(cor, Imagens.carregarSemFundo("fantasma_" + cor + ".png"));
        }

        logArea.setPrefHeight(Tabuleiro.TAMANHO * TAMANHO_CELULA + (Tabuleiro.TAMANHO - 1) * boardGrid.getVgap());
        atualizarBotoes();
    }

    private void atualizarCamposDoModo() {
        ModoJogo selecionado = modoCombo.getValue();
        boolean doisRobos = selecionado.getQuantidadeRobos() == 2;
        modoExplicacaoLabel.setText(selecionado.getExplicacao());
        mostrar(cor2Box, doisRobos);
        if (!doisRobos) {
            cor1Label.setText("Cor do pacman:");
        } else if (selecionado.isComRoboInteligente()) {
            cor1Label.setText("Cor do pacman normal:");
            cor2Label.setText("Cor do pacman inteligente:");
        } else {
            cor1Label.setText("Cor do pacman 1:");
            cor2Label.setText("Cor do pacman 2:");
        }
        setupErrorLabel.setText("");
    }

    @FXML
    private void iniciarJogo() {
        ModoJogo escolhido = modoCombo.getValue();
        String cor1 = corCombo.getValue();
        String cor2 = cor2Combo.getValue();
        if (escolhido.getQuantidadeRobos() == 2 && cor1.equals(cor2)) {
            setupErrorLabel.setText("Escolha cores diferentes para os dois pacmans.");
            return;
        }

        try {
            tabuleiro = new Tabuleiro(xSpinner.getValue(), ySpinner.getValue());
        } catch (IllegalArgumentException e) {
            setupErrorLabel.setText(e.getMessage());
            return;
        }

        modo = escolhido;
        if (modo.getQuantidadeRobos() == 1) {
            robos = new Robo[]{new Robo(cor1)};
        } else {
            robos = new Robo[]{
                    new Robo(cor1),
                    modo.isComRoboInteligente() ? new RoboInteligente(cor2) : new Robo(cor2)
            };
        }
        direcoes.clear();
        simulacao = null;
        posicionandoObstaculos = modo.isComObstaculos();
        fantasmasColocados = 0;
        fruta = frutas[random.nextInt(frutas.length)];

        setupErrorLabel.setText("");
        statsLabel.setText("");
        logArea.clear();
        mostrar(setupPane, false);
        mostrar(gamePane, true);
        mostrar(logArea, !modo.isManual());
        mostrar(obstaculosBar, posicionandoObstaculos);
        mostrar(legendaBox, robos.length > 1);

        if (posicionandoObstaculos) {
            atualizarStatusObstaculos();
        }

        bocaAberta = true;
        terminada = false;
        pausado = false;
        progressoMorte.clear();
        desenharTabuleiro();
        boardGrid.setFocusTraversable(true);
        boardGrid.requestFocus();

        if (!posicionandoObstaculos) {
            comecarPartida();
        }
        atualizarBotoes();
    }

    @FXML
    private void comecarSimulacao() {
        posicionandoObstaculos = false;
        mostrar(obstaculosBar, false);
        desenharTabuleiro();
        comecarPartida();
        atualizarBotoes();
    }

    private void comecarPartida() {
        congelado = true;
        statusLabel.setText("Prepare-se...");
        sons.tocarInicio(this::liberarPartida);
        limiteMusicaInicio = new PauseTransition(LIMITE_MUSICA_INICIO);
        limiteMusicaInicio.setOnFinished(e -> liberarPartida());
        limiteMusicaInicio.play();
    }

    private void liberarPartida() {
        if (!congelado) {
            return;
        }
        congelado = false;
        limiteMusicaInicio.stop();
        sons.pararInicio();
        mostrarStart();
        iniciarAnimacaoBoca();
        sons.iniciarWakawaka();
        if (modo.isManual()) {
            statusLabel.setText("Use as setas do teclado para mover o pacman " + robos[0].getCor() + ".");
        } else {
            iniciarSimulacao();
        }
        atualizarBotoes();
    }

    @FXML
    private void alternarPausa() {
        if (!podePausar()) {
            return;
        }
        pausado = !pausado;
        for (Animation animacao : new Animation[]{animacaoBoca, animacaoSimulacao, animacaoMorte, animacaoStart}) {
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
            sons.pausarTudo();
        } else {
            sons.retomarTudo();
        }
        pausaLabel.setVisible(pausado);
        atualizarBotoes();
    }

    @FXML
    private void alternarSom() {
        sons.setMudo(!sons.isMudo());
        atualizarBotoes();
    }

    private boolean podePausar() {
        return robos != null && !congelado && !terminada && !posicionandoObstaculos;
    }

    private void atualizarBotoes() {
        pausaButton.setText(pausado ? "Continuar (P)" : "Pausar (P)");
        pausaButton.setDisable(!podePausar());
        somButton.setText(sons.isMudo() ? "Som: desligado (M)" : "Som: ligado (M)");
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

    @FXML
    private void novoJogo() {
        pararAnimacoes();
        sons.pararTudo();
        if (limiteMusicaInicio != null) {
            limiteMusicaInicio.stop();
        }
        congelado = false;
        pausado = false;
        if (animacaoStart != null) {
            animacaoStart.stop();
        }
        if (animacaoMorte != null) {
            animacaoMorte.stop();
        }
        progressoMorte.clear();
        nosMorrendo.clear();
        startLabel.setVisible(false);
        pausaLabel.setVisible(false);
        robos = null;
        tabuleiro = null;
        simulacao = null;
        posicionandoObstaculos = false;
        spritesPacman.clear();

        mostrar(gamePane, false);
        mostrar(setupPane, true);
        setupErrorLabel.setText("");
    }

    public void aoTeclaPressionada(KeyEvent evento) {
        if (robos == null) {
            return;
        }
        switch (evento.getCode()) {
            case M -> {
                alternarSom();
                evento.consume();
                return;
            }
            case P, SPACE -> {
                if (podePausar()) {
                    alternarPausa();
                    evento.consume();
                }
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
        if (direcao == null) {
            return;
        }
        evento.consume();

        Robo robo = robos[0];
        if (congelado || pausado || terminada) {
            return;
        }
        direcoes.put(robo, direcao);

        try {
            robo.mover(direcao);
            statusLabel.setText("pacman " + robo.getCor() + " em (" + robo.getX() + "," + robo.getY() + ")");
        } catch (MovimentoInvalidoException e) {
            statusLabel.setText(e.getMessage());
        }

        desenharTabuleiro();

        if (tabuleiro.alimentoEncontradoPor(robo)) {
            sons.tocarFruta();
            finalizar("pacman " + robo.getCor() + " encontrou o alimento!");
        }
    }

    private void iniciarSimulacao() {
        simulacao = new Simulacao(modo, tabuleiro, robos);
        statusLabel.setText("Simulação em andamento...");
        animacaoSimulacao = new Timeline(new KeyFrame(INTERVALO_SIMULACAO, e -> executarPasso()));
        animacaoSimulacao.setCycleCount(Timeline.INDEFINITE);
        animacaoSimulacao.play();
    }

    private void executarPasso() {
        Simulacao.Passo passo = simulacao.proximoPasso();
        direcoes.put(passo.robo(), passo.direcao());
        statusLabel.setText(passo.mensagem());
        logArea.appendText(passo.mensagem() + "\n");

        Robo robo = passo.robo();
        if (!robo.isAtivo()) {
            iniciarMorte(robo);
        } else if (tabuleiro.alimentoEncontradoPor(robo)) {
            sons.tocarFruta();
        }
        desenharTabuleiro();

        if (simulacao.isTerminada()) {
            logArea.appendText(simulacao.getResultado() + "\n");
            finalizar(simulacao.getResultado());
        }
    }

    private void iniciarMorte(Robo robo) {
        progressoMorte.put(robo, 0.0);
        sons.pararWakawaka();
        sons.tocarMorte(this::retomarWakawakaSeAlguemVivo);
        if (animacaoMorte == null) {
            animacaoMorte = new Timeline(new KeyFrame(PASSO_ANIMACAO_MORTE, e -> avancarMortes()));
            animacaoMorte.setCycleCount(Timeline.INDEFINITE);
        }
        animacaoMorte.play();
    }

    private void retomarWakawakaSeAlguemVivo() {
        if (robos == null || terminada) {
            return;
        }
        for (Robo r : robos) {
            if (r.isAtivo()) {
                sons.iniciarWakawaka();
                return;
            }
        }
    }

    private void avancarMortes() {
        double incremento = PASSO_ANIMACAO_MORTE.toMillis() / DURACAO_MORTE.toMillis();
        boolean alguemSumiu = false;
        Iterator<Map.Entry<Robo, Double>> it = progressoMorte.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Robo, Double> entrada = it.next();
            double progresso = entrada.getValue() + incremento;
            if (progresso >= 1) {
                it.remove();
                alguemSumiu = true;
            } else {
                entrada.setValue(progresso);
                aplicarMorte(nosMorrendo.get(entrada.getKey()), progresso);
            }
        }
        if (alguemSumiu && robos != null) {
            desenharTabuleiro();
        }
        if (progressoMorte.isEmpty()) {
            animacaoMorte.stop();
        }
    }

    private static void aplicarMorte(Node no, double progresso) {
        if (no == null) {
            return;
        }
        double escala = 1 - progresso;
        no.setScaleX(escala);
        no.setScaleY(escala);
        no.setRotate(720 * progresso);
        no.setOpacity(1 - progresso * progresso);
    }

    private void finalizar(String resultado) {
        terminada = true;
        pararAnimacoes();
        sons.pararWakawaka();
        atualizarBotoes();
        statusLabel.setText(resultado);
        StringBuilder estatisticas = new StringBuilder();
        for (Robo r : robos) {
            if (!estatisticas.isEmpty()) {
                estatisticas.append('\n');
            }
            estatisticas.append(robos.length > 1 ? "pacman " + r.getCor() + " (" + tipo(r) + "): " : "")
                    .append(r.getTotalMovimentos()).append(" movimentos — ")
                    .append(r.getMovimentosValidos()).append(" válidos, ")
                    .append(r.getMovimentosInvalidos()).append(" inválidos");
        }
        statsLabel.setText(estatisticas.toString());
    }

    private void iniciarAnimacaoBoca() {
        bocaAberta = true;
        animacaoBoca = new Timeline(new KeyFrame(Duration.millis(150), evento -> {
            bocaAberta = !bocaAberta;
            for (ImageView sprite : spritesPacman) {
                sprite.setImage(bocaAberta ? pacmanAberto : pacmanFechado);
            }
        }));
        animacaoBoca.setCycleCount(Timeline.INDEFINITE);
        animacaoBoca.play();
    }

    private void pararAnimacoes() {
        if (animacaoBoca != null) {
            animacaoBoca.stop();
        }
        if (animacaoSimulacao != null) {
            animacaoSimulacao.stop();
        }
    }

    private void alternarObstaculo(int x, int y) {
        if (tabuleiro.getObstaculo(x, y) != null) {
            tabuleiro.removerObstaculo(x, y);
        } else {
            boolean ehFantasma = fantasmaRadio.isSelected();
            Obstaculo novo = ehFantasma ? new Fantasma(proximaCorFantasma()) : new Rocha();
            try {
                tabuleiro.adicionarObstaculo(novo, x, y);
            } catch (IllegalArgumentException e) {
                statusLabel.setText(e.getMessage());
                return;
            }
            if (ehFantasma) {
                fantasmasColocados++;
            }
        }
        atualizarStatusObstaculos();
        desenharTabuleiro();
    }

    private String proximaCorFantasma() {
        return ORDEM_FANTASMAS[fantasmasColocados % ORDEM_FANTASMAS.length];
    }

    private void atualizarStatusObstaculos() {
        int fantasmas = 0;
        int rochas = 0;
        for (int x = 0; x < Tabuleiro.TAMANHO; x++) {
            for (int y = 0; y < Tabuleiro.TAMANHO; y++) {
                Obstaculo o = tabuleiro.getObstaculo(x, y);
                if (o instanceof Fantasma) {
                    fantasmas++;
                } else if (o instanceof Rocha) {
                    rochas++;
                }
            }
        }
        statusLabel.setText("Clique nas casas para colocar ou remover obstáculos ("
                + fantasmas + " fantasma(s), " + rochas + " rocha(s)). Próximo fantasma: "
                + proximaCorFantasma() + ".");
    }

    private void desenharTabuleiro() {
        boardGrid.getChildren().clear();
        spritesPacman.clear();
        nosMorrendo.clear();
        int n = Tabuleiro.TAMANHO;
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                boardGrid.add(criarCelula(x, y), x, n - 1 - y);
            }
        }
        if (robos.length > 1) {
            atualizarLegenda();
        }
    }

    private StackPane criarCelula(int x, int y) {
        StackPane celula = new StackPane();
        celula.setPrefSize(TAMANHO_CELULA, TAMANHO_CELULA);
        celula.setStyle("-fx-background-color: #10101c; -fx-border-color: #2a2a55;");

        List<Integer> robosNaCasa = new ArrayList<>();
        for (int i = 0; i < robos.length; i++) {
            boolean visivel = robos[i].isAtivo() || progressoMorte.containsKey(robos[i]);
            if (visivel && robos[i].getX() == x && robos[i].getY() == y) {
                robosNaCasa.add(i);
            }
        }

        if (!robosNaCasa.isEmpty()) {
            HBox caixa = new HBox(2);
            caixa.setAlignment(Pos.CENTER);
            double largura = TAMANHO_CELULA / (robosNaCasa.size() == 1 ? 1.4 : 2.4);
            for (int i : robosNaCasa) {
                caixa.getChildren().add(criarRobo(i, largura));
            }
            celula.getChildren().add(caixa);
        } else if (x == tabuleiro.getXAlimento() && y == tabuleiro.getYAlimento()) {
            ImageView frutaView = new ImageView(fruta);
            frutaView.setPreserveRatio(true);
            frutaView.setFitWidth(TAMANHO_CELULA / 2.0);
            celula.getChildren().add(frutaView);
        } else if (tabuleiro.getObstaculo(x, y) != null) {
            celula.getChildren().add(criarObstaculo(tabuleiro.getObstaculo(x, y)));
        }

        if (posicionandoObstaculos) {
            celula.setCursor(Cursor.HAND);
            celula.setOnMouseClicked(e -> alternarObstaculo(x, y));
        }
        return celula;
    }

    private Node criarRobo(int indice, double largura) {
        Robo robo = robos[indice];
        ImageView sprite = new ImageView(bocaAberta ? pacmanAberto : pacmanFechado);
        sprite.setPreserveRatio(true);
        sprite.setFitWidth(largura);

        String direcao = direcoes.getOrDefault(robo, "right");
        sprite.setRotate(switch (direcao) {
            case "down" -> 90;
            case "up" -> 270;
            default -> 0;
        });
        sprite.setScaleX("left".equals(direcao) ? -1 : 1);

        Color cor = corDoRobo(robo.getCor());
        double altura = largura * pacmanAberto.getHeight() / pacmanAberto.getWidth();
        sprite.setEffect(new Blend(BlendMode.SRC_ATOP, null, new ColorInput(-2, -2, largura + 4, altura + 4, cor)));

        Node no = sprite;
        if (robos.length > 1) {
            Label numero = criarNumero(indice, cor);
            StackPane.setAlignment(numero, Pos.BOTTOM_RIGHT);
            no = new StackPane(sprite, numero);
        }

        if (progressoMorte.containsKey(robo)) {
            sprite.setImage(pacmanAberto);
            StackPane morrendo = new StackPane(no);
            aplicarMorte(morrendo, progressoMorte.get(robo));
            nosMorrendo.put(robo, morrendo);
            return morrendo;
        }
        spritesPacman.add(sprite);
        return no;
    }

    private Label criarNumero(int indice, Color cor) {
        Label numero = new Label(String.valueOf(indice + 1));
        numero.setStyle("-fx-background-color: " + hex(cor) + ";"
                + "-fx-background-radius: 8; -fx-text-fill: black; -fx-font-weight: bold;"
                + "-fx-font-size: 10px; -fx-padding: 0 4 0 4;");
        return numero;
    }

    private Node criarObstaculo(Obstaculo obstaculo) {
        Image imagem = obstaculo instanceof Fantasma fantasma ? imagensFantasmas.get(fantasma.getCor()) : pedra;
        ImageView view = new ImageView(imagem);
        view.setPreserveRatio(true);
        view.setFitWidth(TAMANHO_CELULA / 1.6);
        view.setFitHeight(TAMANHO_CELULA / 1.6);
        return view;
    }

    private void atualizarLegenda() {
        legendaBox.getChildren().clear();
        for (int i = 0; i < robos.length; i++) {
            Robo r = robos[i];
            String situacao = !r.isAtivo() ? " — pego por um fantasma"
                    : modo == ModoJogo.NORMAL_X_INTELIGENTE && tabuleiro.alimentoEncontradoPor(r) ? " — achou o alimento"
                    : "";
            Label texto = new Label(tipo(r) + " (" + r.getCor() + ")" + situacao);
            texto.setStyle("-fx-text-fill: #dddddd;");
            HBox item = new HBox(6, criarNumero(i, corDoRobo(r.getCor())), texto);
            item.setAlignment(Pos.CENTER);
            legendaBox.getChildren().add(item);
        }
    }

    private String tipo(Robo robo) {
        return robo instanceof RoboInteligente ? "pacman inteligente" : "pacman normal";
    }

    private static Color corDoRobo(String nome) {
        return CORES.get(nome);
    }

    private static ListCell<String> celulaDeCor() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String nome, boolean vazio) {
                super.updateItem(nome, vazio);
                if (vazio || nome == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(Character.toUpperCase(nome.charAt(0)) + nome.substring(1));
                    Circle amostra = new Circle(7, CORES.get(nome));
                    amostra.setStroke(Color.web("#444444"));
                    setGraphic(amostra);
                }
            }
        };
    }

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }

    private static void mostrar(Node no, boolean visivel) {
        no.setVisible(visivel);
        no.setManaged(visivel);
    }
}
