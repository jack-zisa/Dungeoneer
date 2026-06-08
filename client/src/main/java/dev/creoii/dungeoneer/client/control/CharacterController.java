package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.InputAdapter;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.network.c2s.character.CharacterMoveC2S;
import dev.creoii.dungeoneer.util.Constants;

public class CharacterController extends InputAdapter {
    private final Dungeoneer client;
    private int movementFlags;

    public CharacterController(Dungeoneer client) {
        this.client = client;
    }

    @Override
    public boolean keyDown(int keycode) {
        ClientCharacter character = client.getState().getActiveCharacter();

        if (character.isNull())
            return false;

        if (character.canMove()) {
            if (keycode == client.getSettings().leftKey().value()) {
                movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_RIGHT;
                movementFlags |= Constants.CHARACTER_MOVEMENT_FLAG_LEFT;
            }

            if (keycode == client.getSettings().rightKey().value()) {
                movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_LEFT;
                movementFlags |= Constants.CHARACTER_MOVEMENT_FLAG_RIGHT;
            }

            if (keycode == client.getSettings().upKey().value()) {
                movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_DOWN;
                movementFlags |= Constants.CHARACTER_MOVEMENT_FLAG_UP;
            }

            if (keycode == client.getSettings().downKey().value()) {
                movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_UP;
                movementFlags |= Constants.CHARACTER_MOVEMENT_FLAG_DOWN;
            }
            updateMovement();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        ClientCharacter character = client.getState().getActiveCharacter();

        if (character.isNull())
            return false;

        if (character.canMove()) {
            if (keycode == client.getSettings().leftKey().value()) movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_LEFT;
            if (keycode == client.getSettings().rightKey().value()) movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_RIGHT;
            if (keycode == client.getSettings().upKey().value()) movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_UP;
            if (keycode == client.getSettings().downKey().value()) movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_DOWN;
            updateMovement();
            return true;
        }

        return false;
    }

    private void updateMovement() {
        float dx = 0;
        float dy = 0;

        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_LEFT) != 0) --dx;
        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_RIGHT) != 0) ++dx;
        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_UP) != 0) ++dy;
        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_DOWN) != 0) --dy;

        ClientCharacter character = client.getState().getActiveCharacter();
        if (character.isNull())
            return;

        character.getVelocity().set(dx, dy);
        if (!character.getVelocity().isZero()) {
            character.getVelocity().nor();
        }

        client.get().sendUDP(new CharacterMoveC2S(client.getState().getCurrentRaid().get().id(), character.get().id(), movementFlags));
    }
}
