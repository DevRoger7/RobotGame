package robotgame.ui;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

final class Sons {

    private final MediaPlayer inicio = criar("inicio.wav");
    private final MediaPlayer wakawaka = criar("wakawaka.wav");
    private final MediaPlayer morte = criar("morte.wav");
    private final MediaPlayer fruta = criar("comer_fruta.wav");
    private final List<MediaPlayer> todos = List.of(inicio, wakawaka, morte, fruta);

    private final Set<MediaPlayer> tocando = new LinkedHashSet<>();
    private final Map<MediaPlayer, Runnable> aoTerminar = new HashMap<>();
    private boolean mudo;

    Sons() {
        for (MediaPlayer player : todos) {
            player.setOnEndOfMedia(() -> terminou(player));
            player.setOnError(() -> terminou(player));
        }
        wakawaka.setOnEndOfMedia(() -> {
            if (tocando.contains(wakawaka)) {
                wakawaka.seek(Duration.ZERO);
                wakawaka.play();
            }
        });
    }

    void tocarInicio(Runnable depois) {
        tocarDoComeco(inicio, depois);
    }

    void pararInicio() {
        aoTerminar.remove(inicio);
        tocando.remove(inicio);
        inicio.stop();
    }

    void tocarMorte(Runnable depois) {
        tocarDoComeco(morte, depois);
    }

    void tocarFruta() {
        tocarDoComeco(fruta, null);
    }

    void iniciarWakawaka() {
        tocando.add(wakawaka);
        wakawaka.play();
    }

    void pararWakawaka() {
        tocando.remove(wakawaka);
        wakawaka.stop();
    }

    void pausarTudo() {
        tocando.forEach(MediaPlayer::pause);
    }

    void retomarTudo() {
        tocando.forEach(MediaPlayer::play);
    }

    void pararTudo() {
        aoTerminar.clear();
        tocando.clear();
        todos.forEach(MediaPlayer::stop);
    }

    boolean isMudo() {
        return mudo;
    }

    void setMudo(boolean mudo) {
        this.mudo = mudo;
        todos.forEach(player -> player.setMute(mudo));
    }

    private void tocarDoComeco(MediaPlayer player, Runnable depois) {
        if (player.getStatus() == MediaPlayer.Status.HALTED) {
            if (depois != null) {
                depois.run();
            }
            return;
        }
        if (depois != null) {
            aoTerminar.put(player, depois);
        } else {
            aoTerminar.remove(player);
        }
        tocando.add(player);
        player.stop();
        player.play();
    }

    private void terminou(MediaPlayer player) {
        tocando.remove(player);
        player.stop();
        Runnable depois = aoTerminar.remove(player);
        if (depois != null) {
            depois.run();
        }
    }

    private static MediaPlayer criar(String arquivo) {
        return new MediaPlayer(new Media(Sons.class.getResource("sons/" + arquivo).toExternalForm()));
    }
}
