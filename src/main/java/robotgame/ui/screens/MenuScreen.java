package robotgame.ui.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import robotgame.modelo.FantasmaInteligente;
import robotgame.modelo.ModoJogo;
import robotgame.modelo.PlacarPvp;
import robotgame.modelo.Tabuleiro;
import robotgame.ui.components.ColorSwatchRow;
import robotgame.ui.components.Icones;
import robotgame.ui.components.KeyCap;
import robotgame.ui.components.MiniBoardPicker;
import robotgame.ui.components.ModeCard;
import robotgame.ui.components.NeonCard;
import robotgame.ui.components.Sprites;
import robotgame.ui.components.StatTile;
import robotgame.ui.components.Ui;
import robotgame.ui.partida.ConfiguracaoPartida;
import robotgame.ui.partida.CoresPacman;
import robotgame.ui.theme.Tema;

public class MenuScreen implements Tela {

    private static final String[][] MODOS = {
            {"MANUAL", "Você move o robô com as setas do teclado até achar a fruta.", "1 ROBÔ · VOCÊ MOVE"},
            {"CORRIDA", "Dois robôs andam sozinhos. Vence quem achar a fruta primeiro.", "2 ROBÔS · AUTOMÁTICO"},
            {"NORMAL × INTELIGENTE",
                    "Um robô comum e um esperto, que evita repetir o erro. Termina quando os dois acham a fruta.",
                    "2 ROBÔS · AUTOMÁTICO"},
            {"OBSTÁCULOS", "Você espalha fantasmas e rochas. O fantasma elimina o robô; a rocha o faz voltar.",
                    "2 ROBÔS · COM OBSTÁCULOS"},
            {"PAC-MAN COM PODER", "Três pacmans contra um fantasma caçador. Com a bolinha de poder, eles comem fantasmas.",
                    "3 ROBÔS · COM PODER"},
            {"PVP", "Setas contra W A S D, sem fruta e com rochas. Quem pegar a bolinha de poder elimina o outro.",
                    "2 JOGADORES · SEM FRUTA"}
    };

    private final Navegador navegador;
    private final ConfiguracaoPartida config;
    private final VBox raiz;
    private final List<ModeCard> cartoesModo = new ArrayList<>();
    private final ColorSwatchRow cores;
    private final Label nomeCor = Ui.label("", 16, "bold");
    private final HBox abas;
    private final Label aba1 = Ui.label("PACMAN 1", "aba");
    private final Label aba2 = Ui.label("PACMAN 2", "aba");
    private final Label aba3 = Ui.label("PACMAN 3", "aba");
    private final MiniBoardPicker mini;
    private final Label tituloFruta = Ui.label("ONDE FICA A FRUTA?", "section-label");
    private final HBox linhaFruta;
    private final Label tituloPvp = Ui.label("COMO JOGAR O PVP", "section-label");
    private final VBox ajudaPvp = new VBox(12);
    private final Label[] abasFormato = new Label[PlacarPvp.Formato.values().length];
    private final HBox linhaFormato;
    private final StatTile tileX = new StatTile("X (0–3)", "0", 26);
    private final StatTile tileY = new StatTile("Y (0–3)", "3", 26);
    private final Label erro = Ui.label("", "erro");
    private int editando = 1;

