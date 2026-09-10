package dev.creoii.dungeoneer.client.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public final class ProjectionUtils {
    public static void project(float[] out, float x, float y, float z, float angle, float originX, float originY) {
        float rad = angle * MathUtils.degreesToRadians;

        float sin = MathUtils.sin(rad);
        float cos = MathUtils.cos(rad);

        float dx = x - originX;
        float dy = y - originY;

        float rx = dx * cos - dy * sin;
        float ry = dx * sin + dy * cos;

        float screenX = rx;
        float screenY = ry + z;

        out[0] = screenX + originX;
        out[1] = screenY + originY;
    }

    public static Vector2 project(float x, float y, float z, float angle, float originX, float originY) {
        float[] result = {0f, 0f};
        project(result, x, y, z, angle, originX, originY);
        return new Vector2(result[0], result[1]);
    }

    public static void projectMouse(float[] vec, final Matrix4 matrix) {
        final float[] m = matrix.val;

        float x = vec[0];
        float y = vec[1];

        final float w = 1f / (x * m[Matrix4.M30] + y * m[Matrix4.M31] + m[Matrix4.M33]);

        vec[0] = (x * m[Matrix4.M00] + y * m[Matrix4.M01] + m[Matrix4.M03]) * w;
        vec[1] = (x * m[Matrix4.M10] + y * m[Matrix4.M11] + m[Matrix4.M13]) * w;
    }

    public static void prj2(float[] vec, float z, float angle, float originX, float originY) {
        project(vec, vec[0], vec[1], z, angle, originX, originY);
    }
}
