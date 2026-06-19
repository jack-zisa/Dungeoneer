package dev.creoii.dungeoneer.client.screen.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.control.CharacterInputListener;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientLaser;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.client.game.*;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import dev.creoii.dungeoneer.definitions.attack.bullet.SingleBulletType;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.network.c2s.raid.EndRaidC2S;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.stat.StatUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class GameScreen extends AbstractScreen {
    private OrthographicCamera camera;
    private CharacterInputListener inputListener;
    private OrthogonalTiledMapRenderer mapRenderer;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Label timeRemainingLabel;
    private HealthBar healthBar;

    public GameScreen(Dungeoneer client) {
        super(client);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Label getTimeRemainingLabel() {
        return timeRemainingLabel;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = .25f;

        batch = new SpriteBatch();

        mapRenderer = new OrthogonalTiledMapRenderer(getClient().getState().getCurrentRaid().getDungeon().getMap());

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        ClientRaid currentRaid = getClient().getState().getCurrentRaid();
        if (currentRaid.isNull()) {
            getClient().getState().setStatus(ClientState.Status.LOBBY);
            getClient().setScreen(new MainScreen(getClient()));
            Dungeoneer.LOGGER.error("Raid failed to start");
            return;
        }

        Table root = new Table();
        root.setFillParent(true);
        root.top().left();

        root.add(new Label(String.format("Raiding %s!", getClient().getState().getCurrentRaid().get().target().username()), SKIN)).left().row();
        root.add(timeRemainingLabel = new Label(String.format("Time Remaining: %s!", getClient().getState().getCurrentRaid().getRemainingTimeString()), SKIN)).left().row();

        TextButton surrenderButton = new TextButton("Surrender", SKIN);
        surrenderButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().get().sendTCP(new EndRaidC2S(getClient().getState().getCurrentRaid().get().id()));
                getClient().setScreen(new MainScreen(getClient()));
                getClient().getState().getActiveCharacter().setPos(0f, 0f);
                getClient().getState().getActiveCharacter().setRenderPos(0f, 0f);
                getClient().getState().setStatus(ClientState.Status.LOBBY);
                getClient().getState().getCurrentRaid().end();
            }
        });
        root.add(surrenderButton).left().row();
        root.add(new Table()).grow().row();

        healthBar = new HealthBar(getClient().getState().getActiveCharacter(), getStage().getViewport().getWorldWidth() / 3f, false);
        root.add(healthBar).width(getStage().getViewport().getWorldWidth() / 3f);

        getStage().addActor(root);

        getClient().getInputMultiplexer().addProcessor(getStage());
        getClient().getInputMultiplexer().addProcessor(inputListener = new CharacterInputListener(getClient()));
    }

    @Override
    public void hide() {
        getClient().getInputMultiplexer().removeProcessor(inputListener);
        getClient().getInputMultiplexer().removeProcessor(getStage());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    public void applyCorrection(Entity entity, float[] pos, float[] correction, float[] renderPos, float dt, BiConsumer<Float, Float> setRenderPos) {
        float error = VectorUtils.len(correction);
        if (error > 30f) {
            if (getClient().getSettings().debug().value()) Dungeoneer.LOGGER.debug("Correcting client position %s to %s", Arrays.toString(renderPos), Arrays.toString(pos));
            setRenderPos.accept(entity.getX(), entity.getY());
            VectorUtils.setZero(correction);
        } else if (error > 5f) {
            if (getClient().getSettings().debug().value()) Dungeoneer.LOGGER.debug("Correcting client position %s to %s", Arrays.toString(renderPos), Arrays.toString(pos));
            float amount = Math.min(error, 15f * dt);
            VectorUtils.nor(correction);
            VectorUtils.mulAdd(renderPos, correction, amount);
            VectorUtils.scl(correction, Math.max(0f, 1f - amount / error));
        }
    }

    @Override
    public void render(float delta) {
        ClientCharacter character = getClient().getState().getActiveCharacter();
        if (character.isNull())
            return;

        ClientRaid raid = getClient().getState().getCurrentRaid();
        if (raid.isNull())
            return;

        float speed = StatUtils.getCalculatedSpeed(character.getStats().speed().value()) * delta;
        VectorUtils.mulAdd(character.getRenderPos(), character.getVelocity(), speed);

        applyCorrection(character, character.getPos(), character.getCorrection(), character.getRenderPos(), delta, character::setRenderPos);

        inputListener.updateMousePos(camera);
        if (inputListener.isAttacking())
            inputListener.tryAttack();

        camera.position.x = character.getRenderX() + character.getSprite().getWidth() * .5f;
        camera.position.y = character.getRenderY() + character.getSprite().getHeight() * .5f;
        camera.update();

        mapRenderer.setView(camera);
        mapRenderer.render();

        TiledMapTile tile = character.getTileOn((TiledMapTileLayer) mapRenderer.getMap().getLayers().get(Constants.MAP_LAYER_GROUND));
        if (tile != null) {
        }

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.setShader(Assets.BORDER_SHADER);
        Assets.BORDER_SHADER.setUniformf("u_pixelSize", (1f / character.getSprite().getWidth()) * .25f, (1f / character.getSprite().getHeight()) * .25f);
        Assets.BORDER_SHADER.setUniformf("u_borderColor", Color.BLACK);

        for (ClientBullet bullet : raid.getBullets().values()) {
            applyCorrection(bullet, bullet.getPos(), bullet.getCorrection(), bullet.getRenderPos(), delta, bullet::setRenderPos);
            renderBullet(bullet, getClient(), batch, delta);
        }

        for (ClientBulletGroup bulletGroup : raid.getBulletGroups().values()) {
            applyCorrection(bulletGroup, bulletGroup.getPos(), bulletGroup.getCorrection(), bulletGroup.getRenderPos(), delta, bulletGroup::setRenderPos);
            bulletGroup.getChildren().forEach(child -> renderBullet(child, getClient(), batch, delta));
        }

        for (ClientLaser laser : raid.getLasers()) {
            Vector2 pos = laser.getPos();
            batch.draw(
                Assets.PURPLE_LASER_REGION,
                pos.x, pos.y - laser.getWidth() * 0.5f,
                0, laser.getWidth() * 0.5f,
                laser.getLength(), laser.getWidth(),
                1f, 1f,
                laser.getDirection().angleDeg()
            );
        }

        Sprite sprite = character.getSprite();
        sprite.setPosition(character.getRenderX(), character.getRenderY());
        sprite.draw(batch);

        batch.setShader(null);
        batch.end();

        if (getClient().getSettings().debug().value()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            float x = character.getCenterX();
            float y = character.getCenterY();

            shapeRenderer.setColor(character.isAttackPending() ? Color.GREEN : Color.WHITE);
            float[] mouseDir = inputListener.getDirectionToMouse(x, y);
            shapeRenderer.line(x, y, x + mouseDir[0] * 32f, y + mouseDir[1] * 32f);
            shapeRenderer.end();
        }

        super.render(delta);
    }

    public void renderBullet(BulletNode node, Dungeoneer client, SpriteBatch batch, float dt) {
        if (node instanceof BulletGroup group) {
            group.getChildren().forEach(child -> renderBullet(child, client, batch, dt));
            return;
        }

        Bullet bullet = (Bullet) node;

        Texture texture = client.getAssets().getTexture(Assets.Atlas.BULLET, bullet.getType().id());

        float scale = bullet.getType() instanceof SingleBulletType singleBulletType ? singleBulletType.scale() : 1f;
        float rotationSpeed = bullet.getType() instanceof SingleBulletType singleBulletType ? singleBulletType.rotationSpeed() : 0f;
        float angleOffset = bullet.getType() instanceof SingleBulletType singleBulletType ? singleBulletType.angleOffset() : 0f;
        float angle = 0f;

        if (rotationSpeed != 0f) {
            bullet.incrementAngle(rotationSpeed * dt);
            angle = bullet.getAngle();
        }

        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;

        batch.draw(texture,
            bullet.getX() - width * .5f, bullet.getY() - height * .5f,
            width * .5f, height * .5f,
            width, height,
            1f, 1f,
            angleDeg(bullet) + angleOffset + angle,
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false
        );
    }

    public float angleDeg(Bullet bullet) {
        float angle = (float) Math.atan2(bullet.getDirY(), bullet.getDirX()) * MathUtils.radiansToDegrees;
        if (angle < 0f)
            angle += 360f;
        return angle;
    }

    @Override
    public void dispose() {
        super.dispose();
        SKIN.dispose();
        mapRenderer.dispose();
        batch.dispose();
    }

    public static class HealthBar extends Stack {
        private final ClientCharacter character;
        private final Container<Table> fillContainer;
        private float percent;
        @Nullable
        private Label amount;

        public HealthBar(ClientCharacter character, float width, boolean displayAmount) {
            this.character = character;
            percent = 1f;

            setWidth(width);
            Table background = new Table();
            background.setBackground(new NinePatchDrawable(Assets.HEALTH_BAR_EMPTY_9PATCH));

            Table fill = new Table();
            fill.setBackground(new NinePatchDrawable(Assets.HEALTH_BAR_9PATCH));

            fillContainer = new Container<>(fill);
            fillContainer.width(getWidth() * percent);
            fillContainer.left();

            add(background);
            add(fillContainer);

            if (displayAmount) {
                add(amount = new Label(String.valueOf(character.getStats().health().value()), SKIN));
            }
        }

        public void setPercent(float percent) {
            if (percent == 0f) {
                fillContainer.setVisible(false);
            } else if (!fillContainer.isVisible()) fillContainer.setVisible(true);

            this.percent = percent;
            fillContainer.width(getWidth() * percent);
            fillContainer.left();

            if (amount != null) {
                amount.setText(character.getStats().health().value());
            }
        }
    }
}
