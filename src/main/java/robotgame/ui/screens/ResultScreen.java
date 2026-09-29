package robotgame.ui.screens;

import java.util.EnumSet;
import java.util.Set;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import robotgame.modelo.EventoPartida;
import robotgame.modelo.ModoJogo;
import robotgame.modelo.Robo;
import robotgame.modelo.RoboInteligente;
import robotgame.modelo.Tabuleiro;
import robotgame.ui.Sons;
import robotgame.ui.components.BoardView;
import robotgame.ui.components.DiaryList;
import robotgame.ui.components.Icones;
import robotgame.ui.components.KeyCap;
import robotgame.ui.components.MessageBar;
import robotgame.ui.components.NeonCard;
import robotgame.ui.components.ScoreCard;
import robotgame.ui.components.Sprites;
import robotgame.ui.components.StepperBar;
import robotgame.ui.components.Ui;
import robotgame.ui.partida.CoresPacman;
import robotgame.ui.partida.DiarioPartida;
import robotgame.ui.partida.ResultadoPartida;
import robotgame.ui.theme.Tema;

/** Fim de jogo: tabuleiro final com a faixa "FIM DE JOGO", vencedor, placares e como terminou. */
public class ResultScreen implements Tela {

    private static final Set<EventoPartida.Tipo> RELEVANTES = EnumSet.of(EventoPartida.Tipo.ROCHA,
            EventoPartida.Tipo.EXPLODIU, EventoPartida.Tipo.FANTASMA_SUMIU, EventoPartida.Tipo.ACHOU_ALIMENTO);

    private final Navegador navegador;
    private final ResultadoPartida resultado;
    private final ModoJogo modo;
    private final Robo[] robos;
    private final Sons sons;
    private final DiaryList comoTerminou;
    private final Button botaoSom;
    private final VBox raiz;

    // Textos calculados a partir do estado final
    private String titulo;
    private String detalhe;
    private String faixa;
    private String rodape;
    private Robo destaque;

