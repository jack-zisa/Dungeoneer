package dev.creoii.dungeoneer.client.util;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface Renderable {
    RenderLayer renderLayer();

    default void render(PolygonSpriteBatch batch, Camera camera, float rotation) {
    }

    default void renderDebug(ShapeRenderer shapeRenderer, float[] mouseDir) {
    }

    float depth(float rotation, OrthographicCamera camera);
}
