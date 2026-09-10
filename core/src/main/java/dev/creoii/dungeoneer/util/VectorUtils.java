package dev.creoii.dungeoneer.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public final class VectorUtils {
    public static final float[] ZERO = zero();
    public static final float[] UP = Direction.UP.getVector();
    public static final float[] DOWN = Direction.DOWN.getVector();
    public static final float[] LEFT = Direction.LEFT.getVector();
    public static final float[] RIGHT = Direction.RIGHT.getVector();

    public static float[] zero() {
        return new float[]{0f, 0f};
    }

    public static void nor(float[] vec) {
        float len = len(vec);
        if (len != 0f) {
            vec[0] /= len;
            vec[1] /= len;
        }
    }

    public static float len(float[] vec) {
        return (float) Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1]);
    }

    public static boolean isZero(float[] vec) {
        return vec[0] == 0f && vec[1] == 0f;
    }

    public static void setZero(float[] vec) {
        vec[0] = 0f;
        vec[1] = 0f;
    }

    public static void mulAdd(float[] vec, float[] mul, float scalar) {
        vec[0] += mul[0] * scalar;
        vec[1] += mul[1] * scalar;
    }

    public static void scl(float[] vec, float scalar) {
        vec[0] *= scalar;
        vec[1] *= scalar;
    }

    public static void rotateDeg(float[] vec, float degrees) {
        rotateRad(vec, degrees * MathUtils.degreesToRadians);
    }

    public static void rotateRad(float[] vec, float radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float newX = vec[0] * cos - vec[1] * sin;
        float newY = vec[0] * sin + vec[1] * cos;
        vec[0] = newX;
        vec[1] = newY;
    }
}
