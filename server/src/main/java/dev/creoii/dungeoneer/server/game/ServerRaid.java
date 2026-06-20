package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.network.s2c.raid.MoveRaidCharactersS2C;
import dev.creoii.dungeoneer.network.s2c.raid.SyncRaidTimerS2C;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Tickable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ServerRaid extends Raid<Bullet, BulletGroup, ServerCharacter> implements Tickable {
    private static final float SYNC_INTERVAL = 5f; // 5 seconds
    private final long id;
    private final DungeoneerServer server;
    private final ServerDungeon dungeon;
    private Status status;
    private float timer;
    private final List<MoveRaidCharactersS2C.Entry> moveEntries;

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

    public ServerRaid(long id, DungeoneerServer server, ServerDungeon dungeon, ServerCharacter character, RaidDefinition raid) {
        super(raid);
        this.id = id;
        this.server = server;
        this.dungeon = dungeon;
        getCharacters().put(character.get().accountId(), character);
        status = Status.WAITING;
        timer = SYNC_INTERVAL;
        setEndTime(System.currentTimeMillis() + Constants.RAID_DURATION_MS);
        moveEntries = new ArrayList<>();
    }

    @Override
    public Pool<Bullet> getBulletPool() {
        return bulletPool;
    }

    @Override
    public Pool<BulletGroup> getBulletGroupPool() {
        return bulletGroupPool;
    }

    public long getId() {
        return id;
    }

    public DungeoneerServer getServer() {
        return server;
    }

    public ServerDungeon getDungeon() {
        return dungeon;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<MoveRaidCharactersS2C.Entry> getMoveEntries() {
        return moveEntries;
    }

    @Override
    public void tick(float dt) {
        if (status == Status.ACTIVE) {
            timer -= dt;

            // Update bullet positions
            super.update(dt);

            for (ServerCharacter character : getCharacters().values()) {
                // Sync raid timer
                if (timer <= 0f) {
                    timer += SYNC_INTERVAL;
                    server.get().sendToUDP(server.getSessionManager().getAccountConnections().get(character.get().accountId()), new SyncRaidTimerS2C(getRemainingTimeMs()));
                }
                character.tick(this, dt);
            }

            if (!moveEntries.isEmpty()) {
                getCharacters().values().forEach(serverCharacter -> {
                    server.get().sendToUDP(serverCharacter.getConnectionId(), new MoveRaidCharactersS2C(moveEntries));
                });

                moveEntries.clear();
            }
        } else if (status == Status.WAITING && getCharacters().size() == get().requiredCharacters()) {
            setStatus(Status.ACTIVE);
        }
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

    public enum Status {
        WAITING,
        ACTIVE
    }
}
