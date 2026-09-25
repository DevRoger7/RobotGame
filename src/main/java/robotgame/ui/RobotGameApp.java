package robotgame.ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RobotGameApp extends Application {

    @Override
    public void start(Stage palco) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent raiz = loader.load();
        MainController controller = loader.getController();

        Scene cena = new Scene(raiz, 520, 640);
        cena.setOnKeyPressed(controller::aoTeclaPressionada);

        palco.setTitle("RobotGame");
        palco.setScene(cena);
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
