package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.InputAdapter;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.AnimationState;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.screen.game.GameScreen;
import dev.creoii.dungeoneer.definitions.attack.*;
import dev.creoii.dungeoneer.network.c2s.character.CharacterMoveC2S;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.stat.StatUtils;

public class CharacterInputListener extends InputAdapter implements MousePosListener {
    private final Dungeoneer client;
    private final GameScreen screen;
    private int movementFlags;
    private final float[] mousePos;
    private boolean attacking;

    public CharacterInputListener(Dungeoneer client, GameScreen screen) {
        this.client = client;
        this.screen = screen;
        mousePos = VectorUtils.zero();
    }

    @Override
    public float[] getMousePos() {
        return mousePos;
    }

    public boolean isAttacking() {
        return attacking;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        updateMousePos(screen.getCamera());
        return super.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        attacking = true;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        attacking = false;
        return true;
    }

    public void tryAttack() {
        ClientCharacter character = client.getState().getActiveCharacter();
        AnimationState animationState = character.getAnimationState();

        long currentTime = System.currentTimeMillis();
        long cooldown = (long) StatUtils.getCalculatedAttackSpeed(character.getStats().attackSpeed().value());

        if (client.getState().getStatus() == ClientState.Status.RAIDING && !client.getState().getCurrentRaid().isNull()) {
            if (!character.isAttackPending() && (currentTime - character.getLastAttackTime()) >= cooldown) {
                character.setAttackPending(true);

                Attack attack = DataManager.getAttack("simple");
                character.attack(attack, getDirectionToMouse(character.getCenterX(), character.getCenterY()));
            }
            character.setAnimationState(AnimationState.toAttacking(animationState));
        } else character.setAnimationState(character.isMoving() ? AnimationState.toMoving(animationState) : AnimationState.toIdle(animationState));
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

        character.setVelocity(dx, dy);
        float[] velocity = character.getVelocity();
        if (!VectorUtils.isZero(velocity)) {
            VectorUtils.nor(velocity);
        }

        AnimationState to;
        if (VectorUtils.isZero(velocity)) {
            to = AnimationState.toIdle(character.getAnimationState());
        } else if (Math.abs(velocity[1]) >= Math.abs(velocity[0])) {
            to = velocity[1] > 0 ? AnimationState.MOVING_UP : AnimationState.MOVING_DOWN;
        } else to = velocity[0] > 0 ? AnimationState.MOVING_RIGHT : AnimationState.MOVING_LEFT;
        character.setAnimationState(AnimationState.toMoving(to));

        client.get().sendUDP(new CharacterMoveC2S(client.getState().getCurrentRaid().get().id(), character.get().id(), movementFlags));
    }
}
