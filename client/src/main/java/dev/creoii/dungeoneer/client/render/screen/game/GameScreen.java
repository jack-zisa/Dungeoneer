package dev.creoii.dungeoneer.client.render.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.control.CharacterInputListener;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.client.game.*;
import dev.creoii.dungeoneer.client.render.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.render.screen.main.MainScreen;
import dev.creoii.dungeoneer.client.render.GroundTileRenderable;
import dev.creoii.dungeoneer.client.render.ObjectTileRenderable;
import dev.creoii.dungeoneer.client.render.RenderLayer;
import dev.creoii.dungeoneer.client.render.Renderable;
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

    public HealthBar getHealthBar() {
        return healthBar;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false);
        camera.zoom = .25f;
        camera.update();

        polygonBatch = new PolygonSpriteBatch();

        mapRenderer = new OrthogonalTiledMapRenderer(getClient().getState().getCurrentRaid().getDungeonMap().getMap());

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

        healthBar = new HealthBar(getClient().getState().getActiveCharacter(), getStage().getViewport().getWorldWidth() / 3f, 20f, false);
        root.add(healthBar).width(getStage().getViewport().getWorldWidth() / 3f).height(24f);

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

        inputListener.updateRotation();
        inputListener.updateMousePos(camera, inputListener.getRotation());
        if (inputListener.isAttacking())
            inputListener.tryAttack();

        camera.position.x = character.getRenderX() + character.getSprite().getWidth() * .5f;
        camera.position.y = character.getRenderY() + character.getSprite().getHeight() * .5f;
        camera.update();

        java.util.List<Renderable> renderables = new ArrayList<>(visibleCharacters);
        renderables.addAll(getClient().getState().getCurrentRaid().getDungeonMap().getWallTops());
        for (ClientDungeonMap.WallFace face : getClient().getState().getCurrentRaid().getDungeonMap().getWallFaces()) {
            if (ClientDungeonMap.WallFace.isVisible(face.direction(), inputListener.getRotation())) {
                renderables.add(face);
            }
        }

        TiledMapTileLayer ground = ((TiledMapTileLayer) mapRenderer.getMap().getLayers().get(Constants.MAP_LAYER_GROUND));
        for (int x = 0; x < ground.getWidth(); x++) {
            for (int y = 0; y < ground.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = ground.getCell(x, y);
                if (cell != null) {
                    renderables.add(new GroundTileRenderable(cell.getTile().getTextureRegion(), x, y));
                }
            }
        }

        TiledMapTileLayer object = ((TiledMapTileLayer) mapRenderer.getMap().getLayers().get(Constants.MAP_LAYER_OBJECT));
        if (object != null) {
            for (int x = 0; x < object.getWidth(); x++) {
                for (int y = 0; y < object.getHeight(); y++) {
                    TiledMapTileLayer.Cell cell = object.getCell(x, y);
                    if (cell != null) {
                        renderables.add(new ObjectTileRenderable(cell.getTile().getTextureRegion(), x, y));
                    }
                }
            }
        }

        renderables.addAll(raid.getBullets().values());
        renderables.addAll(raid.getBulletGroups().values());

        renderables.sort(Comparator.comparingInt((Renderable r) -> r.renderLayer().getPriority()).thenComparingDouble(r -> -r.depth(inputListener.getRotation(), camera)));

        polygonBatch.setProjectionMatrix(camera.combined);
        polygonBatch.begin();

        ShaderProgram shader = null;
        for (Renderable renderable : renderables) {
            ShaderProgram toShader = renderable.renderLayer() == RenderLayer.OBJECT_OUTLINED ? Assets.BORDER_SHADER : null;
            if (toShader != shader) {
                polygonBatch.end();
                polygonBatch.setShader(toShader);

                if (toShader == Assets.BORDER_SHADER) {
                    Assets.BORDER_SHADER.setUniformf("u_pixelSize", (1f / character.getSprite().getWidth()) * .25f, (1f / character.getSprite().getHeight()) * .25f);
                    Assets.BORDER_SHADER.setUniformf("u_borderColor", Color.BLACK);
                }

                polygonBatch.begin();
                shader = toShader;
            }
            renderable.render(getClient(), polygonBatch, camera, inputListener.getRotation(), dt);
        }

        polygonBatch.end();

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
        polygonBatch.dispose();
    }

    public static class HealthBar extends Widget {
        private final ClientCharacter character;
        private final NinePatch emptyPatch;
        private final NinePatch filledPatch;
        private float percent = 1f;
        @Nullable private final Label amount;

        public HealthBar(ClientCharacter character, float width, float height, boolean displayAmount) {
            this.character = character;

            emptyPatch = Assets.HEALTH_BAR_EMPTY_9PATCH;
            filledPatch = Assets.HEALTH_BAR_9PATCH;

            setSize(width, height);

            if (displayAmount) {
                amount = new Label("", SKIN);
                updateAmount();
            } else amount = null;
        }

        public void setPercent(float percent) {
            this.percent = MathUtils.clamp(percent, 0f, 1f);
            if (amount != null) {
                updateAmount();
            }
        }

        private void updateAmount() {
            if (amount != null) amount.setText(character.getStats().health().value() + "/" + character.getMaxStats().health().value());
        }

        @Override
        public float getPrefWidth() {
            return getWidth();
        }

        @Override
        public float getPrefHeight() {
            return getHeight();
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            validate();

            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

            float x = getX();
            float y = getY();
            float width = getWidth();
            float height = getHeight();

            emptyPatch.draw(batch, x, y, width, height);

            if (percent > 0f) {
                filledPatch.draw(batch, x, y, width * percent, height);
            }

            if (amount != null) {
                amount.setBounds(x, y, width, height);
                amount.draw(batch, parentAlpha);
            }
        }

        public void update() {
            float percent = (float) character.getStats().health().value() / character.getMaxStats().health().value();
            setPercent(percent);
        }
    }
}
