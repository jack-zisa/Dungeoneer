package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.Bullet;

public class ClientBullet implements Pool.Poolable {
    private Bullet bullet;
    private final Vector2 pos;
    private float startX;
    private float startY;
    private final Vector2 direction;
    private float speed;
    private float distanceTravelled;
    private float lifetime;
    private int index;
    private float age;
    private float orbitPhase;
    private float angleOffset;
    private float angle;

    public ClientBullet() {
        pos = new Vector2();
        direction = new Vector2();
        reset();
    }

    public Bullet get() {
        return bullet;
    }

    public void set(Bullet bullet) {
        this.bullet = bullet;
    }

    public Vector2 getPos() {
        return pos;
    }

    public void setPos(float x, float y) {
        pos.set(x, y);
    }

    public void setStartPos(float x, float y) {
        startX = x;
        startY = y;
    }

    public float getAngleOffset() {
        return angleOffset;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngleOffset(float angleOffset) {
        this.angleOffset = angleOffset;
    }

    public void incrementAngle(float f) {
        angle += f;
    }

    public void setDirection(float x, float y) {
        direction.set(x, y);
    }

    public Vector2 getDirection() {
        return direction;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public void setOrbitPhase(float orbitPhase) {
        this.orbitPhase = orbitPhase;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void reset() {
        setPos(0f, 0f);
        setDirection(0f, 0f);
        bullet = null;
        lifetime = 0f;
        age = 0f;
        index = 0;
        startX = 0f;
        startY = 0f;
        distanceTravelled = 0f;
        speed = 0f;
        orbitPhase = 0f;
        angle = 0f;
    }

    public boolean update(float dt) {
        if ((lifetime -= dt) <= 0f) {
            return false;
        }

        age += dt;
        speed += bullet.acceleration() * dt;
        distanceTravelled += speed * dt;

        float phase = (index & 1) == 0 ? 0f : MathUtils.PI;
        float wave = MathUtils.sin(age * bullet.frequency() + phase) * bullet.amplitude();

        float perpX = -direction.y;
        float perpY = direction.x;

        float orbitAngle = age * MathUtils.PI2 * bullet.orbitSpeed() + orbitPhase;
        float orbitForward = MathUtils.cos(orbitAngle) * bullet.orbitRadius();
        float orbitSide = MathUtils.sin(orbitAngle) * bullet.orbitRadius();

        float orbitX = direction.x * orbitForward + perpX * orbitSide;
        float orbitY = direction.y * orbitForward + perpY * orbitSide;

        getPos().set(
            startX + direction.x * distanceTravelled + perpX * wave + orbitX,
            startY + direction.y * distanceTravelled + perpY * wave + orbitY
        );
        return true;
    }
}
