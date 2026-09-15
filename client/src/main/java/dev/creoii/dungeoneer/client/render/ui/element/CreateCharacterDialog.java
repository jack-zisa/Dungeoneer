package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.network.c2s.character.CreateCharacterC2S;
import dev.creoii.dungeoneer.util.Identifiable;

public class CreateCharacterDialog extends Dialog {
    private final Dungeoneer client;
    private final int classIndex;
    private final SelectBox<String> classBox;

    public CreateCharacterDialog(Dungeoneer client, int classIndex) {
        super("Create Character", AbstractScreen.SKIN);
        this.client = client;
        this.classIndex = classIndex;
        classBox = new SelectBox<>(AbstractScreen.SKIN);
        classBox.setItems(DataManager.getClasses().values().stream().map(Identifiable::id).toArray(String[]::new));

        getContentTable().add(new Label("Class:", AbstractScreen.SKIN)).pad(10);
        getContentTable().add(classBox).width(200);

        button("Create", true);
        button("Cancel", false);

        pack();
    }

    @Override
    protected void result(Object object) {
        if (Boolean.TRUE.equals(object)) {
            CharacterClass selected = DataManager.getCharacterClass(classBox.getSelected());
            if (selected != null)
                client.get().sendTCP(new CreateCharacterC2S(client.getState().getAccount().id(), classIndex, selected));
        }
    }
}
