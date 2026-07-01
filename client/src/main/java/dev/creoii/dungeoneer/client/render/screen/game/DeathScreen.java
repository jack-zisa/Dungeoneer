package dev.creoii.dungeoneer.client.render.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.render.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.sided.Raid;

public class DeathScreen extends AbstractScreen {
    public DeathScreen(Dungeoneer client) {
        super(client);
    }

    @Override
    public void show() {
        Table root = new Table();
        root.setFillParent(true);

        Label youDiedLabel = new Label("You died!", SKIN);
        Label targetLabel = new Label(String.format("Raided %s", getClient().getState().getCurrentRaid().get().target().username()), SKIN);

        root.add(youDiedLabel).row();
        root.add(targetLabel).pad(10f).row();

        TextButton returnHomeButton = new TextButton("Return Home", SKIN);
        returnHomeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().getState().getCurrentRaid().end();
                getClient().getState().getCurrentRaid().setStatus(Raid.Status.WAITING);
                getClient().getState().setStatus(ClientState.Status.LOBBY);
                Gdx.app.postRunnable(() -> getClient().setScreen(new MainScreen(getClient())));
            }
        });
        root.add(returnHomeButton);

        getStage().addActor(root);
        getClient().getInputMultiplexer().addProcessor(getStage());

        super.show();
    }

    @Override
    public void hide() {
        getClient().getInputMultiplexer().removeProcessor(getStage());
    }

    @Override
    public void dispose() {
        super.dispose();
        SKIN.dispose();
    }
}
