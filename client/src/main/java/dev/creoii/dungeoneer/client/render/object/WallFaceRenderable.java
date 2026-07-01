package dev.creoii.dungeoneer.client.render.object;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientDungeonMap;
import dev.creoii.dungeoneer.client.render.RenderLayer;
import dev.creoii.dungeoneer.client.render.RenderUtils;
import dev.creoii.dungeoneer.client.render.Renderable;
import dev.creoii.dungeoneer.util.Direction;

public record WallFaceRenderable(TextureRegion texture, int x, int y, Direction direction) implements Renderable {
    @Override
    public RenderLayer renderLayer() {
        return RenderLayer.OBJECT;
    }

    @Override
    public void render(Dungeoneer client, PolygonSpriteBatch batch, Camera camera, float rotation, float dt) {
        RenderUtils.drawWallSide(camera, batch, this, rotation);
    }

    @Override
    public float depth(float rotation, OrthographicCamera camera) {
        float worldX = x * ClientDungeonMap.TILE_SIZE;
        float worldY = y * ClientDungeonMap.TILE_SIZE;
        Vector2 p = ClientDungeonMap.project(worldX, worldY, -ClientDungeonMap.WALL_HEIGHT, rotation, camera.position.x, camera.position.y);
        return p.y;
    }

    public static boolean isVisible(Direction direction, float rotation) {
        Vector2 normal = new Vector2(direction.getVector()[0], direction.getVector()[1]);
        normal.rotateDeg(rotation);
        return normal.y <= 0f;
    }
}
