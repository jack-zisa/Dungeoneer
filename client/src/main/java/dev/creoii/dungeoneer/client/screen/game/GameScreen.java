package dev.creoii.dungeoneer.client.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.network.c2s.raid.EndRaidC2S;
import dev.creoii.dungeoneer.util.stat.StatUtils;

public class GameScreen extends AbstractScreen {
    private final Dungeoneer client;
    private Skin skin;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer mapRenderer;
    private SpriteBatch batch;

    public GameScreen(Dungeoneer client) {
        this.client = client;
    }

    public Dungeoneer getClient() {
        return client;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = .4f;

        batch = new SpriteBatch();

        client.getState().getCurrentRaid().getDungeon().build();
        mapRenderer = new OrthogonalTiledMapRenderer(client.getState().getCurrentRaid().getDungeon().getMap());

        skin = new Skin(Gdx.files.internal("uiskin.json"));

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

        root.add(new Label(String.format("Raiding %s!", client.getState().getCurrentRaid().get().target().username()), skin)).left().row();

        TextButton surrenderButton = new TextButton("Surrender", skin);
        surrenderButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().get().sendUDP(new EndRaidC2S(client.getState().getCurrentRaid().get().id()));
                getClient().setScreen(new MainScreen(client));
            }
        });
        root.add(surrenderButton).left();

        getStage().addActor(root);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(getStage());
        multiplexer.addProcessor(client.getController());
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        ClientCharacter character = client.getState().getActiveCharacter();

        character.getRenderPos().mulAdd(character.getVelocity(), StatUtils.getCalculatedSpeed(character.getStats().speed().value()) * delta);

        Vector2 correction = character.getCorrection();
        float error = correction.len();
        if (error > 30f) {
            Dungeoneer.LOGGER.debug("Correcting client position %s to %s", character.getRenderPos().toString(), character.getPos().toString());
            character.getRenderPos().set(character.getPos());
            correction.setZero();
        } else if (error > 5f) {
            Dungeoneer.LOGGER.debug("Correcting client position %s to %s", character.getRenderPos().toString(), character.getPos().toString());
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

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        Sprite sprite = character.getSprite();
        sprite.setPosition(character.getRenderPos().x, character.getRenderPos().y);
        sprite.draw(batch);
        batch.end();

        super.render(delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
        mapRenderer.dispose();
        batch.dispose();
    }
}
