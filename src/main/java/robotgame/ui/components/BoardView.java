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
import javafx.scene.effect.DropShadow;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import robotgame.modelo.Robo;
import robotgame.modelo.Tabuleiro;
import robotgame.ui.partida.CoresPacman;
import robotgame.ui.theme.Tema;

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
    private StackPane fantasmaInteligente;
    private TranslateTransition passoFantasma;
    private CellView casaPoder;
    private Animation piscarPoder;

    public BoardView(double celula, boolean compacto) {
        this.celula = celula;
        getStyleClass().add("board-frame");
        if (compacto) {
            getStyleClass().add("compacto");
        }
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

    public void setFruta(int x, int y, Image imagem) {
        for (CellView[] coluna : casas) {
            for (CellView casa : coluna) {
                casa.setFruta(null);
            }
        }
        casas[x][y].setFruta(imagem);
    }

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

    public void setAoClicar(BiConsumer<Integer, Integer> aoClicar) {
        this.aoClicar = aoClicar;
        for (CellView[] coluna : casas) {
            for (CellView casa : coluna) {
                casa.setCursor(aoClicar != null ? Cursor.HAND : Cursor.DEFAULT);
            }
        }
    }

    public void setPrevia(BiFunction<Integer, Integer, Image> previa) {
        this.previa = previa;
    }

    private void atualizarPrevia(CellView casa, boolean dentro) {
        if (previa == null) {
            return;
        }
        casa.setPrevia(dentro ? previa.apply(casa.getX(), casa.getY()) : null);
    }

    public void setRobos(Robo[] robos) {
        this.robos = robos;
        camadaPacman.getChildren().clear();
        if (fantasmaInteligente != null) {
            camadaPacman.getChildren().add(fantasmaInteligente);
        }
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
                double dx = 0;
                double dy = 0;
                if (grupo.size() == 2) {
                    dx = (i == 0 ? -0.25 : 0.25) * celula;
                } else if (grupo.size() >= 3) {
                    // Três na mesma casa: dois embaixo e um em cima, para nenhum sumir atrás do outro.
                    double[][] posicoes = {{-0.22, 0.1}, {0.22, 0.1}, {0, -0.18}};
                    dx = posicoes[i % 3][0] * celula;
                    dy = posicoes[i % 3][1] * celula;
                }
                no.encolher(grupo.size());
                no.mover(r.getX() * celula + dx, (N - 1 - r.getY()) * celula + dy, duracao);
            }
        }
        for (PacmanNode no : pacmans.values()) {
            no.setVisible(visivel(no.robo));
            no.setPoder(no.robo.isAtivo() && no.robo.isComPoder());
        }
    }

    private boolean visivel(Robo r) {
        return r.isAtivo() || morrendo.contains(r);
    }

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

    public void setItemPoder(int x, int y) {
        removerItemPoder();
        casaPoder = casas[x][y];
        casaPoder.setItemPoder(true);
        FadeTransition piscar = new FadeTransition(Duration.millis(450), casaPoder.getBolinhaPoder());
        piscar.setFromValue(1);
        piscar.setToValue(0.25);
        piscar.setCycleCount(Animation.INDEFINITE);
        piscar.setAutoReverse(true);
        piscarPoder = piscar;
        tocar(piscar);
    }

    public void removerItemPoder() {
        if (piscarPoder != null) {
            piscarPoder.stop();
            animacoes.remove(piscarPoder);
            piscarPoder = null;
        }
        if (casaPoder != null) {
            casaPoder.getBolinhaPoder().setOpacity(1);
            casaPoder.setItemPoder(false);
            casaPoder = null;
        }
    }

    public void setFantasmaInteligente(int x, int y) {
        if (fantasmaInteligente == null) {
            fantasmaInteligente = new StackPane(Sprites.fantasmaInteligente(celula * 0.57));
            Ui.tamanhoFixo(fantasmaInteligente, celula, celula);
            // Fica embaixo dos pacmans: quem tem poder aparece "comendo" o fantasma.
            camadaPacman.getChildren().add(0, fantasmaInteligente);
        }
        if (passoFantasma != null) {
            passoFantasma.stop();
            animacoes.remove(passoFantasma);
            passoFantasma = null;
        }
        fantasmaInteligente.setScaleX(1);
        fantasmaInteligente.setScaleY(1);
        fantasmaInteligente.setOpacity(1);
        fantasmaInteligente.setTranslateX(x * celula);
        fantasmaInteligente.setTranslateY((N - 1 - y) * celula);
    }

    public void moverFantasmaInteligente(int x, int y, Duration duracao) {
        if (fantasmaInteligente == null) {
            setFantasmaInteligente(x, y);
            return;
        }
        if (passoFantasma != null) {
            passoFantasma.stop();
            animacoes.remove(passoFantasma);
        }
        TranslateTransition passo = new TranslateTransition(duracao, fantasmaInteligente);
        passo.setToX(x * celula);
        passo.setToY((N - 1 - y) * celula);
        passo.setInterpolator(Interpolator.EASE_BOTH);
        passoFantasma = passo;
        tocar(passo);
    }

    public void removerFantasmaInteligente(boolean animado) {
        if (fantasmaInteligente == null) {
            return;
        }
        StackPane fantasma = fantasmaInteligente;
        fantasmaInteligente = null;
        if (passoFantasma != null) {
            passoFantasma.stop();
            animacoes.remove(passoFantasma);
            passoFantasma = null;
        }
        if (!animado) {
            camadaPacman.getChildren().remove(fantasma);
            return;
        }
        ScaleTransition encolher = new ScaleTransition(Duration.millis(450), fantasma);
        encolher.setToX(0);
        encolher.setToY(0);
        FadeTransition sumir = new FadeTransition(Duration.millis(450), fantasma);
        sumir.setToValue(0);
        ParallelTransition comido = new ParallelTransition(encolher, sumir);
        comido.setOnFinished(e -> camadaPacman.getChildren().remove(fantasma));
        tocar(comido);
    }

    public boolean temAnimacaoDeMorte() {
        return !morrendo.isEmpty();
    }

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

    private final class PacmanNode extends StackPane {

        final Robo robo;
        final StackPane corpo;
        final ImageView sprite;
        final Image aberto;
        final Image fechado;
        final Circle halo;
        TranslateTransition deslize;
        Animation pulso;
        double escala = 1;

        PacmanNode(Robo robo, int numero) {
            this.robo = robo;
            double tamanho = celula * 0.545;
            aberto = Sprites.pacmanAberto(tamanho);
            fechado = Sprites.pacmanFechado(tamanho);
            sprite = Sprites.view(bocaAberta ? aberto : fechado, tamanho);
            Color cor = CoresPacman.pacman(robo.getCor());
            Sprites.tingir(sprite, cor, tamanho);
            halo = new Circle(tamanho * 0.62, Color.TRANSPARENT);
            halo.setStroke(cor);
            halo.setStrokeWidth(Math.max(2, tamanho * 0.06));
            halo.setEffect(new DropShadow(tamanho * 0.35, cor));
            halo.setVisible(false);
            // Fora do layout: o halo é maior que o sprite e não pode empurrar o número do pacman.
            halo.setManaged(false);

            double diametro = Math.max(16, celula * 0.16);
            Label badge = Ui.label(String.valueOf(numero), "pixel");
            badge.setAlignment(Pos.CENTER);
            badge.setStyle("-fx-font-size: " + Math.round(diametro * 0.45) + "px; -fx-text-fill: #050514;"
                    + "-fx-background-color: " + Tema.hex(cor) + "; -fx-background-radius: 50%;"
                    + "-fx-padding: 1 0 0 1;");
            Ui.tamanhoFixo(badge, diametro, diametro);

            StackPane caixaSprite = new StackPane(halo, sprite);
            // A VBox estica a caixa na largura; o sprite fica no centro dela, então o halo também.
            halo.layoutXProperty().bind(caixaSprite.widthProperty().divide(2));
            halo.layoutYProperty().bind(caixaSprite.heightProperty().divide(2));
            VBox pilha = new VBox(-diametro * 0.15, caixaSprite, badge);
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

        void encolher(int naCasa) {
            double nova = naCasa <= 1 ? 1 : naCasa == 2 ? 54.0 / 74.0 : 0.6;
            if (nova == escala) {
                return;
            }
            escala = nova;
            ScaleTransition t = new ScaleTransition(Duration.millis(120), this);
            t.setToX(nova);
            t.setToY(nova);
            tocar(t);
        }

        // Halo pulsando enquanto o pacman tem poder.
        void setPoder(boolean comPoder) {
            if (comPoder == (pulso != null)) {
                return;
            }
            if (!comPoder) {
                pulso.stop();
                animacoes.remove(pulso);
                pulso = null;
                halo.setVisible(false);
                return;
            }
            halo.setVisible(true);
            ScaleTransition crescer = new ScaleTransition(Duration.millis(500), halo);
            crescer.setFromX(0.9);
            crescer.setFromY(0.9);
            crescer.setToX(1.15);
            crescer.setToY(1.15);
            FadeTransition brilhar = new FadeTransition(Duration.millis(500), halo);
            brilhar.setFromValue(1);
            brilhar.setToValue(0.35);
            ParallelTransition p = new ParallelTransition(crescer, brilhar);
            p.setCycleCount(Animation.INDEFINITE);
            p.setAutoReverse(true);
            pulso = p;
            tocar(p);
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
