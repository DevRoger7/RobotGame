package robotgame.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import robotgame.modelo.ModoJogo;
import robotgame.ui.theme.Tema;

/** Cabeçalho das telas de jogo: logo, barra de passos e etiqueta do modo. */
public class AppHeader extends HBox {

    public AppHeader(ModoJogo modo, StepperBar.Passo passo) {
        getStyleClass().add("neon-card");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(0, 22, 0, 22));
        setSpacing(16);
        setMinHeight(84);
        setPrefHeight(84);
        setMaxHeight(84);

        VBox nome = new VBox(6, Ui.label("PACMAN", 14, "pixel", "amarelo"), Ui.label("GAME", 14, "pixel", "ciano"));
        nome.setAlignment(Pos.CENTER_LEFT);
        HBox logo = new HBox(12, Sprites.pacman(Tema.AMARELO, 44), nome);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setMinWidth(USE_PREF_SIZE);

        StepperBar passos = new StepperBar(modo, passo);
        HBox.setHgrow(passos, Priority.ALWAYS);
        passos.setMaxWidth(Double.MAX_VALUE);

        Label etiqueta = Ui.label(rotuloModo(modo), "mode-tag");
        etiqueta.setMinWidth(USE_PREF_SIZE);

        getChildren().addAll(logo, passos, etiqueta);
    }

    public static String rotuloModo(ModoJogo modo) {
        return switch (modo) {
            case MANUAL -> "MODO 1 · MANUAL";
            case CORRIDA -> "MODO 2 · CORRIDA";
            case NORMAL_X_INTELIGENTE -> "MODO 3 · NORMAL × INTELIGENTE";
            case COM_OBSTACULOS -> "MODO 4 · OBSTÁCULOS";
        };
    }
}
