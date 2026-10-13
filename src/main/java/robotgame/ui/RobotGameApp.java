package robotgame.ui;

import javafx.application.Application;
import javafx.stage.Stage;

import robotgame.ui.partida.ConfiguracaoPartida;
import robotgame.ui.screens.Navegador;
import robotgame.ui.theme.Tema;

public class RobotGameApp extends Application {

    @Override
    public void start(Stage palco) {
        Tema.carregarFontes();
        Navegador navegador = new Navegador(palco);
        navegador.irParaMenu(new ConfiguracaoPartida());
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
