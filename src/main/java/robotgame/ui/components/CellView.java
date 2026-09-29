package robotgame.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import robotgame.modelo.Fantasma;
import robotgame.modelo.Obstaculo;
import robotgame.modelo.Rocha;
import robotgame.ui.partida.CoresPacman;
import robotgame.ui.theme.Tema;

/** Uma casa do tabuleiro: fundo, rótulo "(x,y)", bolinha e o conteúdo fixo (fruta, obstáculo, explosão). */
public class CellView extends StackPane {

    private final int x;
    private final int y;
    private final double tamanho;
    private final Region fundo = new Region();
    private final Circle bolinha;
    private final StackPane conteudo = new StackPane();
    private final StackPane previa = new StackPane();

    private Image fruta;
    private Obstaculo obstaculo;
    private boolean explosao;
    private Color corPacman;
    private boolean vencedor;

    public CellView(int x, int y, double tamanho, boolean mostrarCoordenada) {
        this.x = x;
        this.y = y;
        this.tamanho = tamanho;
        Ui.tamanhoFixo(this, tamanho, tamanho);

        bolinha = new Circle(tamanho >= 100 ? 5 : 3.5, Tema.BOLINHA);
        bolinha.setOpacity(0.6);
        previa.setMouseTransparent(true);
        previa.setOpacity(0.55);
        getChildren().addAll(fundo, bolinha, conteudo, previa);

        if (mostrarCoordenada) {
            Label coord = Ui.label("(" + x + "," + y + ")", "cell-coord");
            StackPane.setAlignment(coord, Pos.TOP_LEFT);
            StackPane.setMargin(coord, new Insets(6, 0, 0, 9));
            coord.setMouseTransparent(true);
            getChildren().add(1, coord);
        }
        atualizar();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setFruta(Image fruta) {
        this.fruta = fruta;
        atualizar();
    }

    public boolean temFruta() {
        return fruta != null;
    }

    public void setObstaculo(Obstaculo obstaculo) {
        this.obstaculo = obstaculo;
        atualizar();
    }

    public Obstaculo getObstaculo() {
        return obstaculo;
    }

    public void setExplosao(boolean explosao) {
        this.explosao = explosao;
        atualizar();
    }

    /** Cor do pacman que está na casa (nulo se nenhum), usada na borda e no brilho. */
    public void setCorPacman(Color cor) {
        if (cor == null ? corPacman == null : cor.equals(corPacman)) {
            return;
        }
        this.corPacman = cor;
        atualizar();
    }

    public void setVencedor(boolean vencedor) {
        this.vencedor = vencedor;
        atualizar();
    }

    /** Prévia de colocação: borda tracejada amarela + sprite a 55% de opacidade. */
    public void setPrevia(Image imagem) {
        previa.getChildren().clear();
        if (imagem != null) {
            previa.getChildren().add(Sprites.view(imagem, tamanho * 0.57));
        }
        atualizar();
    }

    /** O fantasma some (fade) e a casa passa a mostrar a explosão (escala + fade). */
    public void animarExplosao(java.util.function.Consumer<javafx.animation.Animation> tocar) {
        Node antigo = conteudo.getChildren().isEmpty() ? null : conteudo.getChildren().get(0);
        obstaculo = null;
        explosao = true;
        atualizar();
        Node estrela = conteudo.getChildren().get(0);
        estrela.setScaleX(0.2);
        estrela.setScaleY(0.2);
        estrela.setOpacity(0);
        ScaleTransition escala = new ScaleTransition(Duration.millis(400), estrela);
        escala.setToX(1);
        escala.setToY(1);
        FadeTransition surgir = new FadeTransition(Duration.millis(400), estrela);
        surgir.setToValue(1);
        ParallelTransition explosaoAnim = new ParallelTransition(escala, surgir);
        if (antigo != null) {
            conteudo.getChildren().add(0, antigo);
            FadeTransition sumir = new FadeTransition(Duration.millis(400), antigo);
            sumir.setToValue(0);
            sumir.setOnFinished(e -> conteudo.getChildren().remove(antigo));
            explosaoAnim.getChildren().add(sumir);
        }
        tocar.accept(explosaoAnim);
    }

    private void atualizar() {
        conteudo.getChildren().clear();
        boolean ocupada = corPacman != null;
        if (explosao) {
            conteudo.getChildren().add(Icones.explosao(tamanho * 0.3));
        } else if (obstaculo instanceof Fantasma f) {
            conteudo.getChildren().add(Sprites.view(Sprites.fantasma(f.getCor(), tamanho * 0.57), tamanho * 0.57));
        } else if (obstaculo != null) {
            conteudo.getChildren().add(Sprites.view(Sprites.rocha(tamanho * 0.57), tamanho * 0.57));
        } else if (fruta != null && !ocupada) {
            ImageView v = Sprites.view(fruta, tamanho * 0.59);
            conteudo.getChildren().add(v);
        }
        bolinha.setVisible(conteudo.getChildren().isEmpty() && !ocupada && previa.getChildren().isEmpty());

        StringBuilder fundoCss = new StringBuilder("-fx-background-color: " + Tema.hex(Tema.CELULA));
        String borda = "-fx-border-color: " + Tema.hex(Tema.CELULA_BORDA) + "; -fx-border-width: 0 1 1 0;";
        String efeito = "";
        if (obstaculo instanceof Fantasma f) {
            fundoCss.append(", ").append(Tema.rgba(CoresPacman.fantasma(f.getCor()), 0.10));
        } else if (obstaculo instanceof Rocha) {
            fundoCss.setLength(0);
            fundoCss.append("-fx-background-color: #0d0d33");
        } else if (explosao) {
            fundoCss.append(", rgba(255,122,46,0.10)");
        }
        Color destaque = vencedor ? Tema.AMARELO : corPacman != null ? corPacman : fruta != null ? Tema.VERDE : null;
        if (destaque != null) {
            fundoCss.append(", ").append(Tema.rgba(destaque, 0.10));
            borda = "-fx-border-color: " + Tema.hex(destaque) + "; -fx-border-width: 3;";
            efeito = "-fx-effect: innershadow(gaussian, " + Tema.rgba(destaque, 0.45) + ", "
                    + (tamanho * 0.2) + ", 0.1, 0, 0);";
        }
        if (vencedor) {
            efeito = "-fx-effect: dropshadow(gaussian, rgba(255,225,77,0.8), 22, 0.35, 0, 0);";
        }
        if (!previa.getChildren().isEmpty()) {
            borda = "-fx-border-color: #ffe14d; -fx-border-width: 3; -fx-border-style: segments(7, 5);";
        }
        fundo.setStyle(fundoCss + ";" + borda + efeito);
    }
}
