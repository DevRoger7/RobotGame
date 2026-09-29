package robotgame.ui.theme;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/** Design tokens da interface "Labirinto Neon": cores, famílias de fonte e a folha de estilo. */
public final class Tema {

    private Tema() {
    }

    public static final double LARGURA = 1280;
    public static final double ALTURA = 800;

    // Nomes reais das famílias, conferidos com Font.getFamilies() depois de Font.loadFont.
    public static final String FONTE_PIXEL = "Press Start 2P";
    public static final String FONTE_TEXTO = "Chakra Petch Medium";
    public static final String FONTE_SEMI = "Chakra Petch SemiBold";
    public static final String FONTE_BOLD = "Chakra Petch";

    public static final Color FUNDO = Color.web("#050514");
    public static final Color PONTO_FUNDO = Color.web("#15154a");
    public static final Color CARTAO = Color.web("#0c0c2c");
    public static final Color BORDA = Color.web("#2a2aff");
    public static final Color BORDA_FORTE = Color.web("#4a4aff");
    public static final Color BLOCO = Color.web("#10103a");
    public static final Color TEXTO = Color.web("#f4f4ff");
    public static final Color TEXTO_SECUNDARIO = Color.web("#9da0d8");
    public static final Color AMARELO = Color.web("#ffe14d");
    public static final Color CIANO = Color.web("#3df0ff");
    public static final Color VERDE = Color.web("#4cff8a");
    public static final Color LARANJA = Color.web("#ffa63d");
    public static final Color VERMELHO = Color.web("#ff4d5e");
    public static final Color CELULA = Color.web("#0a0a28");
    public static final Color CELULA_BORDA = Color.web("#1c1c6b");
    public static final Color ROTULO_CELULA = Color.web("#8488cc");
    public static final Color TABULEIRO = Color.web("#070720");
    public static final Color BOLINHA = Color.web("#ffd9a8");

    private static final String[] FONTES = {
            "PressStart2P-Regular.ttf",
            "ChakraPetch-Medium.ttf",
            "ChakraPetch-SemiBold.ttf",
            "ChakraPetch-Bold.ttf"
    };

    private static boolean fontesCarregadas;

    public static void carregarFontes() {
        if (fontesCarregadas) {
            return;
        }
        for (String arquivo : FONTES) {
            try (InputStream in = Tema.class.getResourceAsStream("/fonts/" + arquivo)) {
                if (in == null || Font.loadFont(in, 12) == null) {
                    System.err.println("Fonte não carregada: " + arquivo);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        fontesCarregadas = true;
    }

    public static String css() {
        return Tema.class.getResource("theme.css").toExternalForm();
    }

    public static String hex(Color c) {
        return String.format("#%02x%02x%02x",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }

    public static String rgba(Color c, double alfa) {
        return String.format(java.util.Locale.ROOT, "rgba(%d,%d,%d,%.2f)",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255),
                alfa);
    }
}
