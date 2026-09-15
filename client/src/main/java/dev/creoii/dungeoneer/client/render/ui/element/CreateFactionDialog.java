package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.network.c2s.faction.CreateFactionC2S;

public class CreateFactionDialog extends Dialog {
    private final Dungeoneer client;
    private final TextField nameField;
    private final TextArea descriptionField;

    public CreateFactionDialog(Dungeoneer client) {
        super("Create Faction", AbstractScreen.SKIN);
        this.client = client;

        nameField = new TextField("", AbstractScreen.SKIN);
        descriptionField = new TextArea("", AbstractScreen.SKIN);

        getContentTable().add(new Label("Name:", AbstractScreen.SKIN)).height(40f);
        getContentTable().add(nameField).size(200f, 40f).row();
        getContentTable().add(new Label("Description:", AbstractScreen.SKIN)).height(40f);
        getContentTable().add(descriptionField).size(200f, 120f);

        button("Create", true);
        button("Cancel", false);

        pack();
    }

    @Override
    protected void result(Object object) {
        if (Boolean.TRUE.equals(object)) {
            client.get().sendTCP(new CreateFactionC2S(client.getState().getAccount(), nameField.getText(), descriptionField.getText()));
        }
    }
}
