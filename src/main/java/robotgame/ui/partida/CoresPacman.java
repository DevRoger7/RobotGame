package robotgame.ui.partida;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.paint.Color;

/** Cores de pacman oferecidas no menu (as mesmas da interface anterior) e cores dos fantasmas. */
public final class CoresPacman {

    private CoresPacman() {
    }

    private static final Map<String, Color> PACMAN = new LinkedHashMap<>();
    private static final Map<String, Color> FANTASMA = new LinkedHashMap<>();

    static {
        PACMAN.put("amarelo", Color.web("#ffd800"));
        PACMAN.put("vermelho", Color.web("#ff4444"));
        PACMAN.put("azul", Color.web("#4488ff"));
        PACMAN.put("verde", Color.web("#44ff66"));
        PACMAN.put("rosa", Color.web("#ff88cc"));
        PACMAN.put("laranja", Color.web("#ffa033"));
        PACMAN.put("roxo", Color.web("#cc55ff"));
        PACMAN.put("ciano", Color.web("#44eaff"));
        PACMAN.put("branco", Color.web("#f0f0f0"));

        FANTASMA.put("azul", Color.web("#4a6cff"));
        FANTASMA.put("vermelho", Color.web("#ff4d5e"));
        FANTASMA.put("rosa", Color.web("#ff7ad9"));
        FANTASMA.put("ciano", Color.web("#3df0ff"));
    }

    /** Ordem em que os fantasmas são colocados no modo 4 (a mesma de antes). */
    public static final List<String> ORDEM_FANTASMAS = List.of("azul", "vermelho", "rosa", "ciano");

    public static List<String> nomes() {
        return List.copyOf(PACMAN.keySet());
    }

    public static Color pacman(String nome) {
        return PACMAN.get(nome);
    }

    public static Color fantasma(String nome) {
        return FANTASMA.getOrDefault(nome, Color.web("#4a6cff"));
    }

    public static String rotulo(String nome) {
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1);
    }
}
