package dev.creoii.dungeoneer.client.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
import dev.creoii.dungeoneer.network.c2s.raid.EndRaidC2S;
import dev.creoii.dungeoneer.util.Constants;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import javax.annotation.Nullable;
import java.util.Comparator;

public class GameScreen extends AbstractScreen {
    private static final float[] WALL_VERTS = new float[20];
    private static final short[] QUAD_INDICES = {
        0, 1, 2,
        2, 3, 0
    };
    private static final float[] QUAD_VERTICES = new float[20];

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

        inputListener.updateMousePos(camera);
        if (inputListener.isAttacking())
            inputListener.tryAttack();

        camera.position.x = character.getRenderX() + character.getSprite().getWidth() * .5f;
        camera.position.y = character.getRenderY() + character.getSprite().getHeight() * .5f;
        camera.update();

        //mapRenderer.setView(camera);
        //mapRenderer.render();

        inputListener.updateRotation();

        polygonBatch.setProjectionMatrix(camera.combined);
        polygonBatch.begin();

        TiledMapTileLayer ground = ((TiledMapTileLayer) mapRenderer.getMap().getLayers().get(Constants.MAP_LAYER_GROUND));
        for (int x = 0; x < ground.getWidth(); x++) {
            for (int y = 0; y < ground.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = ground.getCell(x, y);
                if (cell != null) {
                    drawGroundTile(polygonBatch, cell.getTile().getTextureRegion(), x, y, inputListener.getRotation());
                }
            }
        }

        for (ClientDungeonMap.WallFace face : getClient().getState().getCurrentRaid().getDungeon().getWallFaces()) {
            if (ClientDungeonMap.WallFace.isVisible(face.direction(), inputListener.getRotation())) {
                drawWall(polygonBatch, face, inputListener.getRotation());
            }
        }
        polygonBatch.end();


        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.setShader(Assets.BORDER_SHADER);
        Assets.BORDER_SHADER.setUniformf("u_pixelSize", (1f / character.getSprite().getWidth()) * .25f, (1f / character.getSprite().getHeight()) * .25f);
        Assets.BORDER_SHADER.setUniformf("u_borderColor", Color.BLACK);

        for (Bullet bullet : raid.getBullets().values()) {
            renderBullet(bullet, getClient(), batch, dt);
        }

        for (BulletGroup bulletGroup : raid.getBulletGroups().values()) {
            bulletGroup.getChildren().forEach(child -> renderBullet(child, getClient(), batch, dt));
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

        visibleCharacters.sort(Comparator.comparingDouble(clientCharacter -> -clientCharacter.getRenderY()));
        visibleCharacters.forEach(clientCharacter -> clientCharacter.render(batch));

        batch.setShader(null);
        batch.end();

        if (getClient().getSettings().debug().value()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            character.renderDebug(shapeRenderer, character.getDirectionToMouse(inputListener));

            shapeRenderer.end();
        }

        super.render(dt);
    }

    private void drawWall(PolygonSpriteBatch batch, ClientDungeonMap.WallFace face, float rotation) {
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

        float color = Color.WHITE.toFloatBits();

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


    private void drawGroundTile(PolygonSpriteBatch batch, TextureRegion texture, int tileX, int tileY, float angle) {
        float x = tileX * ClientDungeonMap.TILE_SIZE;
        float y = tileY * ClientDungeonMap.TILE_SIZE;

        float centerX = camera.position.x;
        float centerY = camera.position.y;

        Vector2 bl = ClientDungeonMap.project(x, y, 0f, angle, centerX, centerY);
        Vector2 br = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y, 0f, angle, centerX, centerY);
        Vector2 tr = ClientDungeonMap.project(x + ClientDungeonMap.TILE_SIZE, y + ClientDungeonMap.TILE_SIZE, 0f, angle, centerX, centerY);
        Vector2 tl = ClientDungeonMap.project(x,y + ClientDungeonMap.TILE_SIZE, 0f, angle, centerX, centerY);

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

    public void renderBullet(BulletNode<?> node, Dungeoneer client, SpriteBatch batch, float dt) {
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
