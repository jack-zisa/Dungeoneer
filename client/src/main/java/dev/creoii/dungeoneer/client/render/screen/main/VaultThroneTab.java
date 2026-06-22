package dev.creoii.dungeoneer.client.render.screen.main;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.network.c2s.character.DeleteCharacterC2S;

import java.util.ArrayList;
import java.util.Objects;

public class VaultThroneTab extends Tab {
    private int classIndex;
    private int characterSlots;
    private java.util.List<CharacterDefinition> characters;
    private CheckBox favoriteButton;
    private Label classLabel;
    private Table statsTable;
    private Stack[] classIcons;
    private Table carousel;
    private TextButton createCharacterButton;

    protected VaultThroneTab(Dungeoneer client, Texture tabTexture) {
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

        int favoriteCharacterIndex = getClient().getSettings().favoriteCharacter().value();

        classIndex = favoriteCharacterIndex == -1 ? 0 : favoriteCharacterIndex;
        classIcons = new Stack[]{null, null, null, null, null};
        carousel = new Table();

        characterSlots = getClient().getState().getAccount().characterSlots();
        characters.addAll(getClient().getState().getCharacters());

        favoriteButton = new CheckBox("", getSkin());
        CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle(getSkin().get(CheckBox.CheckBoxStyle.class));
        style.checkboxOn = new TextureRegionDrawable(getClient().getAssets().getTexture(Assets.Atlas.UI, "heart"));
        style.checkboxOff = new TextureRegionDrawable(getClient().getAssets().getTexture(Assets.Atlas.UI, "heart_disabled"));
        favoriteButton.setStyle(style);
        favoriteButton.setProgrammaticChangeEvents(false);
        favoriteButton.setChecked(favoriteCharacterIndex != -1 && classIndex == favoriteCharacterIndex);
        favoriteButton.setDisabled(characters.size() < 2);
        favoriteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((CheckBox) actor).isChecked()) getClient().getSettings().favoriteCharacter().setValue(classIndex);
                else getClient().getSettings().favoriteCharacter().setValue(-1);
                getClient().getSettings().save();
            }
        });
        classLabel = new Label("", getSkin());
        statsTable = new Table();

        for (int i = 0; i < classIcons.length; i++) {
            classIcons[i] = new Stack();
            classIcons[i].add(new ImageButton(new NinePatchDrawable(Assets.TAB_9PATCH)));
            classIcons[i].add(new ImageButton(new TextureRegionDrawable(Assets.MISSING_TEXTURE)));
        }

        classIcons[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (characterSlots < 1)
                    return;

                classIndex = (classIndex - 2 + characterSlots) % characterSlots;

                getClient().getState().setActiveCharacter(characters.get(classIndex));
                updateSelectedCharacter();
            }
        });

        classIcons[1].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (characterSlots < 1)
                    return;

                classIndex = (classIndex - 1 + characterSlots) % characterSlots;

                getClient().getState().setActiveCharacter(characters.get(classIndex));
                updateSelectedCharacter();
            }
        });

        classIcons[3].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (characterSlots < 1)
                    return;

                classIndex = (classIndex + 1) % characterSlots;

                getClient().getState().setActiveCharacter(characters.get(classIndex));
                updateSelectedCharacter();
            }
        });

        classIcons[4].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (characterSlots < 1)
                    return;

                classIndex = (classIndex + 2) % characterSlots;

                getClient().getState().setActiveCharacter(characters.get(classIndex));
                updateSelectedCharacter();
            }
        });

        Table center = new Table();
        center.setBackground(new NinePatchDrawable(Assets.TAB_9PATCH));
        center.add(favoriteButton).size(16f, 16f).left().row();
        center.add(classIcons[2]).size(120).row();
        center.add(classLabel).padTop(10).row();
        center.add(statsTable).padTop(10);
        center.add();

        carousel.add(classIcons[0]).size(96).expandX().pad(10);
        carousel.add(classIcons[1]).size(96).expandX().pad(10);
        carousel.add(center).expandX().pad(20);
        carousel.add(classIcons[3]).size(96).expandX().pad(10);
        carousel.add(classIcons[4]).size(96).expandX().pad(10);

        add(carousel).growX().height(200).padBottom(20).row();

        createCharacterButton = new TextButton("Create Character", getSkin());
        createCharacterButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (characters.get(classIndex) == null) new CreateCharacterDialog(getClient(), classIndex, getSkin()).show(getStage());
                else getClient().get().sendTCP(new DeleteCharacterC2S(getClient().getState().getAccount().id(), classIndex));
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
        CharacterDefinition selected = characters.get(classIndex);
        statsTable.clearChildren();

        if (selected != null) {
            statsTable.add(new Label(String.format("Health: %s", selected.characterClass().baseStats().health()), getSkin())).row();
            statsTable.add(new Label(String.format("Speed: %s", selected.characterClass().baseStats().speed()), getSkin()));
            statsTable.add(new Label(String.format("Attack Speed: %s", selected.characterClass().baseStats().attackSpeed()), getSkin()));
        }
    }

    private void updateSelectedCharacter() {
        CharacterDefinition selected = characters.get(classIndex);
        if (selected != null) {
            createCharacterButton.setText("Delete Character");
            classLabel.setText(classIndex + ": " + selected.characterClass().id());
            statsTable.setVisible(true);
            updateStatsTable();
            getClient().getState().setActiveCharacter(selected);
        } else {
            createCharacterButton.setText("Create Character");
            classLabel.setText(classIndex + ": Empty");
            statsTable.setVisible(false);
            updateStatsTable();
            getClient().getState().setActiveCharacter(null);
        }

        updateCharacterDisplay();
    }

    private void updateCharacterDisplay() {
        if (characters.isEmpty()) {
            classLabel.setText("");
            for (int i = 0; i < 5; i++) {
                classIcons[i].removeActorAt(1, true);
                Container<Image> container = new Container<>(new Image(new TextureRegionDrawable(getClient().getAssets().getTexture(Assets.Atlas.CHARACTER, "silhouette"))));
                container.size(48f);
                classIcons[i].add(container);
            }
            return;
        }

        int leftLeft = characterSlots > 4 ? (classIndex - 2 + characterSlots) % characterSlots : -1;
        int left = characterSlots > 2 ? (classIndex - 1 + characterSlots) % characterSlots : -1;
        int center = classIndex;
        int right = characterSlots > 1 ? (classIndex + 1) % characterSlots : -1;
        int rightRight = characterSlots > 3 ? (classIndex + 2) % characterSlots : -1;

        int[] indices = {leftLeft, left, center, right, rightRight};
        int favoriteCharacterIndex = getClient().getSettings().favoriteCharacter().value();

        CharacterDefinition selected = getCharacterForSlot(center);
        favoriteButton.setDisabled(characters.stream().filter(Objects::nonNull).count() < 2 || selected == null);
        favoriteButton.setChecked(favoriteCharacterIndex != -1 && classIndex == favoriteCharacterIndex);
        createCharacterButton.setText(characters.get(classIndex) == null ? "Create Character" : "Delete Character");
        classLabel.setText(selected == null ? "Empty" : selected.characterClass().id());

        for (int i = 0; i < classIcons.length; i++) {
            CharacterDefinition character = getCharacterForSlot(indices[i]);
            classIcons[i].removeActorAt(1, true);
            Texture texture = character == null ? getClient().getAssets().getTexture(Assets.Atlas.CHARACTER, "silhouette") : getClient().getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id());
            Container<Image> container = new Container<>(new Image(new TextureRegionDrawable(texture)));
            container.size(48f);
            classIcons[i].add(container);
        }

        classIcons[0].setVisible(characters.size() > 4);
        classIcons[1].setVisible(characters.size() > 2);
        classIcons[3].setVisible(characters.size() > 1);
        classIcons[4].setVisible(characters.size() > 3);
    }

    private CharacterDefinition getCharacterForSlot(int slot) {
        return slot < characters.size() ? characters.get(slot) : null;
    }
}
