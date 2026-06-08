package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import dev.creoii.dungeoneer.client.AssetManager;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.network.c2s.character.DeleteCharacterC2S;

import java.util.ArrayList;

public class VaultThroneTab extends Tab {
    private int classIndex = 0;
    private int characterSlots;
    private java.util.List<Character> characters;
    private Label classLabel;
    private Table statsTable;
    private Stack[] classIcons;
    private Table carousel;
    private TextButton createCharacterButton;

    protected VaultThroneTab(Dungeoneer client, TextureRegion tabTexture) {
        super(client, tabTexture);
    }

    @Override
    public void init() {
        characterSlots = 1;
        characters = new ArrayList<>(characterSlots);
    }

    @Override
    protected void build() {
        add(new Label("Vault & Throne", getSkin())).pad(20).row();

        classIcons = new Stack[]{null, null, null};
        carousel = new Table();

        characterSlots = getClient().getState().getAccount().characterSlots();
        characters.addAll(getClient().getState().getCharacters());

        classLabel = new Label("", getSkin());
        statsTable = new Table();

        for (int i = 0; i < 3; i++) {
            classIcons[i] = new Stack();
            classIcons[i].add(new ImageButton(new NinePatchDrawable(AssetManager.TAB_9PATCH)));
            classIcons[i].add(new ImageButton(new TextureRegionDrawable(AssetManager.MISSING_TEXTURE)));
        }

        classIcons[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (characterSlots < 1)
                    return;

                classIndex--;

                if (classIndex < 0) {
                    classIndex = characterSlots - 1;
                }

                getClient().getState().setActiveCharacter(characters.get(classIndex));

                updateSelectedCharacter();
            }
        });

        classIcons[2].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (characterSlots < 1)
                    return;
                classIndex++;

                if (classIndex >= characterSlots) {
                    classIndex = 0;
                }

                getClient().getState().setActiveCharacter(characters.get(classIndex));

                updateSelectedCharacter();
            }
        });

        Table center = new Table();
        center.add(classIcons[1]).size(120).row();
        center.add(classLabel).padTop(10).row();
        center.add(statsTable).padTop(10);
        center.add();

        carousel.add(classIcons[0]).size(96).expandX().pad(10);
        carousel.add(center).expandX().pad(20);
        carousel.add(classIcons[2]).size(96).expandX().pad(10);

        add(carousel).growX().height(200).padBottom(20).row();

        createCharacterButton = new TextButton("Create Character", getSkin());
        createCharacterButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (characters.get(classIndex) == null) new CreateCharacterDialog(getClient(), classIndex, getSkin()).show(getStage());
                else getClient().get().sendUDP(new DeleteCharacterC2S(getClient().getState().getAccount().id(), classIndex));
            }
        });
        add(createCharacterButton);
    }

    @Override
    public void select() {
        characterSlots = getClient().getState().getAccount().characterSlots();

        characters.clear();
        characters.addAll(getClient().getState().getCharacters());

        carousel.setVisible(!characters.isEmpty());

        if (!characters.isEmpty()) {
            updateSelectedCharacter();
        } else updateCharacterDisplay();
    }

    private void updateStatsTable() {
        Character selected = characters.get(classIndex);

        statsTable.clearChildren();
        statsTable.add(new Label(String.format("Speed: %s", selected.characterClass().stats().speed()), getSkin()));
    }

    private void updateSelectedCharacter() {
        Character selected = characters.get(classIndex);

        if (selected != null) {
            createCharacterButton.setText("Delete Character");
            classLabel.setText(classIndex + ": " + selected.characterClass().id());
            statsTable.setVisible(true);
            updateStatsTable();
        } else {
            createCharacterButton.setText("Create Character");
            classLabel.setText(classIndex + ": Empty");
            statsTable.setVisible(false);
            updateStatsTable();
        }

        updateCharacterDisplay();
    }

    private void updateCharacterDisplay() {
        if (characters.isEmpty()) {
            classLabel.setText("");
            for (int i = 0; i < 3; i++) {
                classIcons[i].removeActorAt(1, true);
                Container<Image> container = new Container<>(new Image(new TextureRegionDrawable(AssetManager.MISSING_TEXTURE)));
                container.size(48f);
                classIcons[i].add(container);
            }
            return;
        }

        int left = characterSlots > 2 ? (classIndex - 1 + characterSlots) % characterSlots : -1;
        int center = classIndex;
        int right = characterSlots > 1 ? (classIndex + 1) % characterSlots : -1;

        int[] indices = {left, center, right};

        Character selected = getCharacterForSlot(center);

        createCharacterButton.setText(characters.get(classIndex) == null ? "Create Character" : "Delete Character");
        classLabel.setText(selected == null ? "Empty" : selected.characterClass().id());

        for (int i = 0; i < 3; i++) {
            Character character = getCharacterForSlot(indices[i]);
            classIcons[i].removeActorAt(1, true);
            TextureRegion texture = character == null ? AssetManager.CLASS_SILHOUETTE_TEXTURE : AssetManager.getClassTexture(character.characterClass().id());
            Container<Image> container = new Container<>(new Image(new TextureRegionDrawable(texture)));
            container.size(48f);
            classIcons[i].add(container);
        }

        classIcons[0].setVisible(characters.size() > 2);
        classIcons[2].setVisible(characters.size() > 1);
    }

    private Character getCharacterForSlot(int slot) {
        return slot < characters.size() ? characters.get(slot) : null;
    }
}