    public MenuScreen(Navegador navegador, ConfiguracaoPartida config) {
        this.navegador = navegador;
        this.config = config;

        HBox bolinhas = new HBox(14);
        for (int i = 0; i < 3; i++) {
            Circle c = new Circle(6, Tema.BOLINHA);
            c.setOpacity(0.7);
            bolinhas.getChildren().add(c);
        }
        bolinhas.setAlignment(Pos.CENTER);
        HBox esquerdaTitulo = Ui.linha(26, Sprites.pacman(Tema.AMARELO, 64), bolinhas);
        Label titulo = Ui.label("PACMAN GAME", 32, "pixel", "amarelo");
        titulo.setStyle(titulo.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(255,225,77,0.6), 18, 0.25, 0, 0);");
        VBox centroTitulo = new VBox(14, titulo, Ui.label("AJUDE O ROBÔ A CHEGAR NA FRUTA", 15, "semi", "muted"));
        centroTitulo.setAlignment(Pos.CENTER);
        HBox fantasmas = new HBox(24);
        for (String cor : new String[]{"azul", "rosa", "vermelho"}) {
            fantasmas.getChildren().add(Sprites.view(Sprites.fantasma(cor, 48), 48));
        }
        fantasmas.setAlignment(Pos.CENTER_RIGHT);
        esquerdaTitulo.setPrefWidth(240);
        fantasmas.setPrefWidth(240);
        HBox.setHgrow(centroTitulo, Priority.ALWAYS);
        HBox cartaoTitulo = new HBox(esquerdaTitulo, centroTitulo, fantasmas);
        cartaoTitulo.getStyleClass().add("neon-card");
        cartaoTitulo.setAlignment(Pos.CENTER);
        cartaoTitulo.setPadding(new Insets(0, 44, 0, 44));
        Ui.tamanhoFixo(cartaoTitulo, 1232, 132);

        NeonCard cartaoModos = new NeonCard("1 · ESCOLHA O MODO");
        cartaoModos.setSpacing(10);
        ModoJogo[] modos = ModoJogo.values();
        for (int i = 0; i < modos.length; i++) {
            ModoJogo modo = modos[i];
            ModeCard card = new ModeCard(i + 1, MODOS[i][0], MODOS[i][1], MODOS[i][2], () -> escolherModo(modo));
            VBox.setVgrow(card, Priority.ALWAYS);
            card.setMaxHeight(Double.MAX_VALUE);
            card.setPrefHeight(0);
            cartoesModo.add(card);
            cartaoModos.getChildren().add(card);
        }
        Ui.tamanhoFixo(cartaoModos, 640, 612);

        cores = new ColorSwatchRow(42, this::escolherCor);
        aba1.setOnMouseClicked(e -> editar(1));
        aba2.setOnMouseClicked(e -> editar(2));
        aba3.setOnMouseClicked(e -> editar(3));
        aba3.managedProperty().bind(aba3.visibleProperty());
        abas = new HBox(6, aba1, aba2, aba3);
        HBox cabecalhoCor = Ui.linha(0, Ui.label("COR DO PACMAN", "section-label"), Ui.espaco(), abas);
        HBox linhaCores = Ui.linha(12, cores, nomeCor);

        PlacarPvp.Formato[] formatos = PlacarPvp.Formato.values();
        HBox grupoFormato = new HBox(6);
        for (int i = 0; i < formatos.length; i++) {
            PlacarPvp.Formato formato = formatos[i];
            abasFormato[i] = Ui.label(formato.toString(), "aba");
            abasFormato[i].setOnMouseClicked(e -> escolherFormato(formato));
            grupoFormato.getChildren().add(abasFormato[i]);
        }
        linhaFormato = Ui.linha(0, Ui.label("PLACAR", "section-label"), Ui.espaco(), grupoFormato);

        mini = new MiniBoardPicker(this::escolherCasa);
        mini.setFruta(config.getXFruta(), config.getYFruta(), Sprites.fruta(config.getIndiceFruta(), 40));
        Label ajuda = Ui.label("Toque numa casa do mini-tabuleiro para mudar a fruta de lugar. "
                + "O pacman sempre começa no canto de baixo à esquerda.", 13.5, "muted");
        ajuda.setWrapText(true);
        erro.setWrapText(true);
        HBox tiles = Ui.linhaIgual(10, tileX, tileY);
        tileX.setPrefHeight(78);
        tileY.setPrefHeight(78);
        tileX.getValor().getStyleClass().add("amarelo");
        tileY.getValor().getStyleClass().add("amarelo");
        VBox ladoMini = new VBox(14, tiles, ajuda, erro);
        HBox.setHgrow(ladoMini, Priority.ALWAYS);
        linhaFruta = new HBox(18, mini, ladoMini);

        Button iniciar = Ui.primario("INICIAR", "btn-verde", 64);
        iniciar.setStyle("-fx-font-size: 14px;");
        iniciar.setGraphic(Icones.play(Tema.FUNDO, 14));
        iniciar.setOnAction(e -> iniciar());

        NeonCard cartaoPartida = new NeonCard("2 · MONTE SUA PARTIDA",
                cabecalhoCor, linhaCores, tituloFruta, linhaFruta, tituloPvp, ajudaPvp,
                linhaFormato, Ui.espaco(), iniciar);
        for (var no : new javafx.scene.Node[]{tituloPvp, ajudaPvp, linhaFormato}) {
            no.managedProperty().bind(no.visibleProperty());
        }
        for (var no : new javafx.scene.Node[]{tituloFruta, linhaFruta}) {
            no.managedProperty().bind(no.visibleProperty());
        }
        cartaoPartida.setSpacing(12);
        VBox.setMargin(cabecalhoCor, new Insets(8, 0, 0, 0));
        VBox.setMargin(iniciar, new Insets(0, 0, 6, 0));
        HBox.setHgrow(cartaoPartida, Priority.ALWAYS);
        cartaoPartida.setPrefHeight(612);

        HBox baixo = new HBox(24, cartaoModos, cartaoPartida);
        raiz = new VBox(16, cartaoTitulo, baixo);
        raiz.setPadding(new Insets(20, 24, 20, 24));

        escolherModo(config.getModo());
    }

