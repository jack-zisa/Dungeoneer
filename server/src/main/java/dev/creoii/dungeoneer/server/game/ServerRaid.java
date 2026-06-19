package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.network.s2c.raid.SyncRaidTimerS2C;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Tickable;

public class ServerRaid extends Raid<Bullet, BulletGroup> implements Tickable {
    private static final float SYNC_INTERVAL = 5f; // 5 seconds
    private final long id;
    private final DungeoneerServer server;
    private final ServerDungeon dungeon;
    private final ServerCharacter character;
    private float timer;

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
        this.character = character;
        timer = SYNC_INTERVAL;
        setEndTime(System.currentTimeMillis() + Constants.RAID_DURATION_MS);
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

    public ServerDungeon getDungeon() {
        return dungeon;
    }

    public ServerCharacter getCharacter() {
        return character;
    }

    @Override
    public void tick(float dt) {
        timer -= dt;

        // Sync raid timer
        if (timer <= 0f) {
            timer += SYNC_INTERVAL;
            server.get().sendToUDP(server.getSessionManager().getAccountConnections().get(character.get().accountId()), new SyncRaidTimerS2C(getRemainingTimeMs()));
        }

        // Update bullet positions
        super.update(dt);

        character.tick(server, dt);
    }
}
