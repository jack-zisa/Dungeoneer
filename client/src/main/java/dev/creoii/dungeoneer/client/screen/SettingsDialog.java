package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.network.c2s.ApplySettingsC2S;

public class SettingsDialog extends Dialog {
    private final Dungeoneer client;

    public SettingsDialog(Dungeoneer client, Skin skin) {
        super("Settings", skin);
        this.client = client;

        button("Apply", true);
        button("Cancel", false);

        pack();
    }

    @Override
    protected void result(Object object) {
        if (Boolean.TRUE.equals(object)) {
            client.get().sendUDP(new ApplySettingsC2S(client.getState().getAccount().id(), ""));
        }
    }
}
