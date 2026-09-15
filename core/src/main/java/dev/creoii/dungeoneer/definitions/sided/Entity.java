package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.math.MathUtils;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import dev.creoii.dungeoneer.util.collision.Collidable;
import org.jspecify.annotations.Nullable;

public interface Entity<R extends Raid<?, ?, ?, ?>> extends Collidable {
    long id();

    void setId(long id);

    float[] getPos();

    default float getX() {
        return getPos()[0];
    }

    default float getY() {
        return getPos()[1];
    }

    default void setPos(float x, float y) {
        getPos()[0] = x;
        getPos()[1] = y;
    }

    float getCenterX();

    float getCenterY();

    default int getTileX() {
        return MathUtils.floor(getX() * .125f);
    }

    default int getTileY() {
        return MathUtils.floor(getY() * .125f);
    }

    @Nullable
    default Tile getTileOn(MapLayerType layerType) {
        if (getRaid() == null || getRaid().getDungeonMap() == null) return null;
        return getRaid().getDungeonMap().getTileAt(layerType, getTileX(), getTileY());
    }

    @Nullable
    R getRaid();

    void setRaid(@Nullable R raid);

    void setDead(boolean dead);

    boolean isDead();

    default void onTileCollision() {
    }
}
