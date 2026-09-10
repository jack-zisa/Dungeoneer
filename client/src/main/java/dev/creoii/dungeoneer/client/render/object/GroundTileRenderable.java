package dev.creoii.dungeoneer.client.render.object;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientDungeonMap;
import dev.creoii.dungeoneer.client.render.RenderLayer;
import dev.creoii.dungeoneer.client.render.RenderUtils;
import dev.creoii.dungeoneer.client.render.Renderable;
import dev.creoii.dungeoneer.client.util.ProjectionUtils;
import dev.creoii.dungeoneer.util.VectorUtils;

public record GroundTileRenderable(TextureRegion texture, int tileX, int tileY) implements Renderable {
    @Override
    public RenderLayer renderLayer() {
        return RenderLayer.GROUND;
    }

    @Override
    public void render(Dungeoneer client, PolygonSpriteBatch batch, Camera camera, float rotation, float dt) {
        RenderUtils.drawGroundTile(camera, batch, texture, tileX, tileY, rotation);
    }

    @Override
    public float depth(float rotation, OrthographicCamera camera) {
        return ProjectionUtils.project(tileX * ClientDungeonMap.TILE_SIZE, tileY * ClientDungeonMap.TILE_SIZE, 0f, rotation, camera.position.x, camera.position.y).y;
    }
}
