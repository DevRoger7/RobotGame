package robotgame.ui.screens;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import robotgame.ui.Sons;
import robotgame.ui.partida.ConfiguracaoPartida;
import robotgame.ui.partida.ResultadoPartida;
import robotgame.ui.theme.Tema;

/**
 * Uma única Stage/Scene; cada tela troca o conteúdo da área de design (1280×800), que é escalada
 * proporcionalmente para caber na janela.
 */
public class Navegador {

    private final StackPane area = new StackPane();
    private final Scene cena;
    private final Sons sons = new Sons();
    private Tela atual;

    public Navegador(Stage palco) {
        area.setMinSize(Tema.LARGURA, Tema.ALTURA);
        area.setPrefSize(Tema.LARGURA, Tema.ALTURA);
        area.setMaxSize(Tema.LARGURA, Tema.ALTURA);

        StackPane janela = new StackPane(new Group(area));
        janela.setBackground(fundoPontilhado());
        Scale escala = new Scale(1, 1, 0, 0);
        area.getTransforms().add(escala);
        DoubleBinding fator = Bindings.createDoubleBinding(
                () -> Math.min(janela.getWidth() / Tema.LARGURA, janela.getHeight() / Tema.ALTURA),
                janela.widthProperty(), janela.heightProperty());
        escala.xProperty().bind(fator);
        escala.yProperty().bind(fator);

        Rectangle2D tela = Screen.getPrimary().getVisualBounds();
        double inicial = Math.min(1, Math.min(tela.getWidth() / Tema.LARGURA, (tela.getHeight() - 28) / Tema.ALTURA));
        cena = new Scene(janela, Tema.LARGURA * inicial, Tema.ALTURA * inicial);
        cena.getStylesheets().add(Tema.css());
        cena.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (atual != null) {
                atual.aoTecla(e);
            }
        });

        palco.setTitle("PacMan Game");
        palco.setScene(cena);
        palco.setMinWidth(640);
        palco.setMinHeight(430);
        palco.setOnCloseRequest(e -> {
            if (atual != null) {
                atual.aoSair();
            }
            sons.pararTudo();
        });
    }

    public Sons getSons() {
        return sons;
    }

    public Scene getCena() {
        return cena;
    }

    public void irParaMenu(ConfiguracaoPartida configuracao) {
        ir(new MenuScreen(this, configuracao));
    }

    public void irParaCenario(ConfiguracaoPartida configuracao) {
        ir(new CenarioScreen(this, configuracao));
    }

    public void irParaJogo(ConfiguracaoPartida configuracao) {
        ir(new GameScreen(this, configuracao));
    }

    public void irParaFim(ResultadoPartida resultado) {
        ir(new ResultScreen(this, resultado));
    }

    public void ir(Tela nova) {
        if (atual != null) {
            atual.aoSair();
        }
        atual = nova;
        Parent raiz = nova.getRaiz();
        area.getChildren().setAll(raiz);
        raiz.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(150), raiz);
        fade.setToValue(1);
        fade.play();
        nova.aoMostrar();
    }

    // Fundo #050514 com pontos #15154a de 1px a cada 24px (tile gerado por código).
    private static Background fundoPontilhado() {
        WritableImage tile = new WritableImage(24, 24);
        for (int y = 0; y < 24; y++) {
            for (int x = 0; x < 24; x++) {
                tile.getPixelWriter().setColor(x, y, x == 0 && y == 0 ? Tema.PONTO_FUNDO : Tema.FUNDO);
            }
        }
        return new Background(
                new BackgroundFill[]{new BackgroundFill(Tema.FUNDO, null, null)},
                new BackgroundImage[]{new BackgroundImage(tile, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                        BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)});
    }
}
