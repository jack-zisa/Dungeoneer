package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.screen.editor.DungeonEditorScreen;
import dev.creoii.dungeoneer.client.screen.game.RaidLoadingScreen;
import dev.creoii.dungeoneer.network.c2s.raid.RequestRaidTargetC2S;

public class PlayTab extends Tab {
    private ClientCharacter selected;
    private Image selectedImage;
    private TextTooltip characterTooltip;
    private TextButton raidButton;

    protected PlayTab(Dungeoneer client, Texture tabTexture) {
        super(client, tabTexture);
    }

    @Override
    protected void build() {
        selected = getClient().getState().getActiveCharacter();
        TextTooltip.TextTooltipStyle tooltipStyle = new TextTooltip.TextTooltipStyle();
        tooltipStyle.label = getSkin().get(Label.LabelStyle.class);
        tooltipStyle.background = TAB_BACKGROUND;

        Table statsTable = new Table();

        Image goldImage = new Image(getClient().getAssets().getTexture(Assets.Atlas.UI, "gold"));
        Label goldLabel = new Label(String.format("%s", getClient().getState().getAccount().gold()), getSkin());
        Image gemImage = new Image(getClient().getAssets().getTexture(Assets.Atlas.UI, "gem"));
        Label gemsLabel = new Label(String.format("%s", getClient().getState().getAccount().gems()), getSkin());

        statsTable.add(goldImage).size(24f).left();
        statsTable.add(goldLabel).left().padLeft(5f).padRight(30f);
        statsTable.add(gemImage).size(24f).left();
        statsTable.add(gemsLabel).left().padLeft(5f);

        Table accountTable = new Table();
        String classId = selected.isNull() ? "" : selected.get().characterClass().id();
        selectedImage = new Image(getClient().getAssets().getTexture(Assets.Atlas.CHARACTER, classId));
        characterTooltip = new TextTooltip(classId, tooltipStyle);
        characterTooltip.setInstant(true);
        selectedImage.addListener(characterTooltip);

        accountTable.add(selectedImage).size(32f).pad(10f);
        accountTable.add(new Label(getClient().getState().getAccount().username(), getSkin())).left().pad(10f);

        TextButton settingsButton = new TextButton("Settings", getSkin());
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                new SettingsDialog(getClient(), getSkin()).show(getStage());
            }
        });
        accountTable.add(settingsButton).pad(10f);

        add(statsTable).width(96f).height(32f).top().expandX().fillX().row();
        add(accountTable).width(96f).height(32f).top().expandX().fillX().row();

        Table mainSection = new Table();
        raidButton = new TextButton("Raid", getSkin());
        raidButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().setScreen(new RaidLoadingScreen(getClient()));
                getClient().getState().setStatus(ClientState.Status.RAID_SEARCHING);
                getClient().get().sendUDP(new RequestRaidTargetC2S(getClient().getState().getAccount()));
            }
        });
        mainSection.add(raidButton).size(120f, 80f).row();
        TextButton buildButton = new TextButton("Build Dungeon", getSkin());
        buildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().setScreen(new DungeonEditorScreen(getClient()));
                getClient().getState().setStatus(ClientState.Status.EDITING_DUNGEON);
            }
        });
        mainSection.add(buildButton).size(120f, 40f);
        add(mainSection).expand().fill();
    }

    @Override
    public void select() {
        selected.set(getClient().getState().getCharacters().get(getClient().getSettings().favoriteCharacter().value()));
        String classId = selected.isNull() ? "" : selected.get().characterClass().id();
        selectedImage.setDrawable(new TextureRegionDrawable(getClient().getAssets().getTexture(Assets.Atlas.CHARACTER, classId)));
        characterTooltip.getActor().setText(classId);
        raidButton.setDisabled(selected.isNull());
    }
}
