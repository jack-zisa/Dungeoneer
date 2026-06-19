package dev.creoii.dungeoneer.client.game;

import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.util.VectorUtils;

public class ClientBullet extends Bullet {
    private final float[] renderPos;
    private final float[] correction;

    public ClientBullet() {
        renderPos = VectorUtils.zero();
        correction = VectorUtils.zero();
    }

    public float[] getRenderPos() {
        return renderPos;
    }

    public float getRenderX() {
        return renderPos[0];
    }

    public float getRenderY() {
        return renderPos[1];
    }

    public void setRenderPos(float x, float y) {
        renderPos[0] = x;
        renderPos[1] = y;
    }

    public float[] getCorrection() {
        return correction;
    }

    public void setCorrection(float x, float y) {
        correction[0] = x;
        correction[1] = y;
    }

    @Override
    public float getCenterX() {
        return getRenderX() + getType().scale() * 4f;
    }

    @Override
    public float getCenterY() {
        return getRenderY() + getType().scale() * 4f;
    }

    @Override
    public void applyTransform(float originX, float originY, float dirX, float dirY) {
        float perpX = -dirY;
        float perpY = dirX;

        float worldOffsetX = dirX * getLocalY() + perpX * getLocalX();
        float worldOffsetY = dirY * getLocalY() + perpY * getLocalX();

        setRenderPos(originX + worldOffsetX, originY + worldOffsetY);

        setLocalDirection(dirX, dirY);
    }

    @Override
    public void reset() {
        super.reset();
        setRenderPos(0f, 0f);
        setCorrection(0f, 0f);
    }
}
