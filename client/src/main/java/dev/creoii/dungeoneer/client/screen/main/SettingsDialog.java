package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.option.BooleanOption;
import dev.creoii.dungeoneer.client.option.IntegerOption;

public class SettingsDialog extends Dialog {
    private final Dungeoneer client;

    private final TextButton upButton;
    private final TextButton leftButton;
    private final TextButton downButton;
    private final TextButton rightButton;
    private final TextButton rotateLeftButton;
    private final TextButton rotateRightButton;
    private final Slider cameraRotationSpeedButton;
    private final CheckBox debugButton;

    private IntegerOption listeningFor;

    public SettingsDialog(Dungeoneer client, Skin skin) {
        super("Settings", skin);
        this.client = client;

        Table content = getContentTable();

        upButton = createKeyButton(client.getSettings().upKey(), skin);
        leftButton = createKeyButton(client.getSettings().leftKey(), skin);
        downButton = createKeyButton(client.getSettings().downKey(), skin);
        rightButton = createKeyButton(client.getSettings().rightKey(), skin);
        rotateLeftButton = createKeyButton(client.getSettings().rotateLeftKey(), skin);
        rotateRightButton = createKeyButton(client.getSettings().rotateRightKey(), skin);
        cameraRotationSpeedButton = createSlider(client.getSettings().cameraRotationSpeed(), 0f, 3f, 1f, skin);
        debugButton = createToggleButton(client.getSettings().debug(), skin);

        content.add("Move Up");
        content.add(upButton).padTop(5f).row();

        content.add("Move Left");
        content.add(leftButton).padTop(5f).row();

        content.add("Move Down");
        content.add(downButton).padTop(5f).row();

        content.add("Move Right");
        content.add(rightButton).padTop(5f).row();

        content.add("Rotate Left");
        content.add(rotateLeftButton).padTop(5f).row();

        content.add("Rotate Right");
        content.add(rotateRightButton).padTop(5f).row();

        content.add("Rotation Speed");
        content.add(cameraRotationSpeedButton).padTop(5f).row();

        content.add("Debug");
        content.add(debugButton).padTop(5f).row();

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
                    rotateLeftButton.setText(Input.Keys.toString(client.getSettings().rotateLeftKey().value()));
                    rotateRightButton.setText(Input.Keys.toString(client.getSettings().rotateRightKey().value()));

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

    private TextButton createKeyButton(IntegerOption option, Skin skin) {
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

    private Slider createSlider(IntegerOption option, float min, float max, float stepSize, Skin skin) {
        Slider button = new Slider(min, max, stepSize, false, skin);
        button.setValue(option.value().floatValue());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                option.setValue((int) button.getValue());
            }
        });
        return button;
    }

    private CheckBox createToggleButton(BooleanOption option, Skin skin) {
        CheckBox box = new CheckBox("", skin);
        box.setChecked(option.value());
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                option.setValue(box.isChecked());
            }
        });
        return box;
    }
}
