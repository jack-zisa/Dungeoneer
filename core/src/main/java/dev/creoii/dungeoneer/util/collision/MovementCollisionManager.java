package dev.creoii.dungeoneer.util.collision;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.definitions.sided.DungeonMap;
import dev.creoii.dungeoneer.definitions.sided.Entity;

public final class MovementCollisionManager {
    private static final float HALF_TILE = 4f;
    private static final float BORDER_OFFSET = .01f;

    public static Vector2 modifyMove(DungeonMap map, Entity entity, float x, float y) {
        return modifyMove(map, entity, x, y, false);
    }

    public static Vector2 modifyMove(DungeonMap map, Entity entity, float x, float y, boolean bounded) {
        Vector2 current = new Vector2(entity.getX(), entity.getY());

        float dx = x - current.x;
        float dy = y - current.y;

        if (Math.abs(dx) < HALF_TILE && Math.abs(dy) < HALF_TILE) {
            return modifyStep(map, entity, current, x, y, bounded);
        }

        Vector2 result = new Vector2(current);

        float maxDistance = Math.max(Math.abs(dx), Math.abs(dy));
        float stepSize = HALF_TILE / maxDistance;

        float progress = 0f;
        while (progress < 1f) {
            float step = Math.min(stepSize, 1f - progress);

            float targetX = result.x + dx * step;
            float targetY = result.y + dy * step;

            result = modifyStep(map, entity, result, targetX, targetY, bounded);

            progress += step;
        }
        return result;
    }

    private static Vector2 modifyStep(DungeonMap map, Entity entity, Vector2 position, float x, float y, boolean bounded) {
        boolean xCross = (position.x % HALF_TILE == 0f && x != position.x) || (int) (position.x / HALF_TILE) != (int) (x / HALF_TILE);
        boolean yCross = (position.y % HALF_TILE == 0f && y != position.y) || (int) (position.y / HALF_TILE) != (int) (y / HALF_TILE);
        boolean targetSolid = map.isSolid(MathUtils.floor(x * .125f), MathUtils.floor(y * .125f), bounded);

        if (!targetSolid) {
            if (xCross && yCross) {
                boolean xOnlySolid = map.isSolid(MathUtils.floor(x * .125f), MathUtils.floor(position.y * .125f), bounded);
                boolean yOnlySolid = map.isSolid(MathUtils.floor(position.x * .125f), MathUtils.floor(y * .125f), bounded);
                if (!xOnlySolid || !yOnlySolid) {
                    return new Vector2(x, y);
                }
            } else return new Vector2(x, y);
        } else entity.onTileCollision();

        float nextXBorder = position.x;
        float nextYBorder = position.y;

        if (xCross) {
            nextXBorder = getBorder(position.x, x);

            if (x > position.x) {
                nextXBorder -= BORDER_OFFSET;
            } else nextXBorder += BORDER_OFFSET;
        }

        if (yCross) {
            nextYBorder = getBorder(position.y, y);

            if (y > position.y) {
                nextYBorder -= BORDER_OFFSET;
            } else nextYBorder += BORDER_OFFSET;
        }

        if (!xCross) return new Vector2(x, nextYBorder);
        if (!yCross) return new Vector2(nextXBorder, y);

        float xBorderDist = x > position.x ? x - nextXBorder : nextXBorder - x;
        float yBorderDist = y > position.y ? y - nextYBorder : nextYBorder - y;
        if (xBorderDist > yBorderDist) {
            if (!map.isSolid(MathUtils.floor(x * .125f), MathUtils.floor(nextYBorder * .125f), bounded)) {
                return new Vector2(x, nextYBorder);
            }

            if (!map.isSolid(MathUtils.floor(nextXBorder * .125f), MathUtils.floor(y * .125f), bounded)) {
                return new Vector2(nextXBorder, y);
            }
        } else {
            if (!map.isSolid(MathUtils.floor(nextXBorder * .125f), MathUtils.floor(y * .125f), bounded)) {
                return new Vector2(nextXBorder, y);
            }

            if (!map.isSolid(MathUtils.floor(x * .125f), MathUtils.floor(nextYBorder * .125f), bounded)) {
                return new Vector2(x, nextYBorder);
            }
        }

        return new Vector2(nextXBorder, nextYBorder);
    }

    private static float getBorder(float current, float target) {
        float currentCell = current / HALF_TILE;

        if (target > current) {
            return (MathUtils.floor(currentCell) + 1) * HALF_TILE;
        }

        return MathUtils.floor(currentCell) * HALF_TILE;
    }
}
