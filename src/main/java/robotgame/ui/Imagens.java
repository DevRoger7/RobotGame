package robotgame.ui;

import java.util.ArrayDeque;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

final class Imagens {

    private Imagens() {
    }

    private static final int ALFA_MINIMO_VISIVEL = 16;

    static Image carregar(String arquivo) {
        return new Image(Imagens.class.getResourceAsStream("imagens/" + arquivo));
    }

    static Image carregarRecortada(String arquivo) {
        return recortarMargens(carregar(arquivo));
    }

    // Apaga só o fundo claro ligado à borda, preservando áreas claras internas (olhos, brilho).
    static Image carregarSemFundo(String arquivo) {
        Image original = carregar(arquivo);
        int w = (int) original.getWidth();
        int h = (int) original.getHeight();
        PixelReader leitor = original.getPixelReader();
        WritableImage resultado = new WritableImage(leitor, w, h);
        PixelWriter escritor = resultado.getPixelWriter();

        boolean[] visitado = new boolean[w * h];
        ArrayDeque<Integer> fila = new ArrayDeque<>();
        for (int x = 0; x < w; x++) {
            fila.add(x);
            fila.add((h - 1) * w + x);
        }
        for (int y = 0; y < h; y++) {
            fila.add(y * w);
            fila.add(y * w + w - 1);
        }

        while (!fila.isEmpty()) {
            int p = fila.poll();
            if (visitado[p]) {
                continue;
            }
            visitado[p] = true;
            int x = p % w;
            int y = p / w;
            if (!ehFundo(leitor.getArgb(x, y))) {
                continue;
            }
            escritor.setArgb(x, y, 0);
            if (x > 0) fila.add(p - 1);
            if (x < w - 1) fila.add(p + 1);
            if (y > 0) fila.add(p - w);
            if (y < h - 1) fila.add(p + w);
        }
        return recortarMargens(resultado);
    }

    private static Image recortarMargens(Image imagem) {
        PixelReader leitor = imagem.getPixelReader();
        int w = (int) imagem.getWidth();
        int h = (int) imagem.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((leitor.getArgb(x, y) >>> 24) >= ALFA_MINIMO_VISIVEL) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < 0) {
            return imagem;
        }
        return new WritableImage(leitor, minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private static boolean ehFundo(int argb) {
        if ((argb >>> 24) == 0) {
            return true;
        }
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        return min >= 215 && max - min <= 20;
    }
}
