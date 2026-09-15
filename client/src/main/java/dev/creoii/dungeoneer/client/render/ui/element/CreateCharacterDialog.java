package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.network.c2s.character.CreateCharacterC2S;

public class CreateCharacterDialog extends Dialog {
    private final Dungeoneer client;
    private final int classIndex;
    private final Table classBox;
    private CharacterClassButton selectedButton;
    private boolean changingSelection;

    public CreateCharacterDialog(Dungeoneer client, int classIndex) {
        super("Create Character", AbstractScreen.SKIN);
        this.client = client;
        this.classIndex = classIndex;
        classBox = new Table(AbstractScreen.SKIN);

        int i = 0;
        for (CharacterClass characterClass : DataManager.getClasses().values().stream().map(identifiable -> (CharacterClass) identifiable).toList()) {
            CharacterClassButton button = new CharacterClassButton(client, characterClass);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (changingSelection || !button.getActor().isChecked()) return;

                    changingSelection = true;
                    if (selectedButton != null) {
                        selectedButton.getActor().setChecked(false);
                        selectedButton.getActor().setColor(Color.WHITE);
                    }
                    selectedButton = button;
                    selectedButton.getActor().setColor(Color.GRAY);

                    changingSelection = false;
                }
            });
            classBox.add(button).size(32f).pad(8f);

            if (++i % 3 == 0)
                classBox.row();
        }

        getContentTable().add(new Label("Select Class", AbstractScreen.SKIN)).pad(10f).row();
        getContentTable().add(classBox).width(200);

        button("Create", true);
        button("Cancel", false);

        pack();
    }

    @Override
    protected void result(Object object) {
        if (Boolean.TRUE.equals(object) && selectedButton != null) {
            client.get().sendTCP(new CreateCharacterC2S(client.getState().getAccount().id(), classIndex, selectedButton.getCharacterClass()));
        }
    }
}
