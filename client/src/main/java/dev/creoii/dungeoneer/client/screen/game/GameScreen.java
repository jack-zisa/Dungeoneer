package dev.creoii.dungeoneer.client.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
import dev.creoii.dungeoneer.client.control.CharacterInputListener;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientLaser;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.client.game.*;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.client.util.RenderUtils;
import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import dev.creoii.dungeoneer.network.c2s.raid.EndRaidC2S;
import dev.creoii.dungeoneer.util.Constants;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;

public class GameScreen extends AbstractScreen {
    private final ObjectList<ClientCharacter> visibleCharacters;
    private OrthographicCamera camera;
    private CharacterInputListener inputListener;
    private OrthogonalTiledMapRenderer mapRenderer;
    private SpriteBatch batch;
    private PolygonSpriteBatch polygonBatch;
    private ShapeRenderer shapeRenderer;
    private Label timeRemainingLabel;
    private HealthBar healthBar;

    public GameScreen(Dungeoneer client) {
        super(client);
        visibleCharacters = new ObjectArrayList<>();
        visibleCharacters.add(getClient().getState().getActiveCharacter());
        visibleCharacters.addAll(client.getState().getCurrentRaid().getCharacters().values());
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Label getTimeRemainingLabel() {
        return timeRemainingLabel;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false);
        camera.zoom = .25f;
        camera.update();

        batch = new SpriteBatch();
        polygonBatch = new PolygonSpriteBatch();

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

    @Override
    public void render(float dt) {
        ClientCharacter character = getClient().getState().getActiveCharacter();
        if (character.isNull())
            return;

        ClientRaid raid = getClient().getState().getCurrentRaid();
        if (raid.isNull())
            return;

        character.update(dt);

        inputListener.updateMousePos(camera, inputListener.getRotation());
        if (inputListener.isAttacking())
            inputListener.tryAttack();

        camera.position.x = character.getRenderX() + character.getSprite().getWidth() * .5f;
        camera.position.y = character.getRenderY() + character.getSprite().getHeight() * .5f;
        camera.update();

        //mapRenderer.setView(camera);
        //mapRenderer.render();

        inputListener.updateRotation();

        java.util.List<WallRenderable> renderables = new ArrayList<>(getClient().getState().getCurrentRaid().getDungeon().getWallTops());
        for (ClientDungeonMap.WallFace face : getClient().getState().getCurrentRaid().getDungeon().getWallFaces()) {
            if (ClientDungeonMap.WallFace.isVisible(face.direction(), inputListener.getRotation())) {
                renderables.add(face);
            }
        }

        renderables.sort(Comparator.comparingDouble(r -> -r.depth(inputListener.getRotation(), camera)));

        polygonBatch.setProjectionMatrix(camera.combined);
        polygonBatch.begin();

        TiledMapTileLayer ground = ((TiledMapTileLayer) mapRenderer.getMap().getLayers().get(Constants.MAP_LAYER_GROUND));
        for (int x = 0; x < ground.getWidth(); x++) {
            for (int y = 0; y < ground.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = ground.getCell(x, y);
                if (cell != null) {
                    RenderUtils.drawGroundTile(camera, polygonBatch, cell.getTile().getTextureRegion(), x, y, inputListener.getRotation());
                }
            }
        }

        for (WallRenderable renderable : renderables) {
            if (renderable instanceof ClientDungeonMap.WallTop(TextureRegion texture, int x, int y)) {
                RenderUtils.drawWallTop(camera, polygonBatch, texture, x, y, inputListener.getRotation());
            } else if (renderable instanceof ClientDungeonMap.WallFace face) {
                RenderUtils.drawWall(camera, polygonBatch, face, inputListener.getRotation());
            }
        }
        polygonBatch.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.setShader(Assets.BORDER_SHADER);
        Assets.BORDER_SHADER.setUniformf("u_pixelSize", (1f / character.getSprite().getWidth()) * .25f, (1f / character.getSprite().getHeight()) * .25f);
        Assets.BORDER_SHADER.setUniformf("u_borderColor", Color.BLACK);

        for (Bullet bullet : raid.getBullets().values()) {
            RenderUtils.renderBullet(bullet, getClient(), camera, inputListener.getRotation(), batch, dt);
        }

        for (BulletGroup bulletGroup : raid.getBulletGroups().values()) {
            bulletGroup.getChildren().forEach(child -> RenderUtils.renderBullet(child, getClient(), camera, inputListener.getRotation(), batch, dt));
        }

        for (ClientLaser laser : raid.getLasers()) {
            Vector2 laserPos = laser.getPos();
            batch.draw(
                Assets.PURPLE_LASER_REGION,
                laserPos.x, laserPos.y - laser.getWidth() * .5f,
                0, laser.getWidth() * .5f,
                laser.getLength(), laser.getWidth(),
                1f, 1f,
                laser.getDirection().angleDeg()
            );
        }

        visibleCharacters.sort((a, b) -> {
            float ay = ClientDungeonMap.project(a.getRenderX(), a.getRenderY(), 0f, inputListener.getRotation(), camera.position.x, camera.position.y).y;
            float by = ClientDungeonMap.project(b.getRenderX(), b.getRenderY(), 0f, inputListener.getRotation(), camera.position.x, camera.position.y).y;
            return Float.compare(by, ay);
        });
        visibleCharacters.forEach(clientCharacter -> clientCharacter.render(batch, camera, inputListener.getRotation()));

        batch.setShader(null);
        batch.end();

        if (getClient().getSettings().debug().value()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            character.renderDebug(shapeRenderer, inputListener.getDirectionToMouse(character.getCenterX(), character.getCenterY()));

            shapeRenderer.end();
        }

        super.render(dt);
    }

    @Override
    public void dispose() {
        super.dispose();
        SKIN.dispose();
        mapRenderer.dispose();
        batch.dispose();
        polygonBatch.dispose();
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
