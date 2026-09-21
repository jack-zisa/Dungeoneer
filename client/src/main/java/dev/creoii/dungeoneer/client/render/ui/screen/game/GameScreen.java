package dev.creoii.dungeoneer.client.render.ui.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
import dev.creoii.dungeoneer.client.render.*;
import dev.creoii.dungeoneer.client.render.object.GroundTileRenderable;
import dev.creoii.dungeoneer.client.render.object.ObjectTileRenderable;
import dev.creoii.dungeoneer.client.render.object.WallFaceRenderable;
import dev.creoii.dungeoneer.client.render.ui.element.InventoryWidget;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.render.ui.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;
import dev.creoii.dungeoneer.network.c2s.raid.LeaveRaidC2S;
import dev.creoii.dungeoneer.util.RemovalReason;
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
    private Label timeRemainingLabel;
    private HealthBar healthBar;
    private InventoryWidget inventory;

    public GameScreen(Dungeoneer client) {
        super(client);
        visibleCharacters = new ObjectArrayList<>();
    }

    public ObjectList<ClientCharacter> getVisibleCharacters() {
        return visibleCharacters;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public CharacterInputListener getInputListener() {
        return inputListener;
    }

    public Label getTimeRemainingLabel() {
        return timeRemainingLabel;
    }

    public HealthBar getHealthBar() {
        return healthBar;
    }

    public InventoryWidget getInventory() {
        return inventory;
    }

    public void refreshVisibleCharacters() {
        visibleCharacters.clear();
        visibleCharacters.add(getClient().getState().getActiveCharacter());
        visibleCharacters.addAll(getClient().getState().getCurrentRaid().getCharacters().values());
    }

    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setToOrtho(false);
        camera.zoom = .25f;
        ClientCharacter character = getClient().getState().getActiveCharacter();
        camera.position.x = character.getRenderX() + character.getBounds().width * .5f;
        camera.position.y = character.getRenderY() + character.getBounds().height * .5f;
        camera.update();

        polygonBatch = new PolygonSpriteBatch();

        mapRenderer = new OrthogonalTiledMapRenderer(getClient().getState().getCurrentRaid().getDungeonMap().getMap());

        ClientRaid currentRaid = getClient().getState().getCurrentRaid();
        if (currentRaid.isNull()) {
            getClient().getState().setStatus(ClientState.Status.LOBBY);
            getClient().setScreen(new MainScreen(getClient()));
            Dungeoneer.LOGGER.error("Raid failed to start");
            return;
        }

        refreshVisibleCharacters();

        Table root = new Table();
        root.setFillParent(true);
        root.top().left();

        root.add(new Label(String.format("Raiding %s!", getClient().getState().getCurrentRaid().get().target().username()), SKIN)).left().padBottom(8f).row();
        root.add(timeRemainingLabel = new Label(String.format("Time Remaining: %s!", getClient().getState().getCurrentRaid().getRemainingTimeString()), SKIN)).left().padBottom(8f).row();

        TextButton surrenderButton = new TextButton("Surrender", SKIN);
        surrenderButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().get().sendTCP(new LeaveRaidC2S(getClient().getState().getCurrentRaid().get().id(), getClient().getState().getAccount().id(), RemovalReason.SURRENDER));
                getClient().setScreen(new MainScreen(getClient()));
                getClient().getState().setStatus(ClientState.Status.LOBBY);
                getClient().getState().getCurrentRaid().end();
            }
        });
        root.add(surrenderButton).left().row();
        root.add(new Table()).grow().row();

        healthBar = new HealthBar(getClient().getState().getActiveCharacter(), getStage().getViewport().getWorldWidth() / 3f, 20f, false);
        root.add(healthBar).width(getStage().getViewport().getWorldWidth() / 3f).height(24f).row();

        inventory = new InventoryWidget(getClient(), getClient().getState().getActiveCharacter().getEquipment(), 4);
        root.add(inventory);

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
        if (character == null || character.isNull())
            return;

        ClientRaid raid = getClient().getState().getCurrentRaid();
        if (raid.isNull())
            return;

        character.tick(dt);

        inputListener.updateRotation(dt);
        inputListener.updateMousePos(camera, inputListener.getRotation());
        if (inputListener.isAttacking())
            inputListener.tryAttack();

        camera.position.x = character.getRenderX() + character.getBounds().width * .5f;
        camera.position.y = character.getRenderY() + character.getBounds().height * .5f;
        camera.update();

        java.util.List<Renderable> renderables = new ArrayList<>(visibleCharacters);
        renderables.addAll(getClient().getState().getCurrentRaid().getDungeonMap().getWallTops());

        for (WallFaceRenderable face : getClient().getState().getCurrentRaid().getDungeonMap().getWallFaces()) {
            if (WallFaceRenderable.isVisible(face.direction(), inputListener.getRotation())) {
                renderables.add(face);
            }
        }

        TiledMapTileLayer ground = ((TiledMapTileLayer) mapRenderer.getMap().getLayers().get(MapLayerType.GROUND.id()));
        for (int x = 0; x < ground.getWidth(); x++) {
            for (int y = 0; y < ground.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = ground.getCell(x, y);
                if (cell == null || cell.getTile().getId() == 0) continue; // air
                renderables.add(new GroundTileRenderable(cell.getTile().getTextureRegion(), x, y));
            }
        }

        TiledMapTileLayer object = ((TiledMapTileLayer) mapRenderer.getMap().getLayers().get(MapLayerType.OBJECT.id()));
        if (object != null) {
            for (int x = 0; x < object.getWidth(); x++) {
                for (int y = 0; y < object.getHeight(); y++) {
                    TiledMapTileLayer.Cell cell = object.getCell(x, y);
                    if (cell == null || cell.getTile().getId() == 0) continue; // air
                    renderables.add(new ObjectTileRenderable(cell.getTile().getTextureRegion(), x, y));
                }
            }
        }

        renderables.addAll(raid.getEntityManager().getEntities());

        renderables.sort(Comparator.comparingInt((Renderable r) -> r.renderLayer().getPriority()).thenComparingDouble(r -> -r.depth(inputListener.getRotation(), camera)));

        polygonBatch.setProjectionMatrix(camera.combined);
        polygonBatch.begin();

        ShaderProgram shader = null;
        for (Renderable renderable : renderables) {
            ShaderProgram toShader = renderable.renderLayer() == RenderLayer.OBJECT_OUTLINED ? Assets.BORDER_SHADER : null;
            if (toShader != shader) {
                polygonBatch.end();
                polygonBatch.setShader(toShader);
                polygonBatch.begin();
                if (toShader == Assets.BORDER_SHADER) {
                    Assets.BORDER_SHADER.setUniformf("u_pixelSize", (1f / character.getSprite().getWidth()) * .25f, (1f / character.getSprite().getHeight()) * .25f);
                    Assets.BORDER_SHADER.setUniformf("u_borderColor", Color.BLACK);
                }
                shader = toShader;
            }
            renderable.render(getClient(), polygonBatch, camera, inputListener.getRotation(), dt);
        }

        polygonBatch.end();

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
            if (amount != null) amount.setText(character.getStats().health().value() + "/" + character.get().characterClass().maxStats());
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
            float percent = (float) character.getStats().health().value() / character.get().characterClass().maxStats().health().value();
            setPercent(percent);
        }
    }
}
