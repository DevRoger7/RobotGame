package robotgame.ui.screens;

import java.util.Iterator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import robotgame.modelo.Fantasma;
import robotgame.modelo.Obstaculo;
import robotgame.modelo.Rocha;
import robotgame.modelo.Tabuleiro;
import robotgame.ui.components.BoardView;
import robotgame.ui.components.Icones;
import robotgame.ui.components.LegendGrid;
import robotgame.ui.components.MessageBar;
import robotgame.ui.components.NeonCard;
import robotgame.ui.components.Sprites;
import robotgame.ui.components.StatTile;
import robotgame.ui.components.StepperBar;
import robotgame.ui.components.Ui;
import robotgame.ui.partida.ConfiguracaoPartida;
import robotgame.ui.partida.ConfiguracaoPartida.ObstaculoColocado;
import robotgame.ui.partida.ConfiguracaoPartida.TipoObstaculo;
import robotgame.ui.partida.CoresPacman;
import robotgame.ui.theme.Tema;

/** Modo 4: o jogador espalha fantasmas e rochas antes da simulação. */
public class CenarioScreen implements Tela {

    private final Navegador navegador;
    private final ConfiguracaoPartida config;
    private final Tabuleiro tabuleiro;
    private final BoardView board = new BoardView(136, false);
    private final MessageBar mensagem = new MessageBar(592);
    private final VBox raiz;

    private final HBox cartaoFantasma;
    private final HBox cartaoRocha;
    private final StatTile contagemFantasmas = new StatTile("NO CENÁRIO", "0", 15);
    private final StatTile contagemRochas = new StatTile("NO CENÁRIO", "0", 15);
    private final Label proximoFantasma = Ui.label("", 14.5, "muted");
    private final StackPane iconeFantasma = new StackPane();

    private TipoObstaculo ferramenta = TipoObstaculo.FANTASMA;
    private int fantasmasColocados;

    public CenarioScreen(Navegador navegador, ConfiguracaoPartida config) {
        this.navegador = navegador;
        this.config = config;
        tabuleiro = new Tabuleiro(config.getXFruta(), config.getYFruta());
        restaurarObstaculos();

        board.setFruta(config.getXFruta(), config.getYFruta(), Sprites.fruta(config.getIndiceFruta(), 80));
        board.setRobos(config.criarRobos());
        board.carregarObstaculos(tabuleiro);
        board.setAoClicar(this::clicarCasa);
        board.setPrevia(this::previa);

        // 1 · ESCOLHA O QUE COLOCAR
        cartaoFantasma = ferramenta(iconeFantasma, "FANTASMA", proximoFantasma, contagemFantasmas,
                TipoObstaculo.FANTASMA);
        cartaoRocha = ferramenta(Sprites.view(Sprites.rocha(40), 40), "ROCHA",
                Ui.label("Bloqueia o caminho", 14.5, "muted"), contagemRochas, TipoObstaculo.ROCHA);
        Label apoio = Ui.label("Toque de novo numa casa ocupada para remover o que estiver nela.", 13.5, "muted");
        NeonCard escolha = new NeonCard("1 · ESCOLHA O QUE COLOCAR", cartaoFantasma, cartaoRocha, apoio);
        escolha.setSpacing(8);

        // O QUE CADA COISA FAZ
        HBox doisPacmans = new HBox(-6);
        for (var robo : config.criarRobos()) {
            doisPacmans.getChildren().add(Sprites.pacman(CoresPacman.pacman(robo.getCor()), 18));
        }
        doisPacmans.setAlignment(Pos.CENTER);
        LegendGrid legenda = new LegendGrid(1)
                .item(Sprites.view(Sprites.fantasma("azul", 22), 22), "Fantasma",
                        "elimina o pacman que encostar nele; depois some do tabuleiro")
                .item(Sprites.view(Sprites.rocha(22), 22), "Rocha", "faz o pacman voltar para a casa anterior")
                .item(Sprites.view(Sprites.fruta(config.getIndiceFruta(), 22), 22), "Fruta", "o objetivo da partida")
                .item(doisPacmans, "Pacman 1 e 2", "começam juntos em (0, 0)");
        legenda.setVgap(6);
        NeonCard cartaoLegenda = new NeonCard("O QUE CADA COISA FAZ", legenda);
        LayoutJogo.crescer(cartaoLegenda);
        LayoutJogo.recortar(cartaoLegenda);

        // Ações
        Button voltar = Ui.secundario("Voltar", 50);
        voltar.setGraphic(Icones.chevronEsquerda(Tema.TEXTO));
        voltar.setOnAction(e -> navegador.irParaMenu(config));
        Button limpar = Ui.secundario("Limpar tudo", 50);
        limpar.setGraphic(Icones.xis(Tema.TEXTO));
        limpar.setOnAction(e -> limparTudo());
        Button comecar = Ui.primario("COMEÇAR SIMULAÇÃO", "btn-verde", 64);
        comecar.setStyle("-fx-font-size: 14px;");
        comecar.setGraphic(Icones.play(Tema.FUNDO, 14));
        comecar.setOnAction(e -> navegador.irParaJogo(config));
        VBox acoes = new VBox(12, Ui.linhaIgual(14, voltar, limpar), comecar);
        acoes.getStyleClass().add("neon-card");
        acoes.setPadding(new Insets(14, 18, 18, 18));

        VBox painel = LayoutJogo.painel(12, escolha, cartaoLegenda, acoes);
        raiz = LayoutJogo.montar(config.getModo(), StepperBar.Passo.CENARIO, board, mensagem, painel);
        atualizar();
    }

