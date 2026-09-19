package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.EntityManager;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.network.data.BulletPacketData;
import dev.creoii.dungeoneer.network.s2c.character.KillCharacterS2C;
import dev.creoii.dungeoneer.network.s2c.raid.*;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.server.database.repository.CharacterRepository;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.RemovalReason;
import dev.creoii.dungeoneer.util.collision.EntityCollisionManager;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ServerRaid extends Raid<ServerBullet, ServerBulletGroup, ServerCharacter, ServerDungeonMap> {
    private static final float SYNC_INTERVAL = 5f; // 5 seconds
    private final DungeoneerServer server;
    private final EntityManager<ServerRaid> entityManager;
    private final EntityCollisionManager entityCollisionManager;
    private float timer;
    private final List<MoveCharactersS2C.Entry> moveEntries;
    private final List<StatusEffectsS2C.Entry> statusEffectEntries;
    private final List<AttacksS2C.Entry> attackEntries;
    private final List<DamageCharactersS2C.Entry> damageEntries;
    private final List<MoveEntitiesS2C.Entry> moveEntityEntries;
    private final List<AddEntitiesS2C.Entry> addEntityEntries;
    private final List<Long> removeEntityEntries;

    private final Pool<ServerBullet> bulletPool = new Pool<>() {
        @Override
        protected ServerBullet newObject() {
            return new ServerBullet(ServerRaid.this);
        }
    };
    private final Pool<ServerBulletGroup> bulletGroupPool = new Pool<>() {
        @Override
        protected ServerBulletGroup newObject() {
            return new ServerBulletGroup(null);
        }
    };

    public ServerRaid(DungeoneerServer server, ServerDungeonMap dungeon, ServerCharacter character, RaidDefinition raid) {
        super(raid, dungeon);
        this.server = server;
        entityManager = new EntityManager<>(this, 2);
        entityCollisionManager = new EntityCollisionManager(this);
        addCharacter(character.get().accountId(), character);
        character.setPos(dungeon.getTemplate().spawnPos().x * 8f, dungeon.getTemplate().spawnPos().y * 8f);
        timer = SYNC_INTERVAL;
        moveEntries = new ArrayList<>();
        statusEffectEntries = new ArrayList<>();
        attackEntries = new ArrayList<>();
        damageEntries = new ArrayList<>();
        moveEntityEntries = new ArrayList<>();
        addEntityEntries = new ArrayList<>();
        removeEntityEntries = new ArrayList<>();
        set(raid);
    }

    @Override
    public Pool<ServerBullet> getBulletPool() {
        return bulletPool;
    }

    @Override
    public Pool<ServerBulletGroup> getBulletGroupPool() {
        return bulletGroupPool;
    }

    public DungeoneerServer getServer() {
        return server;
    }

    public EntityManager<ServerRaid> getEntityManager() {
        return entityManager;
    }

    public List<MoveCharactersS2C.Entry> getMoveEntries() {
        return moveEntries;
    }

    public List<MoveEntitiesS2C.Entry> getMoveEntityEntries() {
        return moveEntityEntries;
    }

    public List<AddEntitiesS2C.Entry> getAddEntityEntries() {
        return addEntityEntries;
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

    public List<Long> getRemoveEntityEntries() {
        return removeEntityEntries;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BulletNode<?, ServerRaid> addBullet(int damage, float x, float y, float dirX, float dirY, BulletType bullet, int index, boolean enemy) {
        BulletNode<?, ServerRaid> bulletNode = (BulletNode<?, ServerRaid>) super.addBullet(damage, x, y, dirX, dirY, bullet, index, enemy);
        bulletNode.setRaid(this);
        entityManager.add(bulletNode);
        getAddEntityEntries().add(new AddEntitiesS2C.Entry(bulletNode.id(), x, y, new BulletPacketData(damage, dirX, dirY, bullet.id(), index, enemy)));
        return bulletNode;
    }

    @Override
    public void tick(float dt) {
        if (getStatus() == Status.ACTIVE) {
            if (getRemainingTimeMs() <= 0L || getCharacters().isEmpty()) {
                end();
                return;
            }

            timer -= dt;

            if (getRaidTime() % 2 == 0) { // TODO: Remove as this is just testing
                Vector2 spawnPos = getDungeonMap().getTemplate().spawnPos();
                addBullet(10, spawnPos.x * 8f, spawnPos.y * 8f, MathUtils.cos(getRaidTime()) * .01f, MathUtils.sin(getRaidTime()) * .01f, DataManager.getBullet("fireball"), 0, true);
            }

            // Update bullet positions
            super.tick(dt);

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

            entityCollisionManager.update(); // Update collision after bullets & characters have moved

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

            if (!moveEntityEntries.isEmpty()) {
                MoveEntitiesS2C packet = new MoveEntitiesS2C(List.copyOf(moveEntityEntries));
                getCharacters().values().forEach(serverCharacter -> server.get().sendToUDP(serverCharacter.getConnectionId(), packet));
                moveEntityEntries.clear();
            }

            if (!addEntityEntries.isEmpty()) {
                AddEntitiesS2C packet = new AddEntitiesS2C(List.copyOf(addEntityEntries));
                getCharacters().values().forEach(serverCharacter -> server.get().sendToUDP(serverCharacter.getConnectionId(), packet));
                addEntityEntries.clear();
            }

            if (!removeEntityEntries.isEmpty()) {
                RemoveEntitiesS2C packet = new RemoveEntitiesS2C(List.copyOf(removeEntityEntries));
                getCharacters().values().forEach(serverCharacter -> server.get().sendToUDP(serverCharacter.getConnectionId(), packet));
                removeEntityEntries.clear();
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
        moveEntityEntries.clear();
        addEntityEntries.clear();
        removeEntityEntries.clear();
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
