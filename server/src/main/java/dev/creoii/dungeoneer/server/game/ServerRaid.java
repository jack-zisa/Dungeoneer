package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.network.s2c.character.CharacterMoveS2C;
import dev.creoii.dungeoneer.network.s2c.raid.AttacksS2C;
import dev.creoii.dungeoneer.network.s2c.raid.MoveRaidCharactersS2C;
import dev.creoii.dungeoneer.network.s2c.raid.SyncRaidTimerS2C;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Tickable;
import dev.creoii.dungeoneer.util.collision.EntityCollisionManager;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerRaid extends Raid<ServerBullet, BulletGroup, ServerCharacter, ServerDungeonMap> implements Tickable {
    private static final float SYNC_INTERVAL = 5f; // 5 seconds
    private final DungeoneerServer server;
    private final EntityCollisionManager entityCollisionManager;
    private float timer;
    private final List<MoveRaidCharactersS2C.Entry> moveEntries;
    private final List<AttacksS2C.Entry> attacks;

    private final Pool<ServerBullet> bulletPool = new Pool<>() {
        @Override
        protected ServerBullet newObject() {
            return new ServerBullet(ServerRaid.this);
        }
    };
    private final Pool<BulletGroup> bulletGroupPool = new Pool<>() {
        @Override
        protected BulletGroup newObject() {
            return new BulletGroup();
        }
    };

    public ServerRaid(DungeoneerServer server, ServerDungeonMap dungeon, ServerCharacter character, RaidDefinition raid) {
        super(raid, dungeon);
        this.server = server;
        entityCollisionManager = new EntityCollisionManager(this);
        getCharacters().put(character.get().accountId(), character);
        character.setPos(dungeon.getTemplate().spawnPos().x * 8f, dungeon.getTemplate().spawnPos().y * 8f);
        timer = SYNC_INTERVAL;
        moveEntries = new ArrayList<>();
        attacks = new ArrayList<>();
    }

    @Override
    public Pool<ServerBullet> getBulletPool() {
        return bulletPool;
    }

    @Override
    public Pool<BulletGroup> getBulletGroupPool() {
        return bulletGroupPool;
    }

    public DungeoneerServer getServer() {
        return server;
    }

    public List<MoveRaidCharactersS2C.Entry> getMoveEntries() {
        return moveEntries;
    }

    public List<AttacksS2C.Entry> getAttacks() {
        return attacks;
    }

    @Override
    public void addCharacter(long accountId, ServerCharacter character) {
        super.addCharacter(accountId, character);
    }

    @Override
    public void tick(float dt) {
        if (getStatus() == Status.ACTIVE) {
            if (getRemainingTimeMs() <= 0L) {
                end();
                return;
            }

            timer -= dt;

            entityCollisionManager.update();

            // Update bullet positions
            super.update(dt);

            Iterator<ServerCharacter> iterator = getCharacters().values().iterator();
            while (iterator.hasNext()) {
                ServerCharacter character = iterator.next();
                if (character.isDead()) {
                    iterator.remove();
                    continue;
                }

                // Sync raid timer
                if (timer <= 0f) {
                    timer += SYNC_INTERVAL;
                    server.get().sendToUDP(server.getSessionManager().getAccountConnections().get(character.get().accountId()), new SyncRaidTimerS2C(getRemainingTimeMs()));
                }
                character.tick(this, dt);
            }

            if (!moveEntries.isEmpty()) {
                getCharacters().values().forEach(serverCharacter -> server.get().sendToUDP(serverCharacter.getConnectionId(), new MoveRaidCharactersS2C(moveEntries)));
                moveEntries.clear();
            }

            if (!attacks.isEmpty()) {
                getCharacters().values().forEach(serverCharacter -> server.get().sendToUDP(serverCharacter.getConnectionId(), new AttacksS2C(attacks)));
                attacks.clear();
            }
        } else if (getStatus() == Status.WAITING && getCharacters().size() == get().requiredCharacters()) { // Start raid
            setStatus(Status.ACTIVE);
            setEndTime(System.currentTimeMillis() + Constants.RAID_DURATION_MS);
            updateSpawnPositions(getDungeonMap().getTemplate().spawnPos().x * 8f, getDungeonMap().getTemplate().spawnPos().y * 8f);
            getCharacters().values().forEach(serverCharacter -> {
                server.get().sendToUDP(serverCharacter.getConnectionId(), new CharacterMoveS2C(serverCharacter.get().id(), serverCharacter.getX(), serverCharacter.getY()));
            });
        }
    }

    @Override
    public void end() {
        super.end();

        moveEntries.clear();
        attacks.clear();
        timer = 0f;
        entityCollisionManager.getCollidables().clear();
    }

    @Nullable
    public ServerCharacter getCharacterByAccountId(long accountId) {
        for (ServerCharacter character : getCharacters().values()) {
            if (character.get().accountId() == accountId)
                return character;
        }
        return null;
    }

    @Nullable
    public ServerCharacter getCharacterById(long characterId) {
        for (ServerCharacter character : getCharacters().values()) {
            if (character.get().id() == characterId)
                return character;
        }
        return null;
    }
}
