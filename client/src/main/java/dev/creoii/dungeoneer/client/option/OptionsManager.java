package dev.creoii.dungeoneer.client.option;

import com.badlogic.gdx.Input;

public final class OptionsManager {
    public static final IntOption UP_KEY = new IntOption("key_forwards", Input.Keys.W);
    public static final IntOption DOWN_KEY = new IntOption("key_backwards", Input.Keys.S);
    public static final IntOption LEFT_KEY = new IntOption("key_left", Input.Keys.A);
    public static final IntOption RIGHT_KEY = new IntOption("key_right", Input.Keys.D);

    public static boolean isMovementKey(int keycode) {
        return keycode == UP_KEY.value() || keycode == DOWN_KEY.value() || keycode == LEFT_KEY.value() || keycode == RIGHT_KEY.value();
    }
}
