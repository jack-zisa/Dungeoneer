package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import dev.creoii.dungeoneer.client.AssetManager;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.screen.RaidLoadingScreen;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.network.c2s.raid.RequestRaidTargetC2S;

public class PlayTab extends Tab {
    private Character selected;
    private Image selectedImage;
    private TextTooltip characterTooltip;

    protected PlayTab(Dungeoneer client, TextureRegion tabTexture) {
        super(client, tabTexture);
    }

    @Override
    protected void build() {
        selected = getClient().getState().getSelectedCharacter();
        TextTooltip.TextTooltipStyle tooltipStyle = new TextTooltip.TextTooltipStyle();
        tooltipStyle.label = getSkin().get(Label.LabelStyle.class);
        tooltipStyle.background = TAB_BACKGROUND;

        Table statsTable = new Table();

        Label powerLabel = new Label(String.format("Power: %s", 0), getSkin());
        Image goldImage = new Image(AssetManager.GOLD_TEXTURE);
        Label goldLabel = new Label(String.format("%s", 0), getSkin());
        Image gemImage = new Image(AssetManager.GEM_TEXTURE);
        Label gemsLabel = new Label(String.format("%s", 0), getSkin());

        statsTable.add(powerLabel).left().pad(10f);
        statsTable.add(goldImage).size(24f).left().pad(10f);
        statsTable.add(goldLabel).left().pad(10f);
        statsTable.add(gemImage).size(24f).left().pad(10f);
        statsTable.add(gemsLabel).left().pad(10f);

        Table accountTable = new Table();
        String classId = selected == null ? "" : selected.characterClass().id();
        selectedImage = new Image(AssetManager.getClassTexture(classId));
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
        TextButton raidButton = new TextButton("Raid", getSkin());
        raidButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().setScreen(new RaidLoadingScreen(getClient()));
                getClient().getState().setStatus(ClientState.Status.RAID_SEARCHING);
                getClient().get().sendUDP(new RequestRaidTargetC2S(getClient().getState().getAccount()));
            }
        });
        mainSection.add(raidButton).size(120f, 80f);
        add(mainSection).expand().fill();
    }

    @Override
    public void select() {
        selected = getClient().getState().getSelectedCharacter();
        String classId = selected == null ? "" : selected.characterClass().id();
        selectedImage.setDrawable(new TextureRegionDrawable(AssetManager.getClassTexture(classId)));
        characterTooltip.getActor().setText(classId);
    }
}
