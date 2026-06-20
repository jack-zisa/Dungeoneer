package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.screen.game.GameScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.definitions.attack.bullet.*;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

public class ClientRaid extends Raid<Bullet, BulletGroup, ClientCharacter> {
    private final Dungeoneer client;
    private final ClientDungeonMap dungeon;

    private final Pool<Bullet> bulletPool = new Pool<>() {
        @Override
        protected Bullet newObject() {
            return new Bullet();
        }
    };
    private final Pool<BulletGroup> bulletGroupPool = new Pool<>() {
        @Override
        protected BulletGroup newObject() {
            return new BulletGroup();
        }
    };

    private final Pool<ClientLaser> laserPool = new Pool<>() {
        @Override
        protected ClientLaser newObject() {
            return new ClientLaser();
        }
    };
    private final Array<ClientLaser> lasers = new Array<>();

    public ClientRaid(Dungeoneer client, RaidDefinition raid) {
        super(raid);
        this.client = client;
        dungeon = new ClientDungeonMap();
    }

    @Override
    public Pool<Bullet> getBulletPool() {
        return bulletPool;
    }

    @Override
    public Pool<BulletGroup> getBulletGroupPool() {
        return bulletGroupPool;
    }

    public Array<ClientLaser> getLasers() {
        return lasers;
    }

    public ClientDungeonMap getDungeon() {
        return dungeon;
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

    public void update(float dt) {
        if (client.getScreen() instanceof GameScreen gameScreen) {
            long remaining = getRemainingTimeMs();
            if (remaining <= 0L) {
                Gdx.app.postRunnable(() -> {
                    client.getState().setStatus(ClientState.Status.LOBBY);
                    client.getState().getCurrentRaid().end();
                    client.setScreen(new MainScreen(client));
                });
            } else gameScreen.getTimeRemainingLabel().setText(getRemainingTimeString());
        }

        super.update(dt);

        getCharacters().values().forEach(clientCharacter -> clientCharacter.update(dt));

        for (int i = lasers.size - 1; i >= 0; --i) {
            ClientLaser laser = lasers.get(i);
            if (!laser.update(dt)) {
                lasers.removeIndex(i);
                laserPool.free(laser);
            }
        }
    }

    @Override
    public void end() {
        super.end();

        if (lasers.notEmpty()) {
            laserPool.freeAll(lasers);
            lasers.clear();
        }
    }

    public void addLaser(float x, float y, float angleOffset, float width, float length, float lifetime, @Nullable ClientCharacter character) {
        ClientLaser poolLaser = laserPool.obtain();
        poolLaser.setPos(x, y);
        poolLaser.setAngleOffset(angleOffset);
        poolLaser.setAttached(character);
        poolLaser.setWidth(width);
        poolLaser.setLength(length);
        poolLaser.setLifetime(lifetime);
        lasers.add(poolLaser);
    }
}
