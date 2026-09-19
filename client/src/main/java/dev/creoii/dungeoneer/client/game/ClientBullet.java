package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.RenderLayer;
import dev.creoii.dungeoneer.client.render.RenderUtils;
import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import org.jspecify.annotations.Nullable;

public class ClientBullet extends Bullet<ClientRaid> implements ClientEntity {
    private long id;
    private ClientRaid raid;

    public ClientBullet(ClientRaid raid) {
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
        RenderUtils.renderBullet(this, client, camera, rotation, batch, dt);
    }

    @Override
    public void renderDebug(ShapeRenderer shapeRenderer, float[] mouseDir) {
        if (isDead()) return;

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(getX() - getBounds().width * .5f, getY() - getBounds().height * .5f, getBounds().width, getBounds().height);
    }

    @Override
    public float depth(float rotation, OrthographicCamera camera) {
        return ClientDungeonMap.project(getX() + getType().scale() * .5f, getY() + getType().scale() * .5f, 0f, rotation, camera.position.x, camera.position.y).y;
    }
}
