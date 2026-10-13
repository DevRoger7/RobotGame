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

    // cores[i] é a cor do pacman i+1; editando é o número do pacman em edição (0 = um pacman só, sem números).
    public void mostrar(int editando, String... cores) {
        botoes.forEach((nome, botao) -> {
            botao.getStyleClass().removeAll("selecionado", "outro");
            Label numero = numeros.get(nome);
            numero.setVisible(false);
            int dono = 0;
            for (int i = 0; i < cores.length; i++) {
                if (nome.equals(cores[i]) && (dono == 0 || i + 1 == Math.max(editando, 1))) {
                    dono = i + 1;
                }
            }
            if (dono == 0) {
                return;
            }
            boolean selecionado = dono == Math.max(editando, 1);
            botao.getStyleClass().add(selecionado ? "selecionado" : "outro");
            if (editando > 0) {
                numero.setText(String.valueOf(dono));
                numero.setStyle(numero.getStyle().replaceAll("-fx-background-color: [^;]+;",
                        "-fx-background-color: " + Tema.hex(selecionado ? Tema.AMARELO : Tema.TEXTO_SECUNDARIO) + ";"));
                numero.setVisible(true);
            }
        });
    }
}
