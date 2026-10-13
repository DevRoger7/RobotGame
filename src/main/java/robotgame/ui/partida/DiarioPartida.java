package robotgame.ui.partida;

import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import robotgame.modelo.EventoPartida;
import robotgame.modelo.Fantasma;
import robotgame.modelo.ModoJogo;
import robotgame.modelo.Robo;
import robotgame.ui.theme.Tema;

public class DiarioPartida {

    public record Entrada(int numero, Color cor, String texto, EventoPartida evento) {
    }

    private final ObservableList<Entrada> entradas = FXCollections.observableArrayList();
    private final List<Robo> robos;
    private final ModoJogo modo;

    public DiarioPartida(Robo[] robos, ModoJogo modo) {
        this.robos = Arrays.asList(robos);
        this.modo = modo;
    }

    public ObservableList<Entrada> getEntradas() {
        return entradas;
    }

    public Entrada ultima() {
        return entradas.isEmpty() ? null : entradas.get(entradas.size() - 1);
    }

    public void registrarInicio(int xFruta, int yFruta) {
        adicionar(Tema.TEXTO_SECUNDARIO, "Partida iniciada. Fruta em " + pos(xFruta, yFruta) + ".", null);
    }

    public void registrar(EventoPartida e) {
        String quem = nome(e.robo());
        String texto = switch (e.tipo()) {
            case MOVEU -> quem + " andou para " + direcao(e.direcao()) + " e está em " + pos(e.x(), e.y()) + ".";
            case INVALIDO -> quem + " tentou ir para " + alvo(e) + ": movimento inválido.";
            case ROCHA -> quem + " bateu numa rocha e voltou para " + pos(e.x(), e.y()) + ".";
            case EXPLODIU -> quem + " encostou " + obstaculo(e) + " em " + pos(e.x(), e.y()) + " e explodiu.";
            case FANTASMA_SUMIU -> e.obstaculo() instanceof Fantasma f
                    ? "O fantasma " + f.getCor() + " sumiu do tabuleiro depois da explosão."
                    : "A bomba sumiu do tabuleiro depois da explosão.";
            case ACHOU_ALIMENTO -> quem + " chegou à fruta em " + pos(e.x(), e.y()) + ".";
        };
        Color cor = e.tipo() == EventoPartida.Tipo.FANTASMA_SUMIU && e.obstaculo() instanceof Fantasma f
                ? CoresPacman.fantasma(f.getCor())
                : CoresPacman.pacman(e.robo().getCor());
        adicionar(cor, texto, e);
    }

    public String nome(Robo robo) {
        if (modo == ModoJogo.MANUAL) {
            return "Você";
        }
        return (modo.isPvp() ? "Jogador " : "Pacman ") + (robos.indexOf(robo) + 1);
    }

    private void adicionar(Color cor, String texto, EventoPartida evento) {
        entradas.add(new Entrada(entradas.size() + 1, cor, texto, evento));
    }

    private static String obstaculo(EventoPartida e) {
        return e.obstaculo() instanceof Fantasma f ? "no fantasma " + f.getCor() : "numa bomba";
    }

    public static String direcao(String direcao) {
        return switch (direcao) {
            case "up" -> "cima";
            case "down" -> "baixo";
            case "right" -> "a direita";
            case "left" -> "a esquerda";
            default -> direcao;
        };
    }

    private static String alvo(EventoPartida e) {
        return switch (e.direcao()) {
            case "up" -> "y = " + numero(e.y() + 1);
            case "down" -> "y = " + numero(e.y() - 1);
            case "right" -> "x = " + numero(e.x() + 1);
            case "left" -> "x = " + numero(e.x() - 1);
            default -> "\"" + e.direcao() + "\"";
        };
    }

    private static String numero(int n) {
        return n < 0 ? "−" + (-n) : String.valueOf(n);
    }

    public static String pos(int x, int y) {
        return "(" + x + ", " + y + ")";
    }
}
