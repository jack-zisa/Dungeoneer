package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.InputAdapter;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.network.c2s.character.CharacterMoveC2S;

public class CharacterController extends InputAdapter {
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 4;
    public static final int DOWN = 8;
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
                movementFlags &= ~RIGHT;
                movementFlags |= LEFT;
            }

            if (keycode == client.getSettings().rightKey().value()) {
                movementFlags &= ~LEFT;
                movementFlags |= RIGHT;
            }

            if (keycode == client.getSettings().upKey().value()) {
                movementFlags &= ~DOWN;
                movementFlags |= UP;
            }

            if (keycode == client.getSettings().downKey().value()) {
                movementFlags &= ~UP;
                movementFlags |= DOWN;
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
            if (keycode == client.getSettings().leftKey().value()) movementFlags &= ~LEFT;
            if (keycode == client.getSettings().rightKey().value()) movementFlags &= ~RIGHT;
            if (keycode == client.getSettings().upKey().value()) movementFlags &= ~UP;
            if (keycode == client.getSettings().downKey().value()) movementFlags &= ~DOWN;
            updateMovement();
            return true;
        }

        return false;
    }

    private void updateMovement() {
        float dx = 0;
        float dy = 0;

        if ((movementFlags & LEFT) != 0) --dx;
        if ((movementFlags & RIGHT) != 0) ++dx;
        if ((movementFlags & UP) != 0) ++dy;
        if ((movementFlags & DOWN) != 0) --dy;

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
