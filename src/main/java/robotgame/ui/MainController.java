package robotgame.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import robotgame.excecao.MovimentoInvalidoException;
import robotgame.modelo.Robo;
import robotgame.modelo.Tabuleiro;

public class MainController {

    private static final int TAMANHO_CELULA = 100;

    @FXML
    private VBox setupPane;
    @FXML
    private VBox gamePane;
    @FXML
    private TextField corField;
    @FXML
    private Spinner<Integer> xSpinner;
    @FXML
    private Spinner<Integer> ySpinner;
    @FXML
    private Label setupErrorLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label statsLabel;
    @FXML
    private GridPane boardGrid;

    private Robo robo;
    private Tabuleiro tabuleiro;

    @FXML
    private void initialize() {
        int max = Tabuleiro.TAMANHO - 1;
        xSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, 0));
        ySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, max));
    }

    @FXML
    private void iniciarJogo() {
        String cor = corField.getText() == null ? "" : corField.getText().trim();
        if (cor.isEmpty()) {
            setupErrorLabel.setText("Informe a cor do robô.");
            return;
        }

        try {
            tabuleiro = new Tabuleiro(xSpinner.getValue(), ySpinner.getValue());
        } catch (IllegalArgumentException e) {
            setupErrorLabel.setText(e.getMessage());
            return;
        }

        setupErrorLabel.setText("");
        robo = new Robo(cor);
        statsLabel.setText("");
        statusLabel.setText("Use as setas do teclado para mover o robô " + cor + ".");

        setupPane.setVisible(false);
        setupPane.setManaged(false);
        gamePane.setVisible(true);
        gamePane.setManaged(true);

        desenharTabuleiro();
        boardGrid.requestFocus();
    }

    @FXML
    private void novoJogo() {
        robo = null;
        tabuleiro = null;

        gamePane.setVisible(false);
        gamePane.setManaged(false);
        setupPane.setVisible(true);
        setupPane.setManaged(true);
        setupErrorLabel.setText("");
    }

    public void aoTeclaPressionada(KeyEvent evento) {
        if (robo == null || tabuleiro == null || tabuleiro.alimentoEncontradoPor(robo)) {
            return;
        }

        String direcao = switch (evento.getCode()) {
            case UP -> "up";
            case DOWN -> "down";
            case RIGHT -> "right";
            case LEFT -> "left";
            default -> null;
        };
        if (direcao == null) {
            return;
        }

        try {
            robo.mover(direcao);
            statusLabel.setText("Robô " + robo.getCor() + " em (" + robo.getX() + "," + robo.getY() + ")");
        } catch (MovimentoInvalidoException e) {
            statusLabel.setText(e.getMessage());
        }

        desenharTabuleiro();

        if (tabuleiro.alimentoEncontradoPor(robo)) {
            statusLabel.setText("Robô " + robo.getCor() + " encontrou o alimento!");
            statsLabel.setText(robo.getTotalMovimentos() + " movimentos — "
                    + robo.getMovimentosValidos() + " válidos, "
                    + robo.getMovimentosInvalidos() + " inválidos");
        }
    }

    private void desenharTabuleiro() {
        boardGrid.getChildren().clear();
        int n = Tabuleiro.TAMANHO;
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                boardGrid.add(criarCelula(x, y), x, n - 1 - y);
            }
        }
    }

    private StackPane criarCelula(int x, int y) {
        StackPane celula = new StackPane();
        celula.setPrefSize(TAMANHO_CELULA, TAMANHO_CELULA);
        celula.setStyle("-fx-background-color: #10101c; -fx-border-color: #2a2a55;");

        if (robo.isAtivo() && robo.getX() == x && robo.getY() == y) {
            Circle corpo = new Circle(TAMANHO_CELULA / 2.8, corParaJavaFx(robo.getCor()));
            celula.getChildren().add(corpo);
        } else if (x == tabuleiro.getXAlimento() && y == tabuleiro.getYAlimento()) {
            Circle pastilha = new Circle(TAMANHO_CELULA / 8.0, Color.web("#ffe066"));
            celula.getChildren().add(pastilha);
        }

        return celula;
    }

    private Color corParaJavaFx(String cor) {
        return switch (cor.trim().toLowerCase()) {
            case "vermelho" -> Color.web("#ff4444");
            case "verde" -> Color.web("#44ff66");
            case "amarelo" -> Color.web("#ffe066");
            case "azul" -> Color.web("#4488ff");
            case "roxo", "magenta" -> Color.web("#cc55ff");
            case "ciano" -> Color.web("#44eaff");
            default -> Color.web("#dddddd");
        };
    }
}
