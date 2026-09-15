package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.EntityManager;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.ui.screen.game.GameScreen;
import dev.creoii.dungeoneer.client.render.ui.screen.game.RaidEndScreen;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.RemovalReason;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClientRaid extends Raid<ClientBullet, ClientBulletGroup, ClientCharacter, ClientDungeonMap> {
    private final Dungeoneer client;
    private final ClientEntityManager entityManager;
    private List<Long> pendingPredictionIds;

    private final Pool<ClientBullet> bulletPool = new Pool<>() {
        @Override
        protected ClientBullet newObject() {
            return new ClientBullet(null);
        }
    };
    private final Pool<ClientBulletGroup> bulletGroupPool = new Pool<>() {
        @Override
        protected ClientBulletGroup newObject() {
            return new ClientBulletGroup(null);
        }
    };

    public ClientRaid(Dungeoneer client, RaidDefinition raid) {
        super(raid, new ClientDungeonMap(client));
        this.client = client;
        entityManager = new ClientEntityManager(this, 2);
    }

    @Override
    public ClientEntityManager getEntityManager() {
        return entityManager;
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

    public void beginAttackPrediction() {
        pendingPredictionIds = new ArrayList<>();
    }

    public List<Long> endAttackPrediction() {
        List<Long> ids = pendingPredictionIds;
        pendingPredictionIds = null;
        return ids;
    }

    private long nextPredictionId() {
        long id = getEntityManager().nextPredictionId();
        pendingPredictionIds.add(id);
        return id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BulletNode<?, ?> addBullet(int damage, float x, float y, float dirX, float dirY, BulletType bullet, int index, boolean enemy) {
        BulletNode<?, ?> bulletNode = super.addBullet(damage, x, y, dirX, dirY, bullet, index, enemy);
        if (!enemy && pendingPredictionIds != null) {
            long predictionId = nextPredictionId();
            ((ClientEntity) bulletNode).setClientId(predictionId);
            getEntityManager().addPredicted((Entity<ClientRaid>) bulletNode);
        } else {
            getEntityManager().add((Entity<ClientRaid>) bulletNode);
        }
        return bulletNode;
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
            long remaining = getRemainingTimeMs();
            if (remaining <= 0L) {
                client.getState().getCurrentRaid().setStatus(Raid.Status.END);
                client.getState().setStatus(ClientState.Status.RAID_END);
                end();
                Gdx.app.postRunnable(() -> client.setScreen(new RaidEndScreen(client)));
            } else if (client.getScreen() instanceof GameScreen gameScreen) {
                gameScreen.getTimeRemainingLabel().setText(getRemainingTimeString());
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

            entityManager.tick(dt);
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
