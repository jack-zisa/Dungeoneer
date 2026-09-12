package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.attack.*;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.RemovalReason;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.event.AttackEvents;
import dev.creoii.dungeoneer.util.event.DamageEvents;
import dev.creoii.dungeoneer.util.event.HealEvents;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface Character<R extends Raid<?, ?, ?, ?>> extends LivingEntity {
    int getConnectionId();

    CharacterDefinition get();

    float[] getVelocity();

    default void setVelocity(float x, float y) {
        getVelocity()[0] = x;
        getVelocity()[1] = y;
    }

    StatContainer getStats();

    StatContainer getMaxStats();

    default boolean canMove() {
        return getStats().speed().value() > 0f;
    }

    default boolean isMoving() {
        return getVelocity()[0] != 0f || getVelocity()[1] != 0f;
    }

    default boolean inRaid() {
        return getRaid() != null && !getRaid().isNull();
    }

    @Nullable
    R getRaid();

    void setRaid(@Nullable R raid);

    void tick(float dt);

    default void damage(int damage) {
        if (!inRaid())
            return;

        damage = DamageEvents.MODIFY.invoker().modifyDamage(this, damage);

        getStats().setHealth(getStats().health().value() - damage);

        if (getStats().health().value() <= 0) {
            setDead(true);
        }
    }

    default void heal(int amount) {
        if (!inRaid())
            return;
		amount = HealEvents.MODIFY.invoker().modifyHeal(this, amount);

        getStats().setHealth(getStats().health().value() + amount);
    }

    default void die() {
        if (getRaid() != null && !getRaid().isNull()) {
            getRaid().removeCharacter(get().accountId(), RemovalReason.DEATH);
        }
    }

    default boolean attack(Attack attack, Raid<?, ?, ?, ?> raid, float[] mouseDir) {
        if (!AttackEvents.PRE.invoker().onPreAttack(this, attack, raid))
            return false;

        switch (attack) {
            case ReferenceAttack(String id) -> {
                return attack(DataManager.getAttack(id), raid, mouseDir);
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

                    if (willHitWallRightAway(raid.getDungeonMap(), x, y, rotatedX, rotatedY)) continue;

                    raid.addBullet(x, y, rotatedX, rotatedY, bullet, i + indexOffset, false);
                    success = true;
                }
                return success;
            }
            case CompositeAttack(_, List<Attack> attacks) -> {
                boolean success = false;
                for (Attack attack1 : attacks) {
                    success |= attack(attack1, raid, mouseDir);
                }
                return success;
            }
            case null, default -> throw new IllegalStateException("Unexpected attack value: " + attack);
        }
    }

    private boolean willHitWallRightAway(DungeonMap map, float startX, float startY, float dirX, float dirY) {
        float x = startX + dirX;
        float y = startY + dirY;
        return map.isSolid((int) (x / 8f), (int) (y / 8f), false);
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
