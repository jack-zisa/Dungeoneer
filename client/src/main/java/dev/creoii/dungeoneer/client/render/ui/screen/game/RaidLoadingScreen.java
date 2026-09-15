package dev.creoii.dungeoneer.client.render.ui.screen.game;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.ui.element.ShaderImage;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.render.ui.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.network.c2s.raid.CancelJoinRaidC2S;

public class RaidLoadingScreen extends AbstractScreen {
    private Label targetLabel;
    private Label attackersLabel;
    private Table joinedCharacters;

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

        root.add(title).padBottom(30f).row();
        root.add(targetLabel).padBottom(8f).row();
        root.add(attackersLabel).row();

        joinedCharacters = new Table();
        joinedCharacters.setBackground(new NinePatchDrawable(Assets.TAB_9PATCH));
        root.add(joinedCharacters).pad(10f).row();

        root.add(loadingLabel).padBottom(8f).row();

        TextButton cancelButton = new TextButton("Cancel", SKIN);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!getClient().getState().getCurrentRaid().isNull())
                    getClient().get().sendTCP(new CancelJoinRaidC2S(getClient().getState().getAccount().id(), getClient().getState().getCurrentRaid().get().id()));
                getClient().setScreen(new MainScreen(getClient()));
                getClient().getState().setStatus(ClientState.Status.LOBBY);
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

    @Override
    public void dispose() {
        super.dispose();
        SKIN.dispose();
    }

    public void sync(RaidDefinition raid) {
        targetLabel.setText(String.format("Target: %s", raid.target().username()));
        attackersLabel.setText(String.format("%s / %s Attackers", raid.attackers().size(), raid.requiredCharacters()));

        ShaderImage[] images = new ShaderImage[raid.requiredCharacters()];

        int i = 0;
        for (CharacterDefinition character : raid.characters()) {
            images[i++] = new ShaderImage(getClient().getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()), Assets.BORDER_SHADER);
        }

        for (; i < raid.requiredCharacters(); i++) {
            images[i] = new ShaderImage(getClient().getAssets().getTexture(Assets.Atlas.CHARACTER, "silhouette"), Assets.BORDER_SHADER);
        }

        joinedCharacters.clearChildren();
        for (ShaderImage image : images) {
            joinedCharacters.add(image).size(64).pad(5);
        }
    }
}
