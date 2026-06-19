package dev.creoii.dungeoneer.client.screen.game;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.network.c2s.raid.CancelJoinRaidC2S;

public class RaidLoadingScreen extends AbstractScreen {
    private Label targetLabel;
    private Label attackersLabel;

    public RaidLoadingScreen(Dungeoneer client) {
        super(client);
    }

    @Override
    public void show() {
        Table root = new Table();
        root.setFillParent(true);

        targetLabel = new Label("", SKIN);
        attackersLabel = new Label("", SKIN);

        Label title = new Label("Dungeoneer", SKIN);
        Label loadingLabel = new Label("Searching...", SKIN);

        root.add(title).padBottom(30).row();
        root.add(targetLabel).row();
        root.add(attackersLabel).row();
        root.add(loadingLabel).row();

        TextButton cancelButton = new TextButton("Cancel", SKIN);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().setScreen(new MainScreen(getClient()));
                getClient().getState().setStatus(ClientState.Status.LOBBY);
                getClient().get().sendTCP(new CancelJoinRaidC2S(getClient().getState().getAccount().id(), getClient().getState().getCurrentRaid().get().id()));
            }
        });
        root.add(cancelButton);

        getStage().addActor(root);
        getClient().getInputMultiplexer().addProcessor(getStage());

        super.show();
    }

    @Override
    public void hide() {
        getClient().getInputMultiplexer().removeProcessor(getStage());
    }

    public Label getAttackersLabel() {
        return attackersLabel;
    }

    public Label getTargetLabel() {
        return targetLabel;
    }

    @Override
    public void dispose() {
        super.dispose();
        SKIN.dispose();
    }
}
