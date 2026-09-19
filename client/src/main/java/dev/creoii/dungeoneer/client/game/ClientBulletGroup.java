package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.RenderLayer;
import dev.creoii.dungeoneer.client.render.RenderUtils;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import org.jspecify.annotations.Nullable;

public class ClientBulletGroup extends BulletGroup<ClientRaid> implements ClientEntity {
    private long id;
    private ClientRaid raid;

    public ClientBulletGroup(ClientRaid raid) {
        this.raid = raid;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public @Nullable ClientRaid getRaid() {
        return raid;
    }

    @Override
    public void setRaid(@Nullable ClientRaid raid) {
        this.raid = raid;
    }

    @Override
    public RenderLayer renderLayer() {
        return RenderLayer.OBJECT_OUTLINED;
    }

    @Override
    public void render(Dungeoneer client, PolygonSpriteBatch batch, Camera camera, float rotation, float dt) {
        getChildren().forEach(child -> RenderUtils.renderBullet(child, client, camera, rotation, batch, dt));
    }

    @Override
    public void renderDebug(ShapeRenderer shapeRenderer, float[] mouseDir) {
        if (isDead()) return;

        getChildren().forEach(bulletNode -> {
            if (bulletNode instanceof ClientBullet clientBullet) clientBullet.renderDebug(shapeRenderer, mouseDir);
            else if (bulletNode instanceof ClientBulletGroup clientBulletGroup) clientBulletGroup.renderDebug(shapeRenderer, mouseDir);
        });
    }

    @Override
    public float depth(float rotation, OrthographicCamera camera) {
        return ClientDungeonMap.project(getX(), getY(), 0f, rotation, camera.position.x, camera.position.y).y;
    }
}
