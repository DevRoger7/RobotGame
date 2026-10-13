package robotgame.ui.components;

import javafx.scene.control.Label;

public class KeyCap extends Label {

    public KeyCap(String tecla, boolean claro) {
        super(tecla);
        getStyleClass().add("keycap");
        if (claro) {
            getStyleClass().add("claro");
        }
        setMinWidth(USE_PREF_SIZE);
    }
}
