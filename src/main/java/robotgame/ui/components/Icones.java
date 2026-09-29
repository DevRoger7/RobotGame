package robotgame.ui.components;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/** Ícones vetoriais simples (a fonte pixel não tem esses símbolos). */
public final class Icones {

    private Icones() {
    }

    public static Node check(Color cor) {
        return traco("M1 5 L4 8 L10 1", cor, 2);
    }

    public static Node chevronDireita(Color cor) {
        return traco("M1 1 L5 6 L1 11", cor, 2);
    }

    public static Node chevronEsquerda(Color cor) {
        return traco("M5 1 L1 6 L5 11", cor, 2);
    }

    public static Node xis(Color cor) {
        return traco("M1 1 L10 10 M10 1 L1 10", cor, 2);
    }

    public static Node play(Color cor, double tamanho) {
        return preenchido("M0 0 L10 6 L0 12 Z", cor, tamanho / 12);
    }

    public static Node pausa(Color cor) {
        return preenchido("M0 0 H3 V12 H0 Z M6 0 H9 V12 H6 Z", cor, 1);
    }

    public static Node recomecar(Color cor) {
        return traco("M11 3 A5.5 5.5 0 1 0 12 8 M11 0 V3.5 H7.5", cor, 2);
    }

    public static Node som(Color cor, boolean ligado) {
        SVGPath alto = new SVGPath();
        alto.setContent("M0 4 H3 L7 0 V12 L3 8 H0 Z");
        alto.setFill(cor);
        SVGPath ondas = new SVGPath();
        ondas.setContent(ligado ? "M9 3.5 Q11 6 9 8.5 M11 1.5 Q14.5 6 11 10.5" : "M9 3.5 L14 8.5 M14 3.5 L9 8.5");
        ondas.setStroke(cor);
        ondas.setStrokeWidth(1.6);
        ondas.setFill(null);
        ondas.setStrokeLineCap(StrokeLineCap.ROUND);
        return new javafx.scene.Group(alto, ondas);
    }

    /** Triângulo apontando para a direção ("up", "down", "left", "right"). */
    public static Node seta(String direcao, Color cor, double tamanho) {
        Polygon t = new Polygon(0, tamanho, tamanho / 2, 0, tamanho, tamanho);
        t.setFill(cor);
        t.setRotate(switch (direcao) {
            case "down" -> 180;
            case "left" -> 270;
            case "right" -> 90;
            default -> 0;
        });
        return t;
    }

    /** Estrela de explosão (não há imagem de explosão entre os assets). */
    public static Node explosao(double raio) {
        int pontas = 10;
        Polygon externa = estrela(pontas, raio, raio * 0.48);
        externa.setFill(Color.web("#ff7a2e"));
        Polygon interna = estrela(pontas, raio * 0.6, raio * 0.3);
        interna.setFill(Color.web("#ffe14d"));
        return new javafx.scene.layout.StackPane(externa, interna);
    }

    public static Polygon estrela(int pontas, double raioExterno, double raioInterno) {
        Polygon p = new Polygon();
        for (int i = 0; i < pontas * 2; i++) {
            double r = i % 2 == 0 ? raioExterno : raioInterno;
            double a = Math.PI * i / pontas - Math.PI / 2;
            p.getPoints().addAll(r * Math.cos(a), r * Math.sin(a));
        }
        return p;
    }

    private static SVGPath traco(String caminho, Color cor, double largura) {
        SVGPath p = new SVGPath();
        p.setContent(caminho);
        p.setStroke(cor);
        p.setStrokeWidth(largura);
        p.setFill(null);
        p.setStrokeLineCap(StrokeLineCap.ROUND);
        p.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return p;
    }

    // Group para que a escala entre nos limites usados no layout.
    private static Node preenchido(String caminho, Color cor, double escala) {
        SVGPath p = new SVGPath();
        p.setContent(caminho);
        p.setFill(cor);
        p.setScaleX(escala);
        p.setScaleY(escala);
        return new javafx.scene.Group(p);
    }
}
