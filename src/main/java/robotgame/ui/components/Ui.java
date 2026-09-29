package robotgame.ui.components;

import java.text.Normalizer;
import java.util.Set;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Fábricas pequenas para rótulos e botões no estilo do tema. */
public final class Ui {

    private Ui() {
    }

    // Classes que usam a Press Start 2P (ver pixel()).
    private static final Set<String> CLASSES_PIXEL = Set.of("pixel", "card-title", "mode-title", "step-pill",
            "tag", "mode-tag", "fim-titulo", "stat-value", "axis-label", "diary-num", "aba", "keycap");

    public static Label label(String texto, String... classes) {
        boolean pixel = false;
        for (String c : classes) {
            pixel |= CLASSES_PIXEL.contains(c);
        }
        Label l = new Label(pixel ? pixel(texto) : texto);
        l.getStyleClass().addAll(classes);
        return l;
    }

    /**
     * A Press Start 2P desenha maiúsculas acentuadas com altura de minúscula; como no mockup
     * ("CENARIO", "SIMULAÇAO"), os acentos saem e só o Ç fica.
     */
    public static String pixel(String texto) {
        if (texto == null) {
            return null;
        }
        String decomposto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return Normalizer.normalize(decomposto.replaceAll("[\\u0300-\\u0326\\u0328-\\u036f]", ""),
                Normalizer.Form.NFC);
    }

    public static Label label(String texto, double tamanho, String... classes) {
        Label l = label(texto, classes);
        l.setStyle("-fx-font-size: " + tamanho + "px;");
        return l;
    }

    /** Botão primário colorido (classe {@code btn-verde} ou {@code btn-laranja}). */
    public static Button primario(String texto, String cor, double altura) {
        Button b = botao(pixel(texto), "btn-primary", cor);
        b.setMinHeight(altura);
        b.setPrefHeight(altura);
        return b;
    }

    public static Button secundario(String texto, double altura) {
        Button b = botao(texto, "btn-secondary");
        b.setMinHeight(altura);
        b.setPrefHeight(altura);
        return b;
    }

    private static Button botao(String texto, String... classes) {
        Button b = new Button(texto);
        b.getStyleClass().addAll(classes);
        // O teclado é tratado na Scene; o foco não pode "roubar" espaço e setas.
        b.setFocusTraversable(false);
        b.setMaxWidth(Double.MAX_VALUE);
        animarHover(b);
        return b;
    }

    public static void animarHover(Node no) {
        ScaleTransition entrar = new ScaleTransition(Duration.millis(120), no);
        entrar.setToX(1.03);
        entrar.setToY(1.03);
        ScaleTransition sair = new ScaleTransition(Duration.millis(120), no);
        sair.setToX(1);
        sair.setToY(1);
        no.setOnMouseEntered(e -> {
            sair.stop();
            entrar.playFromStart();
        });
        no.setOnMouseExited(e -> {
            entrar.stop();
            sair.playFromStart();
        });
    }

    public static Region espaco() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        VBox.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    public static HBox linha(double espaco, Node... nos) {
        HBox h = new HBox(espaco, nos);
        h.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return h;
    }

    /** Faz os filhos dividirem a largura igualmente. */
    public static HBox linhaIgual(double espaco, Node... nos) {
        HBox h = new HBox(espaco, nos);
        for (Node n : nos) {
            HBox.setHgrow(n, Priority.ALWAYS);
            if (n instanceof Region r) {
                r.setMaxWidth(Double.MAX_VALUE);
                r.setPrefWidth(0);
                r.setMinWidth(0);
            }
        }
        return h;
    }

    public static void tamanhoFixo(Region r, double largura, double altura) {
        r.setMinSize(largura, altura);
        r.setPrefSize(largura, altura);
        r.setMaxSize(largura, altura);
    }
}
