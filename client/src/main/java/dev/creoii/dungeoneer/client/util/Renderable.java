package dev.creoii.dungeoneer.client.util;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dev.creoii.dungeoneer.client.Dungeoneer;

public interface Renderable {
    RenderLayer renderLayer();

    default void render(Dungeoneer client, PolygonSpriteBatch batch, Camera camera, float rotation, float dt) {
    }

    default void renderDebug(ShapeRenderer shapeRenderer, float[] mouseDir) {
    }

    float depth(float rotation, OrthographicCamera camera);
}
