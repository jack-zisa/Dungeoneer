package dev.creoii.dungeoneer.client.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public final class InputUtils {
    public static boolean isCtrl() {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }

    public static boolean isShift() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }
}
