package robotgame.ui.components;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import robotgame.ui.partida.CoresPacman;
import robotgame.ui.theme.Tema;

public class ColorSwatchRow extends HBox {

    private final Map<String, StackPane> botoes = new LinkedHashMap<>();
    private final Map<String, Label> numeros = new LinkedHashMap<>();

    public ColorSwatchRow(double tamanho, Consumer<String> aoEscolher) {
        setSpacing(7);
        setAlignment(Pos.CENTER_LEFT);
        for (String nome : CoresPacman.nomes()) {
            StackPane botao = new StackPane(Sprites.pacman(CoresPacman.pacman(nome), tamanho * 0.52));
            botao.getStyleClass().add("swatch");
            Ui.tamanhoFixo(botao, tamanho, tamanho);
            Label numero = Ui.label("", "pixel");
            numero.setStyle("-fx-font-size: 7px; -fx-text-fill: #050514; -fx-background-color: #ffe14d;"
                    + "-fx-background-radius: 50%; -fx-alignment: center; -fx-padding: 1 0 0 1;");
            Ui.tamanhoFixo(numero, 15, 15);
            numero.setVisible(false);
            numero.setTranslateX(tamanho * 0.36);
            numero.setTranslateY(-tamanho * 0.36);
            botao.getChildren().add(numero);
            botao.setOnMouseClicked(e -> aoEscolher.accept(nome));
            Ui.animarHover(botao);
            botoes.put(nome, botao);
            numeros.put(nome, numero);
            getChildren().add(botao);
        }
    }

    public void mostrar(String selecionada, int numeroSelecionado, String outra) {
        botoes.forEach((nome, botao) -> {
            botao.getStyleClass().removeAll("selecionado", "outro");
            Label numero = numeros.get(nome);
            numero.setVisible(false);
            if (nome.equals(selecionada)) {
                botao.getStyleClass().add("selecionado");
                if (numeroSelecionado > 0) {
                    numero.setText(String.valueOf(numeroSelecionado));
                    numero.setStyle(numero.getStyle().replaceAll("-fx-background-color: [^;]+;",
                            "-fx-background-color: " + Tema.hex(Tema.AMARELO) + ";"));
                    numero.setVisible(true);
                }
            } else if (nome.equals(outra)) {
                botao.getStyleClass().add("outro");
                numero.setText(String.valueOf(numeroSelecionado == 1 ? 2 : 1));
                numero.setStyle(numero.getStyle().replaceAll("-fx-background-color: [^;]+;",
                        "-fx-background-color: " + Tema.hex(Tema.TEXTO_SECUNDARIO) + ";"));
                numero.setVisible(true);
            }
        });
    }
}
