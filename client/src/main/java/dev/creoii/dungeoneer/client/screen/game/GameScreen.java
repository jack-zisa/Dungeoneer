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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientBullet;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.network.c2s.raid.EndRaidC2S;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.stat.StatUtils;

import javax.annotation.Nullable;

public class GameScreen extends AbstractScreen {
    private final Dungeoneer client;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer mapRenderer;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private HealthBar healthBar;

    public GameScreen(Dungeoneer client) {
        this.client = client;
    }

    public Dungeoneer getClient() {
        return client;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = .25f;

        batch = new SpriteBatch();

        mapRenderer = new OrthogonalTiledMapRenderer(client.getState().getCurrentRaid().getDungeon().getMap());

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        ClientRaid currentRaid = client.getState().getCurrentRaid();
        if (currentRaid.isNull()) {
            client.getState().setStatus(ClientState.Status.LOBBY);
            client.setScreen(new MainScreen(client));
            Dungeoneer.LOGGER.error("Raid failed to start");
            return;
        }

        Table root = new Table();
        root.setFillParent(true);
        root.top().left();

        root.add(new Label(String.format("Raiding %s!", client.getState().getCurrentRaid().get().target().username()), SKIN)).left().row();

        TextButton surrenderButton = new TextButton("Surrender", SKIN);
        surrenderButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().get().sendUDP(new EndRaidC2S(client.getState().getCurrentRaid().get().id()));
                getClient().setScreen(new MainScreen(client));
                getClient().getState().getActiveCharacter().getPos().setZero();
                getClient().getState().getActiveCharacter().getRenderPos().setZero();
                getClient().getState().setStatus(ClientState.Status.LOBBY);
                getClient().getState().getCurrentRaid().end();
            }
        });
        root.add(surrenderButton).left().row();
        root.add(new Table()).grow().row();

        healthBar = new HealthBar(client.getState().getActiveCharacter(), getStage().getViewport().getWorldWidth() / 3f, false);
        root.add(healthBar).width(getStage().getViewport().getWorldWidth() / 3f);

        getStage().addActor(root);
        getStage().addListener(client.getInputListener());
        super.show();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void render(float delta) {
        ClientCharacter character = client.getState().getActiveCharacter();
        if (character.isNull())
            return;

        ClientRaid raid = client.getState().getCurrentRaid();
        if (raid.isNull())
            return;

        character.getRenderPos().mulAdd(character.getVelocity(), StatUtils.getCalculatedSpeed(character.getStats().speed().value()) * delta);

        Vector2 correction = character.getCorrection();
        float error = correction.len();
        if (error > 30f) {
            if (client.getSettings().debug().value()) Dungeoneer.LOGGER.debug("Correcting client position %s to %s", character.getRenderPos().toString(), character.getPos().toString());
            character.getRenderPos().set(character.getPos());
            correction.setZero();
        } else if (error > 5f) {
            if (client.getSettings().debug().value()) Dungeoneer.LOGGER.debug("Correcting client position %s to %s", character.getRenderPos().toString(), character.getPos().toString());
            float amount = Math.min(correction.len(), 15f * delta);
            correction.nor();
            character.getRenderPos().mulAdd(correction, amount);
            correction.scl(Math.max(0f, 1f - amount / error));
        }

        camera.position.x = character.getRenderPos().x + character.getSprite().getWidth() * .5f;
        camera.position.y = character.getRenderPos().y + character.getSprite().getHeight() * .5f;
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

        Sprite sprite = character.getSprite();
        sprite.setPosition(character.getRenderPos().x, character.getRenderPos().y);
        sprite.draw(batch);

        for (ClientBullet bullet : raid.getBullets()) {
            Vector2 pos = bullet.getPos();
            Texture texture = client.getAssets().getTexture(Assets.Atlas.BULLET, "ice_magic_blade");
            bullet.incrementAngle(bullet.getRotation() * delta);

            batch.draw(texture,
                pos.x - texture.getWidth() * .5f, pos.y - texture.getHeight() * .5f,
                texture.getWidth() * .5f, texture.getHeight() * .5f,
                texture.getWidth(), texture.getHeight(),
                1f, 1f,
                bullet.getDirection().angleDeg() + bullet.getAngleOffset() + bullet.getAngle(),
                0, 0,
                texture.getWidth(), texture.getHeight(),
                false, false
            );
        }

        batch.setShader(null);
        batch.end();

        if (client.getSettings().debug().value()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            float x = character.getRenderPos().x + 4f;
            float y = character.getRenderPos().y + 4f;

            shapeRenderer.setColor(character.isAttackPending() ? Color.GREEN : Color.WHITE);
            Vector2 mouseDir = client.getInputListener().getDirectionToMouse(x, y);
            shapeRenderer.line(x, y, x + mouseDir.x * 32f, y + mouseDir.y * 32f);
            shapeRenderer.end();
        }

        super.render(delta);
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