    @Override
    public Parent getRaiz() {
        return raiz;
    }

    @Override
    public void aoTecla(KeyEvent evento) {
        if (evento.getCode() == KeyCode.ENTER) {
            evento.consume();
            iniciar();
        }
    }

    private void escolherModo(ModoJogo modo) {
        config.setModo(modo);
        for (int i = 0; i < cartoesModo.size(); i++) {
            cartoesModo.get(i).setSelecionado(ModoJogo.values()[i] == modo);
        }
        boolean pvp = modo.isPvp();
        tituloFruta.setVisible(!pvp);
        linhaFruta.setVisible(!pvp);
        tituloPvp.setVisible(pvp);
        ajudaPvp.setVisible(pvp);
        linhaFormato.setVisible(pvp);
        int quantidade = modo.getQuantidadeRobos();
        abas.setVisible(quantidade > 1);
        aba3.setVisible(quantidade == 3);
        if (editando > quantidade) {
            editando = 1;
        }
        erro.setText("");
        atualizar();
    }

    private void editar(int pacman) {
        editando = pacman;
        atualizar();
    }

    private void escolherFormato(PlacarPvp.Formato formato) {
        config.setFormatoPvp(formato);
        atualizar();
    }

    private void escolherCor(String cor) {
        switch (editando) {
            case 1 -> config.setCor1(cor);
            case 2 -> config.setCor2(cor);
            default -> config.setCor3(cor);
        }
        erro.setText(coresIguais() ? mensagemCoresIguais() : "");
        atualizar();
    }

    // Cores dos pacmans do modo atual, na ordem das abas.
    private String[] coresAtivas() {
        String[] todas = {config.getCor1(), config.getCor2(), config.getCor3()};
        return Arrays.copyOf(todas, config.getModo().getQuantidadeRobos());
    }

    private boolean coresIguais() {
        String[] ativas = coresAtivas();
        return Arrays.stream(ativas).distinct().count() < ativas.length;
    }

    private String mensagemCoresIguais() {
        return config.getModo().getQuantidadeRobos() == 3
                ? "Escolha cores diferentes para os três pacmans."
                : "Escolha cores diferentes para os dois pacmans.";
    }

