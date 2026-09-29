package robotgame.ui.screens;

import java.util.ArrayList;
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

import robotgame.modelo.ModoJogo;
import robotgame.modelo.Tabuleiro;
import robotgame.ui.components.ColorSwatchRow;
import robotgame.ui.components.Icones;
import robotgame.ui.components.MiniBoardPicker;
import robotgame.ui.components.ModeCard;
import robotgame.ui.components.NeonCard;
import robotgame.ui.components.Sprites;
import robotgame.ui.components.StatTile;
import robotgame.ui.components.Ui;
import robotgame.ui.partida.ConfiguracaoPartida;
import robotgame.ui.partida.CoresPacman;
import robotgame.ui.theme.Tema;

/** Menu: escolha do modo, das cores e da casa da fruta. */
public class MenuScreen implements Tela {

    private static final String[][] MODOS = {
            {"MANUAL", "Você move o robô com as setas do teclado até achar a fruta.", "1 ROBÔ · VOCÊ MOVE"},
            {"CORRIDA", "Dois robôs andam sozinhos. Vence quem achar a fruta primeiro.", "2 ROBÔS · AUTOMÁTICO"},
            {"NORMAL × INTELIGENTE",
                    "Um robô comum e um esperto, que evita repetir o erro. Termina quando os dois acham a fruta.",
                    "2 ROBÔS · AUTOMÁTICO"},
            {"OBSTÁCULOS", "Você espalha fantasmas e rochas. O fantasma elimina o robô; a rocha o faz voltar.",
                    "2 ROBÔS · COM OBSTÁCULOS"}
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
    private final MiniBoardPicker mini;
    private final StatTile tileX = new StatTile("X (0–3)", "0", 26);
    private final StatTile tileY = new StatTile("Y (0–3)", "3", 26);
    private final Label erro = Ui.label("", "erro");
    private int editando = 1;

    public MenuScreen(Navegador navegador, ConfiguracaoPartida config) {
        this.navegador = navegador;
        this.config = config;

        // Cartão de título
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

        // 1 · ESCOLHA O MODO
        NeonCard cartaoModos = new NeonCard("1 · ESCOLHA O MODO");
        cartaoModos.setSpacing(16);
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

        // 2 · MONTE SUA PARTIDA
        cores = new ColorSwatchRow(42, this::escolherCor);
        aba1.setOnMouseClicked(e -> editar(1));
        aba2.setOnMouseClicked(e -> editar(2));
        abas = new HBox(6, aba1, aba2);
        HBox cabecalhoCor = Ui.linha(0, Ui.label("COR DO PACMAN", "section-label"), Ui.espaco(), abas);
        HBox linhaCores = Ui.linha(12, cores, nomeCor);

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
        HBox linhaFruta = new HBox(18, mini, ladoMini);

        Button iniciar = Ui.primario("INICIAR", "btn-verde", 64);
        iniciar.setStyle("-fx-font-size: 14px;");
        iniciar.setGraphic(Icones.play(Tema.FUNDO, 14));
        iniciar.setOnAction(e -> iniciar());

        NeonCard cartaoPartida = new NeonCard("2 · MONTE SUA PARTIDA",
                cabecalhoCor, linhaCores, Ui.label("ONDE FICA A FRUTA?", "section-label"), linhaFruta,
                Ui.espaco(), iniciar);
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
        boolean dois = modo.getQuantidadeRobos() == 2;
        abas.setVisible(dois);
        if (!dois) {
            editando = 1;
        }
        erro.setText("");
        atualizar();
    }

    private void editar(int pacman) {
        editando = pacman;
        atualizar();
    }

    private void escolherCor(String cor) {
        if (editando == 1) {
            config.setCor1(cor);
        } else {
            config.setCor2(cor);
        }
        erro.setText(coresIguais() ? "Escolha cores diferentes para os dois pacmans." : "");
        atualizar();
    }

    private boolean coresIguais() {
        return config.getModo().getQuantidadeRobos() == 2 && config.getCor1().equals(config.getCor2());
    }

    // Mesma validação de antes: o próprio Tabuleiro recusa (0,0) e casas fora de 0–3.
    private void escolherCasa(int x, int y) {
        try {
            new Tabuleiro(x, y);
        } catch (IllegalArgumentException e) {
            erro.setText(e.getMessage());
            return;
        }
        config.setFruta(x, y);
        erro.setText(coresIguais() ? "Escolha cores diferentes para os dois pacmans." : "");
        mini.setFruta(x, y, Sprites.fruta(config.getIndiceFruta(), 40));
        atualizar();
    }

    private void atualizar() {
        boolean dois = config.getModo().getQuantidadeRobos() == 2;
        String atual = editando == 1 ? config.getCor1() : config.getCor2();
        cores.mostrar(atual, dois ? editando : 0, dois ? (editando == 1 ? config.getCor2() : config.getCor1()) : null);
        nomeCor.setText(CoresPacman.rotulo(atual));
        aba1.getStyleClass().remove("ativa");
        aba2.getStyleClass().remove("ativa");
        (editando == 1 ? aba1 : aba2).getStyleClass().add("ativa");
        tileX.setValor(String.valueOf(config.getXFruta()));
        tileY.setValor(String.valueOf(config.getYFruta()));
        if (dois) {
            mini.mostrarPacmans(config.getCor1(), config.getCor2());
        } else {
            mini.mostrarPacmans(config.getCor1());
        }
    }

    private void iniciar() {
        if (coresIguais()) {
            erro.setText("Escolha cores diferentes para os dois pacmans.");
            return;
        }
        try {
            new Tabuleiro(config.getXFruta(), config.getYFruta());
        } catch (IllegalArgumentException e) {
            erro.setText(e.getMessage());
            return;
        }
        if (config.getModo().isComObstaculos()) {
            navegador.irParaCenario(config);
        } else {
            navegador.irParaJogo(config);
        }
    }
}