    public ResultScreen(Navegador navegador, ResultadoPartida resultado) {
        this.navegador = navegador;
        this.resultado = resultado;
        this.modo = resultado.configuracao().getModo();
        this.robos = resultado.robos();
        this.sons = navegador.getSons();
        calcularTextos();

        Tabuleiro tabuleiro = resultado.tabuleiro();
        BoardView board = new BoardView(136, false);
        board.setFruta(tabuleiro.getXAlimento(), tabuleiro.getYAlimento(),
                Sprites.fruta(resultado.configuracao().getIndiceFruta(), 80));
        board.carregarObstaculos(tabuleiro);
        for (int codigo : resultado.explosoes()) {
            board.mostrarExplosao(codigo / Tabuleiro.TAMANHO, codigo % Tabuleiro.TAMANHO);
        }
        board.setRobos(robos);
        for (Robo r : robos) {
            if (resultado.achou(r)) {
                board.setVencedor(r.getX(), r.getY());
            }
        }
        board.getSobreposicao().getChildren().add(sobreposicaoFim());

        MessageBar mensagem = new MessageBar(592);
        mensagem.mostrar("FIM", rodape);

        // Painel direito
        NeonCard vencedor = new NeonCard(null);
        Node icone = destaque != null
                ? Sprites.pacman(CoresPacman.pacman(destaque.getCor()), 64)
                : Sprites.view(Sprites.fantasma("azul", 60), 60);
        Label tituloVencedor = Ui.label(titulo, 15, "pixel", "amarelo");
        Label detalheVencedor = Ui.label(detalhe, 15, "semi");
        detalheVencedor.setWrapText(true);
        VBox textos = new VBox(12, tituloVencedor, detalheVencedor);
        textos.setAlignment(Pos.CENTER_LEFT);
        HBox linhaVencedor = Ui.linha(22, icone, textos);
        vencedor.getChildren().add(linhaVencedor);
        vencedor.setAlignment(Pos.CENTER_LEFT);
        vencedor.setPadding(new Insets(0, 22, 0, 22));
        Ui.tamanhoFixo(vencedor, LayoutJogo.LARGURA_PAINEL, 112);

        ScoreCard[] placares = new ScoreCard[robos.length];
        for (int i = 0; i < robos.length; i++) {
            Robo r = robos[i];
            String nome = modo.isManual() ? "PACMAN 1" : "PACMAN " + (i + 1);
            String sub = modo.isManual() ? "você controlou" : r instanceof RoboInteligente ? "inteligente" : "normal";
            placares[i] = new ScoreCard(CoresPacman.pacman(r.getCor()), nome, sub, 140, "VÁLIDOS", "INVÁLIDOS", "TOTAL");
            placares[i].setValores(String.valueOf(r.getMovimentosValidos()),
                    String.valueOf(r.getMovimentosInvalidos()), String.valueOf(r.getTotalMovimentos()));
            placares[i].setStatus(situacao(r), corSituacao(r));
        }
        Node linhaPlacares = Ui.linhaIgual(12, placares);

        // No modo manual quase tudo é movimento: mostra as 3 últimas jogadas.
        comoTerminou = new DiaryList(resultado.diario().getEntradas(), 3, modo.isManual()
                ? e -> e.evento() != null
                : e -> e.evento() != null && RELEVANTES.contains(e.evento().tipo()));
        NeonCard cartaoFim = new NeonCard("COMO TERMINOU", comoTerminou);
        LayoutJogo.crescer(cartaoFim);
        LayoutJogo.recortar(cartaoFim);

        Button jogarDeNovo = Ui.primario("JOGAR DE NOVO", "btn-verde", 64);
        jogarDeNovo.setStyle("-fx-font-size: 14px;");
        jogarDeNovo.setGraphic(Icones.recomecar(Tema.FUNDO));
        jogarDeNovo.setOnAction(e -> navegador.irParaJogo(resultado.configuracao()));
        Button voltar = Ui.secundario("Voltar ao menu", 52);
        voltar.setGraphic(Icones.chevronEsquerda(Tema.TEXTO));
        voltar.setOnAction(e -> navegador.irParaMenu(resultado.configuracao()));
        botaoSom = Ui.secundario(null, 52);
        botaoSom.setOnAction(e -> alternarSom());
        atualizarSom();
        VBox acoes = new VBox(14, jogarDeNovo, Ui.linhaIgual(14, voltar, botaoSom));
        acoes.getStyleClass().add("neon-card");
        acoes.setPadding(new Insets(16, 18, 18, 18));

        VBox painel = LayoutJogo.painel(12, vencedor, linhaPlacares, cartaoFim, acoes);
        raiz = LayoutJogo.montar(modo, StepperBar.Passo.FIM, board, mensagem, painel);
    }

    @Override
    public Parent getRaiz() {
        return raiz;
    }

    @Override
    public void aoTecla(KeyEvent evento) {
        if (evento.getCode() == KeyCode.M) {
            alternarSom();
            evento.consume();
        }
    }

    @Override
    public void aoSair() {
        comoTerminou.desligar();
        sons.pararTudo();
    }

    private void alternarSom() {
        sons.setMudo(!sons.isMudo());
        atualizarSom();
    }

    private void atualizarSom() {
        HBox conteudo = new HBox(8, Icones.som(Tema.TEXTO, !sons.isMudo()),
                Ui.label(sons.isMudo() ? "Som: desligado" : "Som: ligado", 15, "bold"), new KeyCap("M", true));
        conteudo.setAlignment(Pos.CENTER);
        botaoSom.setGraphic(conteudo);
    }

    private Node sobreposicaoFim() {
        Region escuro = new Region();
        escuro.setStyle("-fx-background-color: rgba(5,5,20,0.72); -fx-background-radius: 22;");
        escuro.setMouseTransparent(true);

        Label estrela1 = Ui.label("★", 24, "pixel", "amarelo");
        Label estrela2 = Ui.label("★", 24, "pixel", "amarelo");
        HBox linhaTitulo = new HBox(22, estrela1, Ui.label("FIM DE JOGO", "fim-titulo"), estrela2);
        linhaTitulo.setAlignment(Pos.CENTER);
        VBox banda = new VBox(20, linhaTitulo, Ui.label(faixa, "fim-sub"));
        banda.getStyleClass().add("fim-faixa");
        banda.setAlignment(Pos.CENTER);
        banda.setPadding(new Insets(34, 0, 30, 0));
        banda.setMaxHeight(Region.USE_PREF_SIZE);
        banda.setTranslateY(-60);

        StackPane camada = new StackPane(escuro, banda);
        camada.setPadding(new Insets(5));
        return camada;
    }

