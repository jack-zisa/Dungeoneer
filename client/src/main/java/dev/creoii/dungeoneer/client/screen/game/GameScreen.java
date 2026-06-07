package dev.creoii.dungeoneer.client.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.Raid;
import dev.creoii.dungeoneer.network.c2s.raid.EndRaidC2S;

public class GameScreen extends AbstractScreen {
    private final Dungeoneer client;
    private Skin skin;

    public GameScreen(Dungeoneer client) {
        this.client = client;
    }

    public Dungeoneer getClient() {
        return client;
    }

    @Override
    public void show() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Raid currentRaid = client.getCurrentRaid();
        if (currentRaid == null) {
            client.getState().setStatus(ClientState.Status.LOBBY);
            client.setScreen(new MainScreen(client));
            Dungeoneer.LOGGER.error("Raid failed to start");
            return;
        }

        Table root = new Table();
        root.setFillParent(true);

        root.add(new Label(String.format("Raiding %s!", client.getCurrentRaid().target().username()), skin)).row();

        TextButton surrenderButton = new TextButton("Surrender", skin);
        surrenderButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().get().sendUDP(new EndRaidC2S(client.getCurrentRaid().id()));
                getClient().setScreen(new MainScreen(client));
            }
        });
        root.add(surrenderButton).bottom();

        getStage().addActor(root);

        super.show();
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
    }
}
