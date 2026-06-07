package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.InputAdapter;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.option.OptionsManager;
import dev.creoii.dungeoneer.network.c2s.character.CharacterMoveEndC2S;
import dev.creoii.dungeoneer.network.c2s.character.CharacterMoveStartC2S;

public class CharacterController extends InputAdapter {
    private final Dungeoneer client;

    public CharacterController(Dungeoneer client) {
        this.client = client;
    }

    @Override
    public boolean keyDown(int keycode) {
        ClientCharacter character = client.getState().getActiveCharacter();

        if (character.isNull())
            return false;

        if (character.canMove()) {
            float dx = 0f;
            float dy = 0f;

            if (keycode == OptionsManager.LEFT_KEY.value())
                dx -= 1;
            if (keycode == OptionsManager.RIGHT_KEY.value())
                dx += 1;
            if (keycode == OptionsManager.UP_KEY.value())
                dy += 1;
            if (keycode == OptionsManager.DOWN_KEY.value())
                dy -= 1;

            if (dx != 0f || dy != 0f) {
                boolean axis = true;
                boolean positive;
                if (dy != 0f) {
                    axis = false;
                    positive = dy > 0f;
                } else positive = dx > 0f;

                client.get().sendUDP(new CharacterMoveStartC2S(client.getState().getCurrentRaid().get().id(), character.get().id(), axis, positive));
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        ClientCharacter character = client.getState().getActiveCharacter();

        if (character.isNull())
            return false;

        if (character.isMoving() && OptionsManager.isMovementKey(keycode)) {
            boolean axis;
            boolean positive;

            if (keycode == OptionsManager.LEFT_KEY.value()) {
                axis = true;
                positive = false;
            } else if (keycode == OptionsManager.RIGHT_KEY.value()) {
                axis = true;
                positive = true;
            } else if (keycode == OptionsManager.UP_KEY.value()) {
                axis = false;
                positive = true;
            } else if (keycode == OptionsManager.DOWN_KEY.value()) {
                axis = false;
                positive = false;
            } else return false;

            client.get().sendUDP(new CharacterMoveEndC2S(client.getState().getCurrentRaid().get().id(), character.get().id(), axis, positive));
            return true;
        }
        return false;
    }
}
