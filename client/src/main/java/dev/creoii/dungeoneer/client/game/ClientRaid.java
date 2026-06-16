package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.screen.game.GameScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletDefinition;
import dev.creoii.dungeoneer.definitions.Raid;
import dev.creoii.dungeoneer.util.Constants;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

public class ClientRaid {
    private final Dungeoneer client;
    private Raid raid;
    private final ClientDungeonMap dungeon;
    private long endTime;

    private final Pool<ClientBullet> bulletPool = new Pool<>() {
        @Override
        protected ClientBullet newObject() {
            return new ClientBullet();
        }
    };
    private final Array<ClientBullet> bullets = new Array<>();

    private final Pool<ClientLaser> laserPool = new Pool<>() {
        @Override
        protected ClientLaser newObject() {
            return new ClientLaser();
        }
    };
    private final Array<ClientLaser> lasers = new Array<>();

    public ClientRaid(Dungeoneer client, Raid raid) {
        this.client = client;
        this.raid = raid;
        dungeon = new ClientDungeonMap();
    }

    public Raid get() {
        return raid;
    }

    public void set(@Nullable Raid raid) {
        this.raid = raid;

        if (raid != null) {
            endTime = System.currentTimeMillis() + Constants.RAID_DURATION_MS;
        } else endTime = -1L;
    }

    public Array<ClientBullet> getBullets() {
        return bullets;
    }

    public Array<ClientLaser> getLasers() {
        return lasers;
    }

    public ClientDungeonMap getDungeon() {
        return dungeon;
    }

    public boolean isNull() {
        return raid == null;
    }

    public long getRemainingTimeMs() {
        return Math.max(0, endTime - System.currentTimeMillis());
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
        endTime = System.currentTimeMillis() + timeRemaining;
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

        for (int i = bullets.size - 1; i >= 0; --i) {
            ClientBullet bullet = bullets.get(i);

            if (!bullet.update(dt)) {
                bullets.removeIndex(i);
                bulletPool.free(bullet);
            }
        }

        for (int i = lasers.size - 1; i >= 0; --i) {
            ClientLaser laser = lasers.get(i);

            if (!laser.update(dt)) {
                lasers.removeIndex(i);
                laserPool.free(laser);
            }
        }
    }

    public void end() {
        if (bullets.notEmpty()) {
            bulletPool.freeAll(bullets);
            bullets.removeRange(0, bullets.size - 1);
        }

        if (lasers.notEmpty()) {
            laserPool.freeAll(lasers);
            lasers.removeRange(0, lasers.size - 1);
        }
    }

    public void addBullet(float x, float y, float dirX, float dirY, BulletDefinition bullet, int index, @Nullable ClientCharacter character) {
        ClientBullet poolBullet = bulletPool.obtain();
        poolBullet.setDefinition(bullet);
        poolBullet.setStartPos(x, y);
        poolBullet.setPos(x, y);
        poolBullet.setAngleOffset(bullet.angleOffset());
        poolBullet.setSpeed(bullet.speed());
        poolBullet.setDirection(dirX, dirY);
        poolBullet.setLifetime(bullet.lifetime());
        poolBullet.setIndex(index);
        poolBullet.setOrbitPhase(MathUtils.PI2 * index / 5f);
        poolBullet.setAttached(character);
        bullets.add(poolBullet);
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
