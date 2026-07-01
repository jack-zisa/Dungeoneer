package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.screen.game.GameScreen;
import dev.creoii.dungeoneer.client.render.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.sided.Raid;

import java.time.Duration;
import java.util.Arrays;

public class ClientRaid extends Raid<ClientBullet, ClientBulletGroup, ClientCharacter> {
    private final Dungeoneer client;
    private final ClientDungeonMap dungeonMap;

    private final Pool<ClientBullet> bulletPool = new Pool<>() {
        @Override
        protected ClientBullet newObject() {
            return new ClientBullet();
        }
    };
    private final Pool<ClientBulletGroup> bulletGroupPool = new Pool<>() {
        @Override
        protected ClientBulletGroup newObject() {
            return new ClientBulletGroup();
        }
    };

    public ClientRaid(Dungeoneer client, RaidDefinition raid) {
        super(raid);
        this.client = client;
        dungeonMap = new ClientDungeonMap(client);
    }

    @Override
    public Pool<ClientBullet> getBulletPool() {
        return bulletPool;
    }

    @Override
    public Pool<ClientBulletGroup> getBulletGroupPool() {
        return bulletGroupPool;
    }

    public ClientDungeonMap getDungeonMap() {
        return dungeonMap;
    }

    public String getRemainingTimeString() {
        Duration duration = Duration.ofMillis(getRemainingTimeMs());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (hours <= 0L && minutes < 0L) {
            return String.valueOf(seconds);
        } else if (hours <= 0L) {
            return String.format("%02d:%02d", minutes, seconds);
        } else return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void syncTimer(long timeRemaining) {
        setEndTime(System.currentTimeMillis() + timeRemaining);
    }

    @Override
    public void updateSpawnPositions(float spawnX, float spawnY) {
        client.getState().getActiveCharacter().setPos(spawnX, spawnY);
        client.getState().getActiveCharacter().setRenderPos(spawnX, spawnY);
        getCharacters().values().forEach(c -> {
            c.setPos(spawnX, spawnY);
            c.setRenderPos(spawnX, spawnY);
        });
    }

    public void update(float dt) {
        if (getStatus() == Status.ACTIVE) {
            if (client.getScreen() instanceof GameScreen gameScreen) {
                long remaining = getRemainingTimeMs();
                if (remaining <= 0L) {
                    Gdx.app.postRunnable(() -> {
                        client.getState().setStatus(ClientState.Status.LOBBY);
                        client.setScreen(new MainScreen(client));
                        end();
                    });
                } else gameScreen.getTimeRemainingLabel().setText(getRemainingTimeString());
            }

            super.update(dt);

            getCharacters().values().forEach(clientCharacter -> clientCharacter.update(dt));
        }
    }

    @Override
    public void end() {
        super.end();
        client.getState().getActiveCharacter().setPos(0f, 0f);
        client.getState().getActiveCharacter().setRenderPos(0f, 0f);
    }
}