    // Lança IllegalArgumentException com a mensagem para o jogador se a fruta não puder ficar em (x,y).
    private void validarFruta(int x, int y) {
        if (config.getModo().isPvp()) {
            return;
        }
        new Tabuleiro(x, y);
        if (config.getModo() == ModoJogo.PACMAN_PODER
                && x == FantasmaInteligente.X_INICIAL && y == FantasmaInteligente.Y_INICIAL) {
            throw new IllegalArgumentException("No modo Pac-Man com poder, a fruta não pode ficar em ("
                    + x + "," + y + "), posição inicial do fantasma inteligente.");
        }
    }

    private void escolherCasa(int x, int y) {
        try {
            validarFruta(x, y);
        } catch (IllegalArgumentException e) {
            erro.setText(e.getMessage());
            return;
        }
        config.setFruta(x, y);
        erro.setText(coresIguais() ? mensagemCoresIguais() : "");
        mini.setFruta(x, y, Sprites.fruta(config.getIndiceFruta(), 40));
        atualizar();
    }

    private void atualizar() {
        String[] ativas = coresAtivas();
        String atual = ativas[editando - 1];
        cores.mostrar(ativas.length > 1 ? editando : 0, ativas);
        nomeCor.setText(CoresPacman.rotulo(atual));
        Label[] todasAbas = {aba1, aba2, aba3};
        for (Label aba : todasAbas) {
            aba.getStyleClass().remove("ativa");
        }
        todasAbas[editando - 1].getStyleClass().add("ativa");
        for (Label aba : abasFormato) {
            aba.getStyleClass().remove("ativa");
        }
        abasFormato[config.getFormatoPvp().ordinal()].getStyleClass().add("ativa");
        tileX.setValor(String.valueOf(config.getXFruta()));
        tileY.setValor(String.valueOf(config.getYFruta()));
        mini.mostrarPacmans(ativas);
        atualizarAjudaPvp(ativas);
    }

    // Duas linhas, uma por jogador, com o pacman na cor escolhida e as teclas que ele usa.
    private void atualizarAjudaPvp(String[] ativas) {
        ajudaPvp.getChildren().clear();
        if (!config.getModo().isPvp()) {
            return;
        }
        ajudaPvp.getChildren().addAll(
                linhaJogador(CoresPacman.pacman(ativas[0]), "Jogador 1",
                        new KeyCap("←", true), new KeyCap("↑", true), new KeyCap("↓", true), new KeyCap("→", true)),
                linhaJogador(CoresPacman.pacman(ativas[1]), "Jogador 2",
                        new KeyCap("A", true), new KeyCap("W", true), new KeyCap("S", true), new KeyCap("D", true)));
        Label regras = Ui.label("Não há fruta. A bolinha de poder aparece sozinha: quem pegar elimina o outro "
                + "jogador ao encostar nele. O fantasma anda ao acaso e elimina quem estiver sem poder. "
                + "Antes de começar, você escolhe onde ficam as rochas. "
                + "Na melhor de 3 ou de 5, vence quem chegar primeiro a 2 ou 3 vitórias; empate não conta.",
                13.5, "muted");
        regras.setWrapText(true);
        ajudaPvp.getChildren().add(regras);
    }

    private HBox linhaJogador(javafx.scene.paint.Color cor, String nome, KeyCap... teclas) {
        Label rotulo = Ui.label(nome, 16, "bold");
        Ui.tamanhoFixo(rotulo, 110, 28);
        HBox linha = Ui.linha(8, Sprites.pacman(cor, 30), rotulo);
        linha.getChildren().addAll(teclas);
        return linha;
    }

    private void iniciar() {
        if (coresIguais()) {
            erro.setText(mensagemCoresIguais());
            return;
        }
        try {
            validarFruta(config.getXFruta(), config.getYFruta());
        } catch (IllegalArgumentException e) {
            erro.setText(e.getMessage());
            return;
        }
        if (config.getModo().isPvp()) {
            config.comecarSeriePvp();
        }
        if (config.getModo().isComObstaculos()) {
            navegador.irParaCenario(config);
        } else {
            navegador.irParaJogo(config);
        }
    }
}
