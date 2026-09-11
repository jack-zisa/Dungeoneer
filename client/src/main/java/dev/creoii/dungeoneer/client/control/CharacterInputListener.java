package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.AnimationState;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.definitions.attack.*;
import dev.creoii.dungeoneer.network.c2s.character.CharacterMoveC2S;
import dev.creoii.dungeoneer.network.c2s.raid.AttackC2S;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.event.AttackEvents;
import dev.creoii.dungeoneer.util.event.MoveEvents;
import dev.creoii.dungeoneer.util.stat.StatUtils;

public class CharacterInputListener extends InputAdapter implements MousePosListener {
    private final Dungeoneer client;
    private int movementFlags;
    private final float[] mousePos;
    private boolean attacking;
    private float rotation;

    public CharacterInputListener(Dungeoneer client) {
        this.client = client;
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
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (client.getCommandManager().isActive())
            return false;

        if (button == Input.Buttons.LEFT) {
            attacking = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (client.getCommandManager().isActive())
            return false;

        if (button == Input.Buttons.LEFT) {
            attacking = false;
            return true;
        }
        return false;
    }

    public void tryAttack() {
        if (client.getCommandManager().isActive())
            return;

        ClientCharacter character = client.getState().getActiveCharacter();
        AnimationState animationState = character.getAnimationState();
        ClientRaid raid = client.getState().getCurrentRaid();

        long currentTime = System.currentTimeMillis();
        long cooldown = (long) StatUtils.getCalculatedAttackSpeed(character.getStats().dexterity().value());

        if (client.getState().getStatus() == ClientState.Status.RAIDING && !raid.isNull()) {
            Attack attack = DataManager.getAttack(Constants.TEST_ATTACK);
            if (!AttackEvents.PRE.invoker().onPreAttack(character, attack, raid))
                return;

            if (!character.isAttackPending() && (currentTime - character.getLastAttackTime()) >= cooldown) {
                character.setAttackPending(true);

                float[] mouseDir = getDirectionToMouse(character.getCenterX(), character.getCenterY());
                if (character.attack(attack, raid, mouseDir)) {
                    AttackEvents.POST.invoker().onPostAttack(character, attack, raid);
                    client.get().sendTCP(new AttackC2S(raid.get().id(), character.get().accountId(), mouseDir[0], mouseDir[1]));
                } else character.setAttackPending(false);
            }
            character.setAnimationState(AnimationState.toAttacking(animationState));
        } else character.setAnimationState(character.isMoving() ? AnimationState.toMoving(animationState) : AnimationState.toIdle(animationState));
    }

    @Override
    public boolean keyDown(int keycode) {
        ClientCharacter character = client.getState().getActiveCharacter();
        if (character.isNull() || client.getCommandManager().isActive())
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

            if (movementFlags != 0) {
                if (!MoveEvents.PRE.invoker().onPreMove(character, client.getState().getCurrentRaid())) {
                    movementFlags = 0;
                    return false;
                }
            }

            updateMovement();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        ClientCharacter character = client.getState().getActiveCharacter();
        if (character.isNull() || client.getCommandManager().isActive())
            return false;

        if (character.canMove()) {
            if (keycode == client.getSettings().leftKey().value()) movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_LEFT;
            if (keycode == client.getSettings().rightKey().value()) movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_RIGHT;
            if (keycode == client.getSettings().upKey().value()) movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_UP;
            if (keycode == client.getSettings().downKey().value()) movementFlags &= ~Constants.CHARACTER_MOVEMENT_FLAG_DOWN;

            if (movementFlags != 0) {
                MoveEvents.POST.invoker().onPostMove(character, client.getState().getCurrentRaid());
            }

            updateMovement();
            return true;
        }

        return false;
    }

    private void updateMovement() {
        ClientCharacter character = client.getState().getActiveCharacter();
        if (character.isNull())
            return;
        client.get().sendUDP(new CharacterMoveC2S(client.getState().getCurrentRaid().get().id(), character.get().id(), movementFlags, rotation));
    }

    public void updateRotation(float dt) {
        if (client.getCommandManager().isActive())
            return;

        float oldRotation = rotation;

        if (Gdx.input.isKeyPressed(client.getSettings().resetRotationKey().value())) {
            rotation = 0f;
            return;
        }

        int rotationSpeed = client.getSettings().cameraRotationSpeed().value() * 100;
        if (Gdx.input.isKeyPressed(client.getSettings().rotateLeftKey().value())) {
            rotation -= rotationSpeed * dt;
        } else if (Gdx.input.isKeyPressed(client.getSettings().rotateRightKey().value())) {
            rotation += rotationSpeed * dt;
        }

        rotation %= 360f;

        if (rotation < 0f)
            rotation += 360f;

        if (rotation != oldRotation)
            updateMovement();
    }

    public float getRotation() {
        return rotation;
    }
}
