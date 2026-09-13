package dev.creoii.dungeoneer.client.render.ui.screen.main;

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
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.render.ui.element.CreateCharacterDialog;
import dev.creoii.dungeoneer.client.render.ui.element.StatsDisplay;
import dev.creoii.dungeoneer.client.render.ui.element.InventoryWidget;
import dev.creoii.dungeoneer.definitions.item.inventory.Inventory;
import dev.creoii.dungeoneer.network.c2s.character.DeleteCharacterC2S;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

public class VaultThroneTab extends Tab {
    private int classIndex;
    private int characterSlots;
    private CheckBox favoriteButton;
    private Label classLabel;
    private InventoryWidget equipment;
    private StatsDisplay stats;
    private Stack[] classIcons;
    private Table carousel;
    private TextButton createCharacterButton;

    protected VaultThroneTab(Dungeoneer client, Texture tabTexture) {
        super(client, tabTexture);
    }

    @Override
    public void init() {
        characterSlots = 1;
    }

    @Override
    protected void build() {
        add(new Label("Vault & Throne", getSkin())).pad(20).row();

        int favoriteCharacterIndex = getClient().getSettings().favoriteCharacter().value();

        classIndex = favoriteCharacterIndex == -1 ? 0 : favoriteCharacterIndex;
        classIcons = new Stack[]{null, null, null, null, null};
        carousel = new Table();

        characterSlots = getClient().getState().getAccount().characterSlots();

        favoriteButton = new CheckBox("", getSkin());
        CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle(getSkin().get(CheckBox.CheckBoxStyle.class));
        style.checkboxOn = new TextureRegionDrawable(getClient().getAssets().getTexture(Assets.Atlas.UI, "heart"));
        style.checkboxOff = new TextureRegionDrawable(getClient().getAssets().getTexture(Assets.Atlas.UI, "heart_disabled"));
        favoriteButton.setStyle(style);
        favoriteButton.setProgrammaticChangeEvents(false);
        favoriteButton.setChecked(favoriteCharacterIndex != -1 && classIndex == favoriteCharacterIndex);
        favoriteButton.setDisabled(getClient().getState().getCharacters().size() < 2);
        favoriteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((CheckBox) actor).isChecked()) getClient().getSettings().favoriteCharacter().setValue(classIndex);
                else getClient().getSettings().favoriteCharacter().setValue(-1);
                getClient().getSettings().save();
            }
        });
        classLabel = new Label("", getSkin());
        equipment = new InventoryWidget(getClient(), getClient().getState().getActiveCharacter().isNull() ? new Inventory(4) : getClient().getState().getActiveCharacter().getEquipment(), 4);
        stats = new StatsDisplay(null, null);

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
                updateSelectedCharacter();
            }
        });

        classIcons[1].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (characterSlots < 1)
                    return;

                classIndex = (classIndex - 1 + characterSlots) % characterSlots;
                updateSelectedCharacter();
            }
        });

        classIcons[3].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (characterSlots < 1)
                    return;

                classIndex = (classIndex + 1) % characterSlots;
                updateSelectedCharacter();
            }
        });

        classIcons[4].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (characterSlots < 1)
                    return;

                classIndex = (classIndex + 2) % characterSlots;
                updateSelectedCharacter();
            }
        });

        Table center = new Table();
        center.setBackground(new NinePatchDrawable(Assets.TAB_9PATCH));
        center.add(favoriteButton).size(16f, 16f).left().row();
        center.add(classIcons[2]).size(120).row();
        center.add(classLabel).padTop(8f).row();
        center.add(equipment).padTop(8f).row();
        center.add(stats).padTop(8f).row();

        createCharacterButton = new TextButton("Create", getSkin());
        createCharacterButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (getClient().getState().getCharacters().get(classIndex).isNull()) new CreateCharacterDialog(getClient(), classIndex, getSkin()).show(getStage());
                else getClient().get().sendTCP(new DeleteCharacterC2S(getClient().getState().getAccount().id(), classIndex));
            }
        });
        center.add(createCharacterButton).padTop(8f);

        carousel.add(classIcons[0]).size(96).expandX().pad(10);
        carousel.add(classIcons[1]).size(96).expandX().pad(10);
        carousel.add(center).expandX().pad(20);
        carousel.add(classIcons[3]).size(96).expandX().pad(10);
        carousel.add(classIcons[4]).size(96).expandX().pad(10);

        add(carousel).growX().height(200).padBottom(20).row();
    }

    @Nullable
    public ClientCharacter getSelectedCharacter() {
        return getClient().getState().getCharacter(classIndex);
    }

    public void select(int selectedIndex) {
        classIndex = selectedIndex;
        select();
    }

    @Override
    public void select() {
        characterSlots = getClient().getState().getAccount().characterSlots();

        if (classIndex >= characterSlots) {
            classIndex = getClient().getSettings().favoriteCharacter().value();
            ClientCharacter favorite = getSelectedCharacter();
            if (favorite == null || favorite.isNull() || favorite.isDead()) {
                classIndex = 0;
                getClient().getSettings().favoriteCharacter().setValue(0);
            }
        }

        boolean hasCharacters = getClient().getState().getCharacters().values().stream().anyMatch(character -> !character.isNull() && !character.isDead());

        carousel.setVisible(hasCharacters);

        if (hasCharacters) {
            updateSelectedCharacter();
        } else updateCharacterDisplay();
    }

    private void updateStatsTable() {
        ClientCharacter selected = getSelectedCharacter();
        if (selected != null && !selected.isNull()) {
            StatContainer baseStats = selected.get().characterClass().baseStats();
            StatContainer maxStats = selected.get().characterClass().maxStats();
            stats.refresh(baseStats, maxStats);
        }
    }

    private void updateEquipment() {
        ClientCharacter selected = getSelectedCharacter();
        if (selected != null && !selected.isNull()) {
            Inventory inventory = selected.getEquipment();
            equipment.refresh(inventory);
            equipment.setVisible(true);
        } else equipment.setVisible(false);
    }

    private void updateSelectedCharacter() {
        updateEquipment();
        updateStatsTable();

        ClientCharacter selected = getSelectedCharacter();
        if (selected != null && !selected.isNull()) {
            createCharacterButton.setText("Delete");
            classLabel.setText(classIndex + ": " + selected.get().characterClass().id());
            stats.setVisible(true);
        } else {
            createCharacterButton.setText("Create");
            classLabel.setText(classIndex + ": Empty");
            stats.setVisible(false);
        }

        getClient().getState().setActiveCharacter(classIndex);
        updateCharacterDisplay();
    }

    private void updateCharacterDisplay() {
        boolean hasCharacters = getClient().getState().getCharacters().values().stream().anyMatch(character -> !character.isNull() && !character.isDead());

        if (!hasCharacters) {
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

        Collection<ClientCharacter> clientCharacters = getClient().getState().getCharacters().values();

        ClientCharacter selected = getClient().getState().getCharacter(center);
        favoriteButton.setDisabled(clientCharacters.stream().filter(Objects::nonNull).count() < 2 || selected.isNull());
        favoriteButton.setChecked(favoriteCharacterIndex != -1 && classIndex == favoriteCharacterIndex);
        createCharacterButton.setText(getSelectedCharacter().isNull() ? "Create" : "Delete");
        classLabel.setText(selected.isNull() ? "Empty" : selected.get().characterClass().id());

        for (int i = 0; i < classIcons.length; i++) {
            ClientCharacter character = getClient().getState().getCharacter(indices[i]);
            classIcons[i].removeActorAt(1, true);
            Texture texture = character.isNull() ? getClient().getAssets().getTexture(Assets.Atlas.CHARACTER, "silhouette") : getClient().getAssets().getTexture(Assets.Atlas.CHARACTER, character.get().characterClass().id());
            Container<Image> container = new Container<>(new Image(new TextureRegionDrawable(texture)));
            container.size(48f);
            classIcons[i].add(container);
        }

        classIcons[0].setVisible(clientCharacters.size() > 4);
        classIcons[1].setVisible(clientCharacters.size() > 2);
        classIcons[3].setVisible(clientCharacters.size() > 1);
        classIcons[4].setVisible(clientCharacters.size() > 3);
    }
}
