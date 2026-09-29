package robotgame.ui.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import robotgame.modelo.Robo;
import robotgame.modelo.Tabuleiro;
import robotgame.ui.partida.CoresPacman;
import robotgame.ui.theme.Tema;

/**
 * Tabuleiro 4×4 com moldura dupla, eixos e as camadas: casas ({@link CellView}), pacmans (animados por
 * translação) e sobreposições (START, PAUSADO, FIM DE JOGO). Só desenha: quem decide é o jogo.
 */
public class BoardView extends StackPane {

    private static final int N = Tabuleiro.TAMANHO;
    private static final Duration DURACAO_MORTE = Duration.millis(2000);
    public static final double EIXO = 20;

    private final double celula;
    private final CellView[][] casas = new CellView[N][N];
    private final Pane camadaPacman = new Pane();
    private final StackPane sobreposicao = new StackPane();
    private final Map<Robo, PacmanNode> pacmans = new LinkedHashMap<>();
    private final Set<Robo> morrendo = new HashSet<>();
    private final Set<Integer> explosoes = new HashSet<>();
    private final List<Animation> animacoes = new ArrayList<>();
    private Robo[] robos = new Robo[0];
    private boolean bocaAberta = true;
    private boolean pausado;
    private BiConsumer<Integer, Integer> aoClicar;
    private BiFunction<Integer, Integer, Image> previa;

