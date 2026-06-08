package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.option.IntOption;

public class SettingsDialog extends Dialog {
    private final Dungeoneer client;

    private final TextButton upButton;
    private final TextButton leftButton;
    private final TextButton downButton;
    private final TextButton rightButton;

    private IntOption listeningFor;

    public SettingsDialog(Dungeoneer client, Skin skin) {
        super("Settings", skin);
        this.client = client;

        Table content = getContentTable();

        upButton = createKeyButton(client.getSettings().upKey(), skin);
        leftButton = createKeyButton(client.getSettings().leftKey(), skin);
        downButton = createKeyButton(client.getSettings().downKey(), skin);
        rightButton = createKeyButton(client.getSettings().rightKey(), skin);

        content.add("Move Up");
        content.add(upButton).padTop(5f).row();

        content.add("Move Left");
        content.add(leftButton).padTop(5f).row();

        content.add("Move Down");
        content.add(downButton).padTop(5f).row();

        content.add("Move Right");
        content.add(rightButton).padTop(5f).row();

        button("Apply", true);
        button("Cancel", false);

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (listeningFor != null) {
                    listeningFor.setValue(keycode);

                    upButton.setText(Input.Keys.toString(client.getSettings().upKey().value()));
                    leftButton.setText(Input.Keys.toString(client.getSettings().leftKey().value()));
                    downButton.setText(Input.Keys.toString(client.getSettings().downKey().value()));
                    rightButton.setText(Input.Keys.toString(client.getSettings().rightKey().value()));

                    listeningFor = null;
                    return true;
                }

                return false;
            }
        });

        pack();
    }

    @Override
    protected void result(Object object) {
        if (Boolean.TRUE.equals(object)) {
            client.getSettings().save();
        }
    }

    private TextButton createKeyButton(IntOption option, Skin skin) {
        TextButton button = new TextButton(Input.Keys.toString(option.value()), skin);

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                listeningFor = option;
                button.setText("Press key...");
            }
        });

        return button;
    }
}
