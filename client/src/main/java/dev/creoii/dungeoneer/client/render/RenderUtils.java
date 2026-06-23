package dev.creoii.dungeoneer.client.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientDungeonMap;
import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import dev.creoii.dungeoneer.definitions.attack.bullet.SingleBulletType;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.util.VectorUtils;

public final class RenderUtils {
    private static final float[] WALL_VERTS = new float[20];
    private static final short[] QUAD_INDICES = {
        0, 1, 2,
        2, 3, 0
    };
    private static final float[] QUAD_VERTICES = new float[20];

    public static void drawWallSide(Camera camera, PolygonSpriteBatch batch, ClientDungeonMap.WallFace face, float rotation) {
        float x = face.x() * ClientDungeonMap.TILE_SIZE;
        float y = face.y() * ClientDungeonMap.TILE_SIZE;

        Vector2 bl;
        Vector2 br;
        Vector2 tr;
        Vector2 tl;

        float centerX = camera.position.x;
        float centerY = camera.position.y;

        switch (face.direction()) {
            case DOWN -> {
                bl = ClientDungeonMap.project(x, y, 0f, rotation, centerX, centerY);
                br = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y, 0f, rotation, centerX, centerY);
                tl = ClientDungeonMap.project(x, y, ClientDungeonMap.WALL_HEIGHT, rotation, centerX, centerY);
                tr = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y, ClientDungeonMap.WALL_HEIGHT, rotation, centerX, centerY);
            }
            case UP -> {
                bl = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y + ClientDungeonMap.TILE_SIZE, 0f, rotation, centerX, centerY);
                br = ClientDungeonMap.project(x, y + ClientDungeonMap.TILE_SIZE, 0f, rotation, centerX, centerY);
                tl = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y + ClientDungeonMap.TILE_SIZE, ClientDungeonMap.WALL_HEIGHT, rotation, centerX, centerY);
                tr = ClientDungeonMap.project(x, y + ClientDungeonMap.TILE_SIZE, ClientDungeonMap.WALL_HEIGHT, rotation, centerX, centerY);
            }
            case LEFT -> {
                bl = ClientDungeonMap.project(x, y + ClientDungeonMap.TILE_SIZE, 0f, rotation, centerX, centerY);
                br = ClientDungeonMap.project(x, y, 0f, rotation, centerX, centerY);
                tl = ClientDungeonMap.project(x, y + ClientDungeonMap.TILE_SIZE, ClientDungeonMap.WALL_HEIGHT, rotation, centerX, centerY);
                tr = ClientDungeonMap.project(x, y, ClientDungeonMap.WALL_HEIGHT, rotation, centerX, centerY);
            }
            case RIGHT -> {
                bl = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y, 0f, rotation, centerX, centerY);
                br = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y + ClientDungeonMap.TILE_SIZE, 0f, rotation, centerX, centerY);
                tl = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y, ClientDungeonMap.WALL_HEIGHT, rotation, centerX, centerY);
                tr = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y + ClientDungeonMap.TILE_SIZE, ClientDungeonMap.WALL_HEIGHT, rotation, centerX, centerY);
            }
            default -> throw new IllegalStateException();
        }

        TextureRegion tex = face.texture();

        float u = tex.getU();
        float v = tex.getV2();
        float u2 = tex.getU2();
        float v2 = tex.getV();

        float color = Color.LIGHT_GRAY.toFloatBits();

        int i = 0;

        // BL
        WALL_VERTS[i++] = bl.x;
        WALL_VERTS[i++] = bl.y;
        WALL_VERTS[i++] = color;
        WALL_VERTS[i++] = u;
        WALL_VERTS[i++] = v;

        // BR
        WALL_VERTS[i++] = br.x;
        WALL_VERTS[i++] = br.y;
        WALL_VERTS[i++] = color;
        WALL_VERTS[i++] = u2;
        WALL_VERTS[i++] = v;

        // TR
        WALL_VERTS[i++] = tr.x;
        WALL_VERTS[i++] = tr.y;
        WALL_VERTS[i++] = color;
        WALL_VERTS[i++] = u2;
        WALL_VERTS[i++] = v2;

        // TL
        WALL_VERTS[i++] = tl.x;
        WALL_VERTS[i++] = tl.y;
        WALL_VERTS[i++] = color;
        WALL_VERTS[i++] = u;
        WALL_VERTS[i++] = v2;

        short[] indices = {0, 1, 2, 2, 3, 0};

        batch.draw(tex.getTexture(), WALL_VERTS, 0, 20, indices, 0, 6);
    }

    public static void drawWallTop(Camera camera, PolygonSpriteBatch batch, TextureRegion texture, int tileX, int tileY, float angle) {
        float x = tileX * ClientDungeonMap.TILE_SIZE;
        float y = tileY * ClientDungeonMap.TILE_SIZE;

        float centerX = camera.position.x;
        float centerY = camera.position.y;

        Vector2 bl = ClientDungeonMap.project(x, y, ClientDungeonMap.WALL_HEIGHT, angle, centerX, centerY);
        Vector2 br = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y, ClientDungeonMap.WALL_HEIGHT, angle, centerX, centerY);
        Vector2 tr = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y + ClientDungeonMap.TILE_SIZE, ClientDungeonMap.WALL_HEIGHT, angle, centerX, centerY);
        Vector2 tl = ClientDungeonMap.project(x, y + ClientDungeonMap.TILE_SIZE, ClientDungeonMap.WALL_HEIGHT, angle, centerX, centerY);

        drawQuad(batch, texture, bl, br, tr, tl);
    }

    public static void drawGroundTile(Camera camera, PolygonSpriteBatch batch, TextureRegion texture, int tileX, int tileY, float angle) {
        float x = tileX * ClientDungeonMap.TILE_SIZE;
        float y = tileY * ClientDungeonMap.TILE_SIZE;

        float centerX = camera.position.x;
        float centerY = camera.position.y;

        Vector2 bl = ClientDungeonMap.project(x, y, 0f, angle, centerX, centerY);
        Vector2 br = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y, 0f, angle, centerX, centerY);
        Vector2 tr = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y + ClientDungeonMap.TILE_SIZE, 0f, angle, centerX, centerY);
        Vector2 tl = ClientDungeonMap.project(x,y + ClientDungeonMap.TILE_SIZE, 0f, angle, centerX, centerY);

        drawQuad(batch, texture, bl, br, tr, tl);
    }

    public static void drawObjectTile(Camera camera, PolygonSpriteBatch batch, TextureRegion texture, int tileX, int tileY, float rotation) {
        float x = tileX * ClientDungeonMap.TILE_SIZE;
        float y = tileY * ClientDungeonMap.TILE_SIZE;

        float centerX = x + ClientDungeonMap.TILE_SIZE * 0.5f;
        float centerY = y + ClientDungeonMap.TILE_SIZE * 0.5f;

        float[] pos = {centerX, centerY};

        VectorUtils.prj2(pos, 0f, rotation, camera.position.x, camera.position.y);

        batch.draw(texture, pos[0] - texture.getRegionWidth() * 0.5f, pos[1], texture.getRegionWidth(), texture.getRegionHeight());
    }

    private static void drawQuad(PolygonSpriteBatch batch, TextureRegion texture, Vector2 bl, Vector2 br, Vector2 tr, Vector2 tl) {
        float color = Color.WHITE.toFloatBits();

        float u  = texture.getU();
        float v  = texture.getV2();

        float u2 = texture.getU2();
        float v2 = texture.getV();

        int i = 0;

        // BL
        QUAD_VERTICES[i++] = bl.x;
        QUAD_VERTICES[i++] = bl.y;
        QUAD_VERTICES[i++] = color;
        QUAD_VERTICES[i++] = u;
        QUAD_VERTICES[i++] = v;

        // BR
        QUAD_VERTICES[i++] = br.x;
        QUAD_VERTICES[i++] = br.y;
        QUAD_VERTICES[i++] = color;
        QUAD_VERTICES[i++] = u2;
        QUAD_VERTICES[i++] = v;

        // TR
        QUAD_VERTICES[i++] = tr.x;
        QUAD_VERTICES[i++] = tr.y;
        QUAD_VERTICES[i++] = color;
        QUAD_VERTICES[i++] = u2;
        QUAD_VERTICES[i++] = v2;

        // TL
        QUAD_VERTICES[i++] = tl.x;
        QUAD_VERTICES[i++] = tl.y;
        QUAD_VERTICES[i++] = color;
        QUAD_VERTICES[i++] = u;
        QUAD_VERTICES[i++] = v2;

        batch.draw(texture.getTexture(), QUAD_VERTICES, 0, 20, QUAD_INDICES, 0, 6);
    }

    public static void renderBullet(BulletNode<?> node, Dungeoneer client, Camera camera, float rotation, PolygonSpriteBatch batch, float dt) {
        if (node instanceof BulletGroup group) {
            group.getChildren().forEach(child -> renderBullet(child, client, camera, rotation, batch, dt));
            return;
        }

        Bullet bullet = (Bullet) node;

        Texture texture = client.getAssets().getTexture(Assets.Atlas.BULLET, bullet.getType().id());

        float scale = bullet.getType() instanceof SingleBulletType singleBulletType ? singleBulletType.scale() : 1f;
        float rotationSpeed = bullet.getType() instanceof SingleBulletType singleBulletType ? singleBulletType.rotationSpeed() : 0f;
        float angleOffset = bullet.getType() instanceof SingleBulletType singleBulletType ? singleBulletType.angleOffset() : 0f;

        float spin = 0f;
        if (rotationSpeed != 0f) {
            bullet.incrementAngle(rotationSpeed * dt);
            spin = bullet.getAngle();
        }

        float[] projected = bullet.getPos().clone();
        VectorUtils.prj2(projected, 0f, rotation, camera.position.x, camera.position.y);

        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;

        batch.draw(texture,
            projected[0] - width * .5f, projected[1] - height * .5f,
            width * .5f, height * .5f,
            width, height,
            1f, 1f,
            angleDeg(bullet) + angleOffset + spin + rotation,
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false
        );
    }

    public static float angleDeg(Bullet bullet) {
        float angle = (float) Math.atan2(bullet.getDirY(), bullet.getDirX()) * MathUtils.radiansToDegrees;
        if (angle < 0f)
            angle += 360f;
        return angle;
    }
}
