package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.ui.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.sided.Raid;

public class DeathDialog extends Dialog {
    public DeathDialog(Dungeoneer client, Skin skin) {
        super("You Died!", skin);

        getContentTable().add(new Label(String.format("Raided %s", client.getState().getCurrentRaid().get().target().username()), skin)).pad(10f).row();

        TextButton returnHomeButton = new TextButton("Return Home", skin);
        returnHomeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                client.getState().getCurrentRaid().end();
                client.getState().getCurrentRaid().setStatus(Raid.Status.WAITING);
                client.getState().setStatus(ClientState.Status.LOBBY);
                client.getState().setActiveCharacter(-1);
                Gdx.app.postRunnable(() -> client.setScreen(new MainScreen(client)));
            }
        });
        getButtonTable().add(returnHomeButton);

        pack();
    }
}
