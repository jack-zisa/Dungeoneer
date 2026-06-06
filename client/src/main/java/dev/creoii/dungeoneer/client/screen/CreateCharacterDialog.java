package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.network.c2s.CreateCharacterC2S;

public class CreateCharacterDialog extends Dialog {
    private final Dungeoneer client;
    private final SelectBox<CharacterClass> classBox;

    public CreateCharacterDialog(Dungeoneer client, Skin skin) {
        super("Create Character", skin);
        this.client = client;
        classBox = new SelectBox<>(skin);
        classBox.setItems(CharacterClass.VALUES);

        getContentTable().add(new Label("Class:", skin)).pad(10);
        getContentTable().add(classBox).width(200);

        button("Create", true);
        button("Cancel", false);

        pack();
    }

    @Override
    protected void result(Object object) {
        if (Boolean.TRUE.equals(object)) {
            CharacterClass selected = classBox.getSelected();
            client.get().sendUDP(new CreateCharacterC2S(client.getState().getAccount().id(), selected));
        }
    }
}
