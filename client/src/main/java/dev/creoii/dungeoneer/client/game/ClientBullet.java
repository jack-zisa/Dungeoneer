package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.attack.bullet.SingleBulletType;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.AngledBulletPath;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

public class ClientBullet extends BulletNode implements Pool.Poolable {
    public void render(Dungeoneer client, SpriteBatch batch, float dt) {
        Texture texture = client.getAssets().getTexture(Assets.Atlas.BULLET, getType().id());

        float scale = getType() instanceof SingleBulletType singleBulletType ? singleBulletType.scale() : 1f;
        float rotationSpeed = getType() instanceof SingleBulletType singleBulletType ? singleBulletType.rotationSpeed() : 0f;
        float angleOffset = getType() instanceof SingleBulletType singleBulletType ? singleBulletType.angleOffset() : 0f;
        float angle = 0f;

        if (getPath() instanceof AngledBulletPath angledBulletPath) {
            angledBulletPath.incrementAngle(rotationSpeed * dt);
            angle = angledBulletPath.getAngle();
        }

        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;

        batch.draw(texture,
            getX() - width * .5f, getY() - height * .5f,
            width * .5f, height * .5f,
            width, height,
            1f, 1f,
            angleDeg() + angleOffset + angle,
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false
        );
    }

    public float angleDeg() {
        float angle = (float) Math.atan2(getDirY(), getDirX()) * MathUtils.radiansToDegrees;
        if (angle < 0f)
            angle += 360f;
        return angle;
    }
}
