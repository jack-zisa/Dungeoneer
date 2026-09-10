package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.attack.*;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.stat.StatContainer;

import java.util.List;
import java.util.function.BiPredicate;

public interface Character extends Entity {
    int getConnectionId();

    CharacterDefinition get();

    float[] getVelocity();

    default void setVelocity(float x, float y) {
        getVelocity()[0] = x;
        getVelocity()[1] = y;
    }

    StatContainer getStats();

    StatContainer getMaxStats();

    void setDead(boolean dead);

    boolean isDead();

    default boolean canMove() {
        return getStats().speed().value() > 0f;
    }

    default boolean isMoving() {
        return getVelocity()[0] != 0f || getVelocity()[1] != 0f;
    }

    default void damage(int damage) {
        getStats().setHealth(getStats().health().value() - damage);

        if (getStats().health().value() <= 0) {
            setDead(true);
        }
    }

    default boolean attack(Attack attack, Raid raid, float[] mouseDir, BiPredicate<Integer, Integer> willHitWallRightAway) {
        switch (attack) {
            case ReferenceAttack(String id) -> {
                return attack(DataManager.getAttack(id), raid, mouseDir, willHitWallRightAway);
            }
            case BulletAttack(_, int bulletCount, float arcGap, float angleOffset, Vector2 offset, int indexOffset) -> {
                BulletType bullet = DataManager.getBullet(Constants.TEST_BULLET);
                if (bullet == null)
                    return false;

                float baseAngle = -arcGap * (bulletCount - 1) / 2f;

                Vector2 up = new Vector2(-mouseDir[1], mouseDir[0]);
                float x = getCenterX() + mouseDir[0] * offset.x + up.x * offset.y;
                float y = getCenterY() + mouseDir[1] * offset.x + up.y * offset.y;

                boolean success = false;
                for (int i = 0; i < bulletCount; ++i) {
                    float angle = (baseAngle + i * arcGap) + angleOffset;

                    float radians = angle * MathUtils.degreesToRadians;
                    float cos = MathUtils.cos(radians);
                    float sin = MathUtils.sin(radians);

                    float rotatedX = mouseDir[0] * cos - mouseDir[1] * sin;
                    float rotatedY = mouseDir[1] * cos + mouseDir[0] * sin;

                    if (willHitWallRightAway(x, y, rotatedX, rotatedY, willHitWallRightAway)) continue;

                    raid.addBullet(x, y, rotatedX, rotatedY, bullet, i + indexOffset, false);
                    success = true;
                }
                return success;
            }
            case CompositeAttack(_, List<Attack> attacks) -> {
                boolean success = false;
                for (Attack attack1 : attacks) {
                    success |= attack(attack1, raid, mouseDir, willHitWallRightAway);
                }
                return success;
            }
            case null, default -> throw new IllegalStateException("Unexpected attack value: " + attack);
        }
    }

    private boolean willHitWallRightAway(float startX, float startY, float dirX, float dirY, BiPredicate<Integer, Integer> predicate) {
        float x = startX + dirX;
        float y = startY + dirY;
        return predicate.test((int) (x / 8f), (int) (y / 8f));
    }

    default void updateVelocity(float[] velocity, int movementFlags, float rotation) {
        float dx = 0;
        float dy = 0;

        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_LEFT) != 0) dx--;
        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_RIGHT) != 0) dx++;
        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_UP) != 0) dy++;
        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_DOWN) != 0) dy--;

        velocity[0] = dx;
        velocity[1] = dy;

        if (!VectorUtils.isZero(velocity)) {
            VectorUtils.nor(velocity);
            VectorUtils.rotateDeg(velocity, -rotation);
        }
    }

    default float[] getTargetPosition(float[] position, float[] velocity, float speed, float dt) {
        float[] ret = new float[2];
        ret[0] = position[0] + velocity[0] * speed * dt;
        ret[1] = position[1] + velocity[1] * speed * dt;
        return ret;
    }
}
