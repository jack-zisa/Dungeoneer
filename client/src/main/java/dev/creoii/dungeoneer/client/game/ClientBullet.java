package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class ClientBullet implements Pool.Poolable {
    private final Vector2 pos;
    private float startX;
    private float startY;
    private final Vector2 direction;
    private float speed;
    private float acceleration;
    private float distanceTravelled;
    private float lifetime;
    private int index;
    private float age;
    private float amplitude;
    private float frequency;
    private float orbitSpeed;
    private float orbitRadius;
    private float orbitPhase;
    private float angleOffset;
    private float angle;
    private float rotation;

    public ClientBullet() {
        pos = new Vector2();
        direction = new Vector2();
        reset();
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

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
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

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public void setOrbitSpeed(float orbitSpeed) {
        this.orbitSpeed = orbitSpeed;
    }

    public void setOrbitRadius(float orbitRadius) {
        this.orbitRadius = orbitRadius;
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
        lifetime = 0f;
        amplitude = 0f;
        frequency = 0f;
        age = 0f;
        index = 0;
        startX = 0f;
        startY = 0f;
        distanceTravelled = 0f;
        acceleration = 0f;
        speed = 0f;
        orbitSpeed = 0f;
        orbitRadius = 0f;
        orbitPhase = 0f;
        angle = 0f;
        rotation = 0f;
    }

    public boolean update(float dt) {
        if ((lifetime -= dt) <= 0f) {
            return false;
        }

        age += dt;
        speed += acceleration * dt;
        distanceTravelled += speed * dt;

        float phase = (index & 1) == 0 ? 0f : MathUtils.PI;
        float wave = MathUtils.sin(age * frequency + phase) * amplitude;

        float perpX = -direction.y;
        float perpY = direction.x;

        float orbitAngle = age * orbitSpeed + orbitPhase;
        float orbitForward = MathUtils.cos(orbitAngle) * orbitRadius;
        float orbitSide    = MathUtils.sin(orbitAngle) * orbitRadius;

        float orbitX = direction.x * orbitForward + perpX * orbitSide;
        float orbitY = direction.y * orbitForward + perpY * orbitSide;

        getPos().set(
            startX + direction.x * distanceTravelled + perpX * wave + orbitX,
            startY + direction.y * distanceTravelled + perpY * wave + orbitY
        );
        return true;
    }
}
