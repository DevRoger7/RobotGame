package robotgame.ui.components;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Screen;

import robotgame.ui.Imagens;

/**
 * Sprites do jogo, carregados dos mesmos arquivos e com o mesmo tratamento de antes ({@link Imagens}).
 *
 * <p>As imagens originais são grandes (até 1254 px). Para ficarem nítidas com {@code setSmooth(false)},
 * cada tamanho de exibição recebe uma cópia reduzida em memória por média de área; os arquivos não mudam.
 */
public final class Sprites {

    private Sprites() {
    }

    public static final String[] FRUTAS = {"cereja", "maca", "morango"};

    private static final Map<String, Image> ORIGINAIS = new HashMap<>();
    private static final Map<String, Image> REDUZIDAS = new HashMap<>();

    public static Image pacmanAberto(double tamanho) {
        return reduzida("pacman_aberto", tamanho, () -> Imagens.carregar("pacman_aberto.png"));
    }

    public static Image pacmanFechado(double tamanho) {
        return reduzida("pacman_fechado", tamanho, () -> Imagens.carregar("pacman_fechado.png"));
    }

    public static Image fantasma(String cor, double tamanho) {
        return reduzida("fantasma_" + cor, tamanho, () -> Imagens.carregarSemFundo("fantasma_" + cor + ".png"));
    }

    public static Image rocha(double tamanho) {
        return reduzida("pedra", tamanho, () -> Imagens.carregarRecortada("obstaculo_pedra.png"));
    }

    public static Image fruta(int indice, double tamanho) {
        String nome = FRUTAS[indice];
        return reduzida("fruta_" + nome, tamanho, () -> "cereja".equals(nome)
                ? Imagens.carregar("fruta_cereja.png")
                : Imagens.carregarSemFundo("fruta_" + nome + ".png"));
    }

    /** ImageView de sprite: nítido, proporção preservada, cabendo num quadrado de {@code tamanho}. */
    public static ImageView view(Image imagem, double tamanho) {
        ImageView view = new ImageView(imagem);
        view.setSmooth(false);
        view.setPreserveRatio(true);
        view.setFitWidth(tamanho);
        view.setFitHeight(tamanho);
        return view;
    }

    /** Pacman tingido na cor do robô (mesmo mecanismo da interface anterior: Blend SRC_ATOP). */
    public static ImageView pacman(Color cor, double tamanho) {
        ImageView sprite = view(pacmanAberto(tamanho), tamanho);
        tingir(sprite, cor, tamanho);
        return sprite;
    }

    public static void tingir(ImageView sprite, Color cor, double tamanho) {
        sprite.setEffect(new Blend(BlendMode.SRC_ATOP, null,
                new ColorInput(-2, -2, tamanho + 4, tamanho + 4, cor)));
    }

    private static Image reduzida(String chave, double tamanho, Supplier<Image> carregar) {
        double escalaTela = 1;
        for (Screen tela : Screen.getScreens()) {
            escalaTela = Math.max(escalaTela, tela.getOutputScaleX());
        }
        int alvo = (int) Math.ceil(tamanho * escalaTela);
        return REDUZIDAS.computeIfAbsent(chave + "@" + alvo, k -> {
            Image original = ORIGINAIS.computeIfAbsent(chave, c -> carregar.get());
            return reduzirPorArea(original, alvo);
        });
    }

    // Redução por média de área (com alfa pré-multiplicado): sem serrilhado, ao contrário do vizinho mais próximo.
    private static Image reduzirPorArea(Image original, int ladoMaximo) {
        int w = (int) original.getWidth();
        int h = (int) original.getHeight();
        double fator = (double) ladoMaximo / Math.max(w, h);
        if (fator >= 1) {
            return original;
        }
        int nw = Math.max(1, (int) Math.round(w * fator));
        int nh = Math.max(1, (int) Math.round(h * fator));
        PixelReader leitor = original.getPixelReader();
        WritableImage destino = new WritableImage(nw, nh);
        PixelWriter escritor = destino.getPixelWriter();
        double passoX = (double) w / nw;
        double passoY = (double) h / nh;
        for (int ty = 0; ty < nh; ty++) {
            int y0 = (int) Math.floor(ty * passoY);
            int y1 = Math.min(h, (int) Math.ceil((ty + 1) * passoY));
            for (int tx = 0; tx < nw; tx++) {
                int x0 = (int) Math.floor(tx * passoX);
                int x1 = Math.min(w, (int) Math.ceil((tx + 1) * passoX));
                double a = 0, r = 0, g = 0, b = 0;
                int n = 0;
                for (int y = y0; y < y1; y++) {
                    for (int x = x0; x < x1; x++) {
                        int argb = leitor.getArgb(x, y);
                        double alfa = (argb >>> 24) / 255.0;
                        a += alfa;
                        r += alfa * ((argb >> 16) & 0xff);
                        g += alfa * ((argb >> 8) & 0xff);
                        b += alfa * (argb & 0xff);
                        n++;
                    }
                }
                if (a <= 0) {
                    escritor.setArgb(tx, ty, 0);
                    continue;
                }
                int ai = (int) Math.round(a / n * 255);
                escritor.setArgb(tx, ty, (ai << 24)
                        | ((int) Math.round(r / a) << 16)
                        | ((int) Math.round(g / a) << 8)
                        | (int) Math.round(b / a));
            }
        }
        return destino;
    }
}
