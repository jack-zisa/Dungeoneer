package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.network.s2c.character.KillCharacterS2C;
import dev.creoii.dungeoneer.network.s2c.raid.*;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.server.database.repository.CharacterRepository;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.RemovalReason;
import dev.creoii.dungeoneer.util.Tickable;
import dev.creoii.dungeoneer.util.collision.EntityCollisionManager;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ServerRaid extends Raid<ServerBullet, BulletGroup, ServerCharacter, ServerDungeonMap> implements Tickable {
    private static final float SYNC_INTERVAL = 5f; // 5 seconds
    private final DungeoneerServer server;
    private final EntityCollisionManager entityCollisionManager;
    private float timer;
    private final List<MoveCharactersS2C.Entry> moveEntries;
    private final List<StatusEffectsS2C.Entry> statusEffectEntries;
    private final List<AttacksS2C.Entry> attackEntries;
    private final List<DamageCharactersS2C.Entry> damageEntries;

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
        addCharacter(character.get().accountId(), character);
        character.setPos(dungeon.getTemplate().spawnPos().x * 8f, dungeon.getTemplate().spawnPos().y * 8f);
        timer = SYNC_INTERVAL;
        moveEntries = new ArrayList<>();
        statusEffectEntries = new ArrayList<>();
        attackEntries = new ArrayList<>();
        damageEntries = new ArrayList<>();
        set(raid);
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

    public List<MoveCharactersS2C.Entry> getMoveEntries() {
        return moveEntries;
    }

    public List<StatusEffectsS2C.Entry> getStatusEffectEntries() {
        return statusEffectEntries;
    }

    public List<AttacksS2C.Entry> getAttackEntries() {
        return attackEntries;
    }

    public List<DamageCharactersS2C.Entry> getDamageEntries() {
        return damageEntries;
    }

    @Override
    public void tick(float dt) {
        if (getStatus() == Status.ACTIVE) {
            if (getRemainingTimeMs() <= 0L || getCharacters().isEmpty()) {
                end();
                return;
            }

            timer -= dt;

            entityCollisionManager.update();

            // Update bullet positions
            super.update(dt);

            for (ServerCharacter character : getCharacters().values()) {
                if (character.isDead()) {
                    character.die();
                    continue;
                }

                // Sync raid timer
                if (timer <= 0f) {
                    timer += SYNC_INTERVAL;
                    server.get().sendToUDP(character.getConnectionId(), new SyncRaidTimerS2C(getRemainingTimeMs()));
                }
                character.tick(dt);
            }

            if (!moveEntries.isEmpty()) {
                MoveCharactersS2C packet = new MoveCharactersS2C(List.copyOf(moveEntries));
                getCharacters().values().forEach(serverCharacter -> server.get().sendToUDP(serverCharacter.getConnectionId(), packet));
                moveEntries.clear();
            }

            if (!statusEffectEntries.isEmpty()) {
                StatusEffectsS2C packet = new StatusEffectsS2C(List.copyOf(statusEffectEntries));
                getCharacters().values().forEach(serverCharacter -> server.get().sendToUDP(serverCharacter.getConnectionId(), packet));
                statusEffectEntries.clear();
            }

            if (!attackEntries.isEmpty()) {
                AttacksS2C packet = new AttacksS2C(List.copyOf(attackEntries));
                getCharacters().values().forEach(serverCharacter -> server.get().sendToUDP(serverCharacter.getConnectionId(), packet));
                attackEntries.clear();
            }

            if (!damageEntries.isEmpty()) {
                DamageCharactersS2C packet = new DamageCharactersS2C(List.copyOf(damageEntries));
                getCharacters().values().forEach(serverCharacter -> server.get().sendToUDP(serverCharacter.getConnectionId(), packet));
                damageEntries.clear();
            }
        } else if (getStatus() == Status.WAITING && getCharacters().size() == get().requiredCharacters()) { // Start raid
            setStatus(Status.ACTIVE);
            setEndTime(System.currentTimeMillis() + Constants.RAID_DURATION_MS);
            updateSpawnPositions(getDungeonMap().getTemplate().spawnPos().x * 8f, getDungeonMap().getTemplate().spawnPos().y * 8f);
        }
    }

    @Override
    public void end() {
        super.end();

        moveEntries.clear();
        statusEffectEntries.clear();
        attackEntries.clear();
        damageEntries.clear();
        timer = 0f;
        entityCollisionManager.getCollidables().clear();
    }

    @Override
    public ServerCharacter removeCharacter(long accountId, RemovalReason reason) {
        ServerCharacter removed = super.removeCharacter(accountId, reason);
        if (removed != null) {
            LeaveRaidS2C packet = new LeaveRaidS2C(get().id(), accountId, reason);
            server.get().sendToTCP(removed.getConnectionId(), packet);

            CharacterRepository characterRepository = server.getDatabase().getCharacters();
            CharacterDefinition killed = characterRepository.getById(removed.get().id());
            characterRepository.updateEquipment(accountId, removed.get().id(), removed.getEquipment());

            server.get().sendToTCP(removed.getConnectionId(), new KillCharacterS2C(killed));

            getCharacters().values().forEach(serverCharacter -> {
                server.get().sendToTCP(serverCharacter.getConnectionId(), packet);
            });
        }
        return removed;
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

    @Nullable
    public ServerCharacter getCharacterByConnectionId(int connectionId) {
        for (ServerCharacter character : getCharacters().values()) {
            if (character.getConnectionId() == connectionId)
                return character;
        }
        return null;
    }
}