    // ------------------------------------------------------------------ textos por modo

    private void calcularTextos() {
        Tabuleiro t = resultado.tabuleiro();
        DiarioPartida diario = resultado.diario();
        String fruta = DiarioPartida.pos(t.getXAlimento(), t.getYAlimento());

        if (modo.isManual()) {
            Robo r = robos[0];
            destaque = r;
            titulo = "VOCÊ ACHOU A FRUTA!";
            detalhe = "Chegou à fruta em " + fruta + " com " + r.getTotalMovimentos() + " movimentos.";
            faixa = "Você achou a fruta!";
            rodape = "Você achou a fruta em " + r.getTotalMovimentos() + " movimentos.";
            return;
        }

        if (modo == ModoJogo.NORMAL_X_INTELIGENTE && resultado.achou(robos[0]) && resultado.achou(robos[1])) {
            int m1 = robos[0].getTotalMovimentos();
            int m2 = robos[1].getTotalMovimentos();
            faixa = "Os dois acharam a fruta!";
            if (m1 == m2) {
                destaque = null;
                titulo = "EMPATE!";
                detalhe = "Os dois chegaram à fruta em " + fruta + " com " + m1 + " movimentos.";
                rodape = "Empate: os dois precisaram de " + m1 + " movimentos.";
            } else {
                destaque = m1 < m2 ? robos[0] : robos[1];
                Robo outro = destaque == robos[0] ? robos[1] : robos[0];
                titulo = diario.nome(destaque).toUpperCase() + " FOI MAIS RÁPIDO!";
                detalhe = "Achou a fruta com " + destaque.getTotalMovimentos() + " movimentos; o "
                        + diario.nome(outro) + " precisou de " + outro.getTotalMovimentos() + ".";
                rodape = diario.nome(destaque) + " foi mais rápido: " + destaque.getTotalMovimentos()
                        + " × " + outro.getTotalMovimentos() + " movimentos.";
            }
            return;
        }

        for (Robo r : robos) {
            if (resultado.achou(r)) {
                destaque = r;
                String nome = diario.nome(r);
                titulo = nome.toUpperCase() + " VENCEU!";
                detalhe = "Chegou à fruta em " + fruta + " com " + r.getTotalMovimentos() + " movimentos.";
                faixa = nome + " achou a fruta!";
                rodape = nome + " achou a fruta em " + r.getTotalMovimentos() + " movimentos.";
                return;
            }
        }

        boolean todosExplodiram = true;
        for (Robo r : robos) {
            todosExplodiram &= !r.isAtivo();
        }
        destaque = null;
        titulo = "NINGUÉM ACHOU A FRUTA";
        detalhe = todosExplodiram ? "Os dois pacmans explodiram nos fantasmas." : resultado.mensagem();
        faixa = "Ninguém achou a fruta";
        rodape = todosExplodiram ? "Os dois pacmans foram eliminados." : resultado.mensagem();
    }

    private String situacao(Robo r) {
        if (!r.isAtivo()) {
            return "EXPLODIU";
        }
        if (resultado.achou(r)) {
            return r == destaque && modo == ModoJogo.NORMAL_X_INTELIGENTE ? "VENCEU" : "ACHOU";
        }
        return "NÃO ACHOU";
    }

    private Color corSituacao(Robo r) {
        if (!r.isAtivo()) {
            return Tema.VERMELHO;
        }
        if (resultado.achou(r)) {
            return r == destaque && modo == ModoJogo.NORMAL_X_INTELIGENTE ? Tema.AMARELO : Tema.VERDE;
        }
        return Tema.TEXTO_SECUNDARIO;
    }
}
