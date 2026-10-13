package robotgame.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeradorPoder {

    public static final int ESPERA_INICIAL = 5;
    public static final int ESPERA_APOS_COMIDO = 5;

    private final Random random;
    private int espera = ESPERA_INICIAL;

    public GeradorPoder(Random random) {
        this.random = random;
    }

    public List<EventoPartida> fimDaRodada(Tabuleiro t, Robo[] robos) {
        List<EventoPartida> eventos = new ArrayList<>();
        ItemPoder item = t.getItemPoder();

        if (item != null) {
            item.passarRodada();
            if (item.expirou()) {
                t.removerItemPoder();
                eventos.add(evento(EventoPartida.Tipo.PODER_SUMIU, item.getX(), item.getY()));
                criar(t, robos, item, eventos);
            }
            return eventos;
        }

        if (espera > 0) {
            espera--;
        }
        if (espera == 0) {
            criar(t, robos, null, eventos);
        }
        return eventos;
    }

    public void itemFoiComido() {
        espera = ESPERA_APOS_COMIDO;
    }

    // Se não houver casa livre, não cria: com a espera em 0, tenta de novo na próxima rodada.
    private void criar(Tabuleiro t, Robo[] robos, ItemPoder anterior, List<EventoPartida> eventos) {
        List<int[]> livres = casasLivres(t, robos, anterior);
        if (livres.isEmpty()) {
            return;
        }
        int[] casa = livres.get(random.nextInt(livres.size()));
        t.setItemPoder(new ItemPoder(casa[0], casa[1]));
        eventos.add(evento(EventoPartida.Tipo.PODER_APARECEU, casa[0], casa[1]));
    }

    private List<int[]> casasLivres(Tabuleiro t, Robo[] robos, ItemPoder anterior) {
        List<int[]> livres = new ArrayList<>();
        for (int x = 0; x < t.getTamanho(); x++) {
            for (int y = 0; y < t.getTamanho(); y++) {
                if (casaLivre(t, robos, anterior, x, y)) {
                    livres.add(new int[]{x, y});
                }
            }
        }
        return livres;
    }

    private boolean casaLivre(Tabuleiro t, Robo[] robos, ItemPoder anterior, int x, int y) {
        if (t.getObstaculo(x, y) != null) {
            return false;
        }
        if (x == t.getXAlimento() && y == t.getYAlimento()) {
            return false;
        }
        FantasmaInteligente fantasma = t.getFantasmaInteligente();
        if (fantasma != null && fantasma.getX() == x && fantasma.getY() == y) {
            return false;
        }
        if (anterior != null && anterior.getX() == x && anterior.getY() == y) {
            return false;
        }
        boolean alcancavel = false;
        for (Robo robo : robos) {
            if (!robo.isAtivo()) {
                continue;
            }
            if (robo.getX() == x && robo.getY() == y) {
                return false;
            }
            if (BuscaCaminho.distancia(t, robo.getX(), robo.getY(), x, y, false) != -1) {
                alcancavel = true;
            }
        }
        return alcancavel;
    }

    private static EventoPartida evento(EventoPartida.Tipo tipo, int x, int y) {
        return new EventoPartida(tipo, null, null, x, y, null);
    }
}