    @Override
    public Parent getRaiz() {
        return raiz;
    }

    // Recoloca o que já estava na configuração (ex.: voltou do menu); descarta o que o tabuleiro recusar
    // (por exemplo, se a fruta foi para uma casa que tinha obstáculo).
    private void restaurarObstaculos() {
        Iterator<ObstaculoColocado> it = config.getObstaculos().iterator();
        while (it.hasNext()) {
            ObstaculoColocado o = it.next();
            try {
                tabuleiro.adicionarObstaculo(criar(o.tipo(), o.cor()), o.x(), o.y());
                if (o.tipo() == TipoObstaculo.FANTASMA) {
                    fantasmasColocados++;
                }
            } catch (IllegalArgumentException e) {
                it.remove();
            }
        }
    }

    private HBox ferramenta(Node icone, String titulo, Label descricao, StatTile contagem, TipoObstaculo tipo) {
        StackPane caixaIcone = new StackPane(icone);
        caixaIcone.getStyleClass().add("tile");
        Ui.tamanhoFixo(caixaIcone, 52, 52);
        VBox textos = new VBox(8, Ui.label(titulo, 12, "pixel", "amarelo"), descricao);
        textos.setAlignment(Pos.CENTER_LEFT);
        contagem.setPrefWidth(106);
        contagem.setMaxHeight(58);
        HBox cartao = Ui.linha(16, caixaIcone, textos, Ui.espaco(), contagem);
        cartao.getStyleClass().add("inner-card");
        cartao.setPadding(new Insets(0, 16, 0, 16));
        Ui.tamanhoFixo(cartao, LayoutJogo.LARGURA_PAINEL - 40, 84);
        cartao.setOnMouseClicked(e -> {
            ferramenta = tipo;
            atualizar();
        });
        return cartao;
    }

    private String proximaCorFantasma() {
        return CoresPacman.ORDEM_FANTASMAS.get(fantasmasColocados % CoresPacman.ORDEM_FANTASMAS.size());
    }

    private static Obstaculo criar(TipoObstaculo tipo, String cor) {
        return tipo == TipoObstaculo.FANTASMA ? new Fantasma(cor) : new Rocha();
    }

    // Mesmas regras da interface anterior: casa ocupada → remove; senão tenta colocar e o Tabuleiro valida.
    private void clicarCasa(int x, int y) {
        if (tabuleiro.getObstaculo(x, y) != null) {
            tabuleiro.removerObstaculo(x, y);
            config.getObstaculos().removeIf(o -> o.x() == x && o.y() == y);
        } else {
            String cor = ferramenta == TipoObstaculo.FANTASMA ? proximaCorFantasma() : null;
            try {
                tabuleiro.adicionarObstaculo(criar(ferramenta, cor), x, y);
            } catch (IllegalArgumentException e) {
                mensagem.mostrar("AVISO", e.getMessage(), Tema.VERMELHO);
                return;
            }
            config.getObstaculos().add(new ObstaculoColocado(ferramenta, cor, x, y));
            if (ferramenta == TipoObstaculo.FANTASMA) {
                fantasmasColocados++;
            }
        }
        board.carregarObstaculos(tabuleiro);
        atualizar();
    }

    private Image previa(int x, int y) {
        boolean livre = tabuleiro.getObstaculo(x, y) == null
                && !(x == 0 && y == 0)
                && !(x == tabuleiro.getXAlimento() && y == tabuleiro.getYAlimento());
        if (!livre) {
            return null;
        }
        return ferramenta == TipoObstaculo.FANTASMA
                ? Sprites.fantasma(proximaCorFantasma(), 78)
                : Sprites.rocha(78);
    }

    private void limparTudo() {
        for (int x = 0; x < Tabuleiro.TAMANHO; x++) {
            for (int y = 0; y < Tabuleiro.TAMANHO; y++) {
                tabuleiro.removerObstaculo(x, y);
            }
        }
        config.getObstaculos().clear();
        fantasmasColocados = 0;
        board.carregarObstaculos(tabuleiro);
        atualizar();
    }

    private void atualizar() {
        long fantasmas = config.getObstaculos().stream().filter(o -> o.tipo() == TipoObstaculo.FANTASMA).count();
        contagemFantasmas.setValor(String.valueOf(fantasmas));
        contagemRochas.setValor(String.valueOf(config.getObstaculos().size() - fantasmas));
        String cor = proximaCorFantasma();
        proximoFantasma.setText("Próximo fantasma: " + cor);
        iconeFantasma.getChildren().setAll(Sprites.view(Sprites.fantasma(cor, 34), 34));
        cartaoFantasma.getStyleClass().remove("selected-glow");
        cartaoRocha.getStyleClass().remove("selected-glow");
        (ferramenta == TipoObstaculo.FANTASMA ? cartaoFantasma : cartaoRocha).getStyleClass().add("selected-glow");
        mensagem.mostrar("DICA", ferramenta == TipoObstaculo.FANTASMA
                ? "Toque numa casa para colocar um fantasma."
                : "Toque numa casa para colocar uma rocha.");
    }
}
