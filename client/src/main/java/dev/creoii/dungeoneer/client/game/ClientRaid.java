package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.screen.game.GameScreen;
import dev.creoii.dungeoneer.client.render.screen.game.RaidEndScreen;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.RemovalReason;

import java.time.Duration;
import java.util.Iterator;

public class ClientRaid extends Raid<ClientBullet, ClientBulletGroup, ClientCharacter, ClientDungeonMap> {
    private final Dungeoneer client;

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
        super(raid, new ClientDungeonMap(client));
        this.client = client;
    }

    @Override
    public Pool<ClientBullet> getBulletPool() {
        return bulletPool;
    }

    @Override
    public Pool<ClientBulletGroup> getBulletGroupPool() {
        return bulletGroupPool;
    }

    @Override
    public void addCharacter(long accountId, ClientCharacter character) {
        super.addCharacter(accountId, character);
    }

    @Override
    public ClientCharacter removeCharacter(long accountId, RemovalReason reason) {
        ClientCharacter character = super.removeCharacter(accountId, reason);
        if (character != null) {
            Gdx.app.postRunnable(() -> {
                if (client.getScreen() instanceof GameScreen gameScreen) {
                    gameScreen.refreshVisibleCharacters();
                }
            });
        }
        return character;
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
                    client.getState().getCurrentRaid().setStatus(Raid.Status.END);
                    client.getState().setStatus(ClientState.Status.RAID_END);
                    end();
                    Gdx.app.postRunnable(() -> client.setScreen(new RaidEndScreen(client)));
                } else gameScreen.getTimeRemainingLabel().setText(getRemainingTimeString());
            }

            super.update(dt);

            Iterator<ClientCharacter> iterator = getCharacters().values().iterator();
            while (iterator.hasNext()) {
                ClientCharacter character = iterator.next();
                if (character.isDead()) {
                    iterator.remove();
                    continue;
                }
                character.tick(dt);
            }
        }
    }

    @Override
    public void end() {
        super.end();

        ClientCharacter character = client.getState().getActiveCharacter();
        character.setPos(0f, 0f);
        character.setRenderPos(0f, 0f);
        character.setDead(false);
        character.getStats().set(character.getMaxStats());
        character.clearStatusEffects();
    }
}