    public BoardView(double celula, boolean compacto) {
        this.celula = celula;
        getStyleClass().add("board-frame");
        if (compacto) {
            getStyleClass().add("compacto");
        }
        // "borda dupla 6 + padding 8": o Region já desconta a borda dupla (6) dos insets.
        double margem = compacto ? 6 : 8;
        double borda = 6;
        double lado = N * celula;

        GridPane grade = new GridPane();
        grade.setStyle("-fx-border-color: " + Tema.hex(Tema.CELULA_BORDA) + "; -fx-border-width: 1 0 0 1;");
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                CellView casa = new CellView(x, y, celula, !compacto);
                casas[x][y] = casa;
                int cx = x;
                int cy = y;
                casa.setOnMouseClicked(e -> {
                    if (aoClicar != null) {
                        aoClicar.accept(cx, cy);
                        atualizarPrevia(casa, true);
                    }
                });
                casa.setOnMouseEntered(e -> atualizarPrevia(casa, true));
                casa.setOnMouseExited(e -> atualizarPrevia(casa, false));
                grade.add(casa, x, N - 1 - y);
            }
        }

        camadaPacman.setMouseTransparent(true);
        camadaPacman.setPrefSize(lado, lado);
        StackPane area = new StackPane(grade, camadaPacman);
        area.setAlignment(Pos.TOP_LEFT);
        Ui.tamanhoFixo(area, lado + 1, lado + 1);

        Pattern listras = new Pattern();
        VBox numerosY = new VBox();
        for (int y = N - 1; y >= 0; y--) {
            Label l = Ui.label(String.valueOf(y), "axis-label");
            Ui.tamanhoFixo(l, EIXO - 6, celula);
            l.setAlignment(Pos.CENTER);
            numerosY.getChildren().add(l);
        }
        Rectangle faixaY = new Rectangle(6, lado + 1, listras.padrao);
        StackPane eixoY = new StackPane(numerosY, faixaY);
        StackPane.setAlignment(numerosY, Pos.CENTER_LEFT);
        StackPane.setAlignment(faixaY, Pos.TOP_RIGHT);
        Ui.tamanhoFixo(eixoY, EIXO, lado + 1);

        HBox numerosX = new HBox();
        for (int x = 0; x < N; x++) {
            Label l = Ui.label(String.valueOf(x), "axis-label");
            Ui.tamanhoFixo(l, celula, EIXO - 6);
            l.setAlignment(Pos.CENTER);
            numerosX.getChildren().add(l);
        }
        Rectangle faixaX = new Rectangle(lado + 1, 6, listras.padrao);
        StackPane eixoX = new StackPane(numerosX, faixaX);
        numerosX.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(numerosX, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(faixaX, Pos.TOP_LEFT);
        Ui.tamanhoFixo(eixoX, lado + 1, EIXO);
        Rectangle canto = new Rectangle(6, 6, listras.padrao);
        StackPane cantoPane = new StackPane(canto);
        StackPane.setAlignment(canto, Pos.TOP_RIGHT);

        GridPane conteudo = new GridPane();
        conteudo.add(eixoY, 0, 0);
        conteudo.add(area, 1, 0);
        conteudo.add(cantoPane, 0, 1);
        conteudo.add(eixoX, 1, 1);
        conteudo.setPadding(new Insets(margem));
        conteudo.setAlignment(Pos.CENTER);

        sobreposicao.setPickOnBounds(false);
        getChildren().addAll(conteudo, sobreposicao);
        double total = lado + 1 + EIXO + 2 * (margem + borda);
        Ui.tamanhoFixo(this, total, total);
    }

    // ---------------------------------------------------------------- conteúdo fixo

    public void setFruta(int x, int y, Image imagem) {
        for (CellView[] coluna : casas) {
            for (CellView casa : coluna) {
                casa.setFruta(null);
            }
        }
        casas[x][y].setFruta(imagem);
    }

    /** Copia os obstáculos atuais do tabuleiro para as casas. */
    public void carregarObstaculos(Tabuleiro tabuleiro) {
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                casas[x][y].setObstaculo(tabuleiro.getObstaculo(x, y));
            }
        }
    }

    public void mostrarExplosao(int x, int y) {
        explosoes.add(x * N + y);
        casas[x][y].setObstaculo(null);
        casas[x][y].setExplosao(true);
    }

    public void animarFantasmaSumindo(int x, int y) {
        explosoes.add(x * N + y);
        casas[x][y].animarExplosao(this::tocar);
    }

    public Set<Integer> getExplosoes() {
        return Set.copyOf(explosoes);
    }

    public void setVencedor(int x, int y) {
        casas[x][y].setVencedor(true);
    }

    public StackPane getSobreposicao() {
        return sobreposicao;
    }

    /** Clique numa casa (cenário e mini-tabuleiro). */
    public void setAoClicar(BiConsumer<Integer, Integer> aoClicar) {
        this.aoClicar = aoClicar;
        for (CellView[] coluna : casas) {
            for (CellView casa : coluna) {
                casa.setCursor(aoClicar != null ? Cursor.HAND : Cursor.DEFAULT);
            }
        }
    }

    /** Imagem de prévia ao passar o mouse numa casa (nulo = sem prévia). */
    public void setPrevia(BiFunction<Integer, Integer, Image> previa) {
        this.previa = previa;
    }

    private void atualizarPrevia(CellView casa, boolean dentro) {
        if (previa == null) {
            return;
        }
        casa.setPrevia(dentro ? previa.apply(casa.getX(), casa.getY()) : null);
    }

    // ---------------------------------------------------------------- pacmans

    public void setRobos(Robo[] robos) {
        this.robos = robos;
        camadaPacman.getChildren().clear();
        pacmans.clear();
        for (int i = 0; i < robos.length; i++) {
            PacmanNode no = new PacmanNode(robos[i], i + 1);
            pacmans.put(robos[i], no);
            camadaPacman.getChildren().add(no);
        }
        sincronizar(Duration.ZERO);
    }

    public void setDirecao(Robo robo, String direcao) {
        PacmanNode no = pacmans.get(robo);
        if (no != null) {
            no.apontar(direcao);
        }
    }

    public void setBoca(boolean aberta) {
        bocaAberta = aberta;
        for (PacmanNode no : pacmans.values()) {
            if (!morrendo.contains(no.robo)) {
                no.sprite.setImage(aberta ? no.aberto : no.fechado);
            }
        }
    }

    /**
     * Leva cada pacman visível para a casa atual do seu robô (deslizando por {@code duracao}) e
     * atualiza o destaque das casas.
     */
    public void sincronizar(Duration duracao) {
        Map<Integer, List<Robo>> porCasa = new LinkedHashMap<>();
        for (Robo r : robos) {
            if (visivel(r)) {
                porCasa.computeIfAbsent(r.getX() * N + r.getY(), k -> new ArrayList<>()).add(r);
            }
        }
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                List<Robo> aqui = porCasa.get(x * N + y);
                Color cor = null;
                if (aqui != null) {
                    List<Robo> vivos = aqui.stream().filter(Robo::isAtivo).toList();
                    cor = vivos.isEmpty() ? null
                            : vivos.size() == 1 ? CoresPacman.pacman(vivos.get(0).getCor())
                            : Tema.TEXTO_SECUNDARIO;
                }
                casas[x][y].setCorPacman(cor);
            }
        }
        for (List<Robo> grupo : porCasa.values()) {
            for (int i = 0; i < grupo.size(); i++) {
                Robo r = grupo.get(i);
                PacmanNode no = pacmans.get(r);
                double deslocamento = grupo.size() == 1 ? 0 : (i == 0 ? -0.25 : 0.25) * celula;
                no.encolher(grupo.size() > 1);
                no.mover(r.getX() * celula + deslocamento, (N - 1 - r.getY()) * celula, duracao);
            }
        }
        for (PacmanNode no : pacmans.values()) {
            no.setVisible(visivel(no.robo));
        }
    }

    private boolean visivel(Robo r) {
        return r.isAtivo() || morrendo.contains(r);
    }

    /** Tremida horizontal curta: movimento inválido. */
    public void tremer(Robo robo) {
        PacmanNode no = pacmans.get(robo);
        if (no == null) {
            return;
        }
        TranslateTransition t = new TranslateTransition(Duration.millis(50), no.corpo);
        t.setFromX(0);
        t.setByX(celula * 0.06);
        t.setCycleCount(5);
        t.setAutoReverse(true);
        t.setOnFinished(e -> no.corpo.setTranslateX(0));
        tocar(t);
    }

    /** Avança um pouco na direção da rocha e volta. */
    public void baterNaRocha(Robo robo, String direcao, Duration duracao) {
        PacmanNode no = pacmans.get(robo);
        if (no == null) {
            return;
        }
        double d = celula * 0.3;
        TranslateTransition t = new TranslateTransition(duracao.divide(2), no.corpo);
        t.setFromX(0);
        t.setFromY(0);
        t.setToX("right".equals(direcao) ? d : "left".equals(direcao) ? -d : 0);
        t.setToY("up".equals(direcao) ? -d : "down".equals(direcao) ? d : 0);
        t.setCycleCount(2);
        t.setAutoReverse(true);
        t.setOnFinished(e -> {
            no.corpo.setTranslateX(0);
            no.corpo.setTranslateY(0);
        });
        tocar(t);
    }

    /** O robô vai até a casa do fantasma e some girando (mesma animação de morte de antes). */
    public void explodir(Robo robo, Duration deslize, Runnable depois) {
        PacmanNode no = pacmans.get(robo);
        if (no == null) {
            return;
        }
        morrendo.add(robo);
        sincronizar(deslize);
        no.sprite.setImage(no.aberto);
        ScaleTransition escala = new ScaleTransition(DURACAO_MORTE, no.corpo);
        escala.setToX(0);
        escala.setToY(0);
        RotateTransition giro = new RotateTransition(DURACAO_MORTE, no.corpo);
        giro.setByAngle(720);
        FadeTransition fade = new FadeTransition(DURACAO_MORTE, no.corpo);
        fade.setToValue(0);
        fade.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition morte = new ParallelTransition(escala, giro, fade);
        morte.setDelay(deslize);
        morte.setOnFinished(e -> {
            morrendo.remove(robo);
            no.setVisible(false);
            if (depois != null) {
                depois.run();
            }
        });
        tocar(morte);
    }

    public boolean temAnimacaoDeMorte() {
        return !morrendo.isEmpty();
    }

    // ---------------------------------------------------------------- animações

    private void tocar(Animation animacao) {
        animacoes.add(animacao);
        EventoFim.encadear(animacao, () -> animacoes.remove(animacao));
        animacao.play();
        if (pausado) {
            animacao.pause();
        }
    }

    public void pausar() {
        pausado = true;
        for (Animation a : animacoes) {
            if (a.getStatus() == Animation.Status.RUNNING) {
                a.pause();
            }
        }
    }

    public void retomar() {
        pausado = false;
        for (Animation a : List.copyOf(animacoes)) {
            if (a.getStatus() == Animation.Status.PAUSED) {
                a.play();
            }
        }
    }

    public void pararAnimacoes() {
        for (Animation a : List.copyOf(animacoes)) {
            a.stop();
        }
        animacoes.clear();
    }

    // ---------------------------------------------------------------- nós internos

    private final class PacmanNode extends StackPane {

        final Robo robo;
        final StackPane corpo;
        final ImageView sprite;
        final Image aberto;
        final Image fechado;
        TranslateTransition deslize;
        boolean pequeno;

        PacmanNode(Robo robo, int numero) {
            this.robo = robo;
            double tamanho = celula * 0.545;
            aberto = Sprites.pacmanAberto(tamanho);
            fechado = Sprites.pacmanFechado(tamanho);
            sprite = Sprites.view(bocaAberta ? aberto : fechado, tamanho);
            Color cor = CoresPacman.pacman(robo.getCor());
            Sprites.tingir(sprite, cor, tamanho);

            double diametro = Math.max(16, celula * 0.16);
            Label badge = Ui.label(String.valueOf(numero), "pixel");
            badge.setAlignment(Pos.CENTER);
            badge.setStyle("-fx-font-size: " + Math.round(diametro * 0.45) + "px; -fx-text-fill: #050514;"
                    + "-fx-background-color: " + Tema.hex(cor) + "; -fx-background-radius: 50%;"
                    + "-fx-padding: 1 0 0 1;");
            Ui.tamanhoFixo(badge, diametro, diametro);

            VBox pilha = new VBox(-diametro * 0.15, new StackPane(sprite), badge);
            pilha.setAlignment(Pos.CENTER);
            corpo = new StackPane(pilha);
            getChildren().add(corpo);
            Ui.tamanhoFixo(this, celula, celula);
            setPadding(new Insets(diametro * 0.6, 0, 0, 0));
        }

        void apontar(String direcao) {
            sprite.setRotate(switch (direcao) {
                case "down" -> 90;
                case "up" -> 270;
                default -> 0;
            });
            sprite.setScaleX("left".equals(direcao) ? -1 : 1);
        }

        void encolher(boolean dividir) {
            if (dividir == pequeno) {
                return;
            }
            pequeno = dividir;
            double escala = dividir ? 54.0 / 74.0 : 1;
            ScaleTransition t = new ScaleTransition(Duration.millis(120), this);
            t.setToX(escala);
            t.setToY(escala);
            tocar(t);
        }

        void mover(double x, double y, Duration duracao) {
            if (deslize != null) {
                deslize.stop();
                animacoes.remove(deslize);
            }
            if (duracao.lessThanOrEqualTo(Duration.ZERO)
                    || (getTranslateX() == x && getTranslateY() == y)) {
                setTranslateX(x);
                setTranslateY(y);
                return;
            }
            deslize = new TranslateTransition(duracao, this);
            deslize.setToX(x);
            deslize.setToY(y);
            deslize.setInterpolator(Interpolator.EASE_BOTH);
            tocar(deslize);
        }
    }

    /** Listras vermelhas diagonais da faixa "Proibido" (fora do tabuleiro). */
    private static final class Pattern {

        final ImagePattern padrao;

        Pattern() {
            int t = 8;
            WritableImage img = new WritableImage(t, t);
            Color vermelho = Tema.VERMELHO;
            Color escuro = Color.web("#3a0f22");
            for (int y = 0; y < t; y++) {
                for (int x = 0; x < t; x++) {
                    img.getPixelWriter().setColor(x, y, ((x + y) % t) < t / 2 ? vermelho : escuro);
                }
            }
            padrao = new ImagePattern(img, 0, 0, t, t, false);
        }
    }

    /** Encadeia um onFinished sem perder o que a animação já tinha. */
    private static final class EventoFim {

        static void encadear(Animation a, Runnable extra) {
            var original = a.getOnFinished();
            a.setOnFinished(e -> {
                if (original != null) {
                    original.handle(e);
                }
                extra.run();
            });
        }
    }
}
