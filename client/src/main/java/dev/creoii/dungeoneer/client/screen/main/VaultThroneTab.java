package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import dev.creoii.dungeoneer.client.AssetManager;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.Character;

import java.util.ArrayList;

public class VaultThroneTab extends Tab {
    private int classIndex = 0;
    private java.util.List<Character> characters;
    private Label classLabel;
    private Stack[] classIcons;
    private Table carousel;

    protected VaultThroneTab(Dungeoneer client, TextureRegion tabTexture) {
        super(client, tabTexture);
    }

    @Override
    public void init() {
        characters = new ArrayList<>();
    }

    @Override
    protected void build() {
        add(new Label("Vault & Throne", getSkin())).pad(20).row();

        classIcons = new Stack[]{null, null, null};
        carousel = new Table();

        characters.addAll(getClient().getState().getCharacters());

        classLabel = new Label("", getSkin());
        for (int i = 0; i < 3; i++) {
            classIcons[i] = new Stack();
            classIcons[i].add(new Image(new NinePatchDrawable(AssetManager.TAB_9PATCH)));
            classIcons[i].add(new Image(new TextureRegionDrawable(AssetManager.MISSING_TEXTURE)));
        }

        TextButton previous = new TextButton("<", getSkin());
        TextButton next = new TextButton(">", getSkin());

        previous.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (characters.isEmpty())
                    return;

                classIndex--;

                if (classIndex < 0) {
                    classIndex = characters.size() - 1;
                }

                Character selected = characters.get(classIndex);
                getClient().getState().setSelectedCharacter(selected);
                classLabel.setText(classIndex + ": " + selected.characterClass().id());

                updateCharacterDisplay();
            }
        });

        next.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (characters.isEmpty())
                    return;
                classIndex++;

                if (classIndex >= characters.size()) {
                    classIndex = 0;
                }

                Character selected = characters.get(classIndex);
                getClient().getState().setSelectedCharacter(selected);
                classLabel.setText(classIndex + ": " + selected.characterClass().id());
                updateCharacterDisplay();
            }
        });

        Table center = new Table();
        center.add(classIcons[1]).size(120).row();
        center.add(classLabel).padTop(10);

        carousel.add(previous).width(40);
        carousel.add(classIcons[0]).size(96).expandX().pad(10);
        carousel.add(center).expandX().pad(20);
        carousel.add(classIcons[2]).size(96).expandX().pad(10);
        carousel.add(next).width(40);

        add(carousel).growX().height(200).padBottom(20).row();

        TextButton button = new TextButton("Create Character", getSkin());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                new CreateCharacterDialog(getClient(), getSkin()).show(getStage());
            }
        });
        add(button);
    }

    @Override
    public void select() {
        characters.clear();
        characters.addAll(getClient().getState().getCharacters());

        carousel.setVisible(!characters.isEmpty());

        if (!characters.isEmpty()) {
            classLabel.setText(classIndex + ": " + characters.get(classIndex).characterClass().id());
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

        int size = characters.size();

        int left = size > 2 ? (classIndex - 1 + size) % size : -1;
        int center = classIndex;
        int right = size > 1 ? (classIndex + 1) % size : -1;

        int[] indices = {left, center, right};

        classLabel.setText(characters.get(center).characterClass().id());
        for (int i = 0; i < 3; i++) {
            if (indices[i] == -1)
                continue;
            classIcons[i].removeActorAt(1, true);
            Container<Image> container = new Container<>(new Image(new TextureRegionDrawable(AssetManager.getClassTexture(characters.get(indices[i]).characterClass().id()))));
            container.size(48f);
            classIcons[i].add(container);
        }

        classIcons[0].setVisible(characters.size() > 2);
        classIcons[2].setVisible(characters.size() > 1);
    }
}
