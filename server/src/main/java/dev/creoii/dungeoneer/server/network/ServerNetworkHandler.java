package dev.creoii.dungeoneer.server.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.password4j.Password;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.attack.Attack;
import dev.creoii.dungeoneer.network.NetworkQueue;
import dev.creoii.dungeoneer.network.PacketResult;
import dev.creoii.dungeoneer.network.PacketSerializer;
import dev.creoii.dungeoneer.network.c2s.dungeon.RequestDungeonMapC2S;
import dev.creoii.dungeoneer.network.c2s.dungeon.SaveDungeonMapC2S;
import dev.creoii.dungeoneer.network.c2s.character.*;
import dev.creoii.dungeoneer.network.c2s.faction.*;
import dev.creoii.dungeoneer.network.c2s.raid.AttackC2S;
import dev.creoii.dungeoneer.network.c2s.raid.CancelJoinRaidC2S;
import dev.creoii.dungeoneer.network.s2c.LoadDataS2C;
import dev.creoii.dungeoneer.network.s2c.SyncDataS2C;
import dev.creoii.dungeoneer.network.s2c.dungeon.SendDungeonMapS2C;
import dev.creoii.dungeoneer.network.s2c.faction.*;
import dev.creoii.dungeoneer.network.s2c.raid.AttackResultS2C;
import dev.creoii.dungeoneer.network.s2c.raid.RaidCharacterWaitStatusS2C;
import dev.creoii.dungeoneer.network.s2c.raid.SyncRaidWaitingStateS2C;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.server.database.Database;
import dev.creoii.dungeoneer.definitions.*;
import dev.creoii.dungeoneer.server.database.definitions.ClientSession;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.c2s.raid.EndRaidC2S;
import dev.creoii.dungeoneer.network.c2s.raid.JoinOrCreateRaidC2S;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;
import dev.creoii.dungeoneer.network.s2c.character.CreateCharacterResultS2C;
import dev.creoii.dungeoneer.network.s2c.character.SendCharactersS2C;
import dev.creoii.dungeoneer.network.s2c.character.SendFactionS2C;
import dev.creoii.dungeoneer.network.s2c.raid.SendRaidTargetS2C;
import dev.creoii.dungeoneer.server.game.ServerCharacter;
import dev.creoii.dungeoneer.server.game.ServerDungeon;
import dev.creoii.dungeoneer.server.game.ServerRaid;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Tickable;
import dev.creoii.dungeoneer.util.stat.StatUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServerNetworkHandler implements Listener, Tickable {
    private final DungeoneerServer server;
    private final NetworkQueue networkQueue;

    public ServerNetworkHandler(DungeoneerServer server) {
        this.server = server;
        networkQueue = new NetworkQueue();
        server.get().addListener(this);

        PacketSerializer.registerDefault(server.get().getKryo());
    }

    @Override
    public void tick(float dt) {
        NetworkQueue.QueuedPacket packet;
        while ((packet = networkQueue.queue().poll()) != null && PacketSerializer.INSTANCE.isValidPacket(packet.data())) {
            handlePacket(packet.connection(), packet.data());
        }
    }

    @Override
    public void connected(Connection connection) {
        DungeoneerServer.LOGGER.info("Client connected: %s", connection);

        if (server.getStatus() == DungeoneerServer.Status.PAUSED && !server.get().getConnections().isEmpty()) {
            server.setStatus(DungeoneerServer.Status.RUNNING);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource("dungeoneer/data/");
        if (url == null)
            throw new IllegalStateException("Could not find data folder");
        Path dataRoot;
        try {
            dataRoot = Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        for (DataManager.SchemaType schemaType : DataManager.SchemaType.values()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(32768);
            try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
                Path schemaRoot = dataRoot.resolve(schemaType.getPath());
                try (Stream<Path> paths = Files.walk(schemaRoot)) {
                    for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                        ZipEntry entry = new ZipEntry(dataRoot.relativize(path).toString().replace("\\", "/"));
                        zipOut.putNextEntry(entry);
                        Files.copy(path, zipOut);
                        zipOut.closeEntry();
                    }
                }
                zipOut.finish();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            server.get().sendToTCP(connection.getID(), new SyncDataS2C(schemaType.getPath(), baos.toByteArray()));
        }

        server.get().sendToTCP(connection.getID(), new LoadDataS2C());
    }

    @Override
    public void disconnected(Connection connection) {
        DungeoneerServer.LOGGER.info("Client disconnected: %s", connection);

        server.getSessionManager().endClientSession(connection);

        if (server.getStatus() == DungeoneerServer.Status.RUNNING && server.get().getConnections().isEmpty()) {
            server.setStatus(DungeoneerServer.Status.PAUSED);
        }
    }

    @Override
    public void received(Connection connection, Object object) {
        networkQueue.queuePacket(connection, object);
    }

    public void handlePacket(Connection connection, Object object) {
        if (server.isDebug())
            DungeoneerServer.LOGGER.debug("%s | Connection %s | %s", connection.getRemoteAddressTCP(), connection.getID(), object.getClass().getSimpleName());

        if (object instanceof RequestLoginC2S) {
            server.get().sendToUDP(connection.getID(), new AuthenticateS2C());
        } else if (object instanceof LoginC2S(String username, String password)) {
            Account account = server.getDatabase().getAccounts().getByUsername(username);
            if (account == null) {
                // TODO: Implement password requirements
                account = server.getDatabase().getAccounts().create(username, Password.hash(password + server.getSecrets().pepper()).withArgon2().getResult(), LocalDateTime.now());
                Database.LOGGER.info("Created account: %s", account.username());
            } else if (Password.check(password + server.getSecrets().pepper(), account.passwordHash()).withArgon2()) {
                Database.LOGGER.info("Loaded account: %s", account.username());
                server.getDatabase().getAccounts().updateLastLoginTime(account.id(), LocalDateTime.now());
                account = server.getDatabase().getAccounts().getByUsername(username);
            } else {
                DungeoneerServer.LOGGER.error("Failed login for account: %s", account.username());
                server.get().sendToUDP(connection.getID(), new LoginResultS2C(PacketResult.FAIL, null));
                return;
            }

            if (account != null) {
                ClientSession clientSession = server.getSessionManager().startClientSession(connection, account.id());
                if (clientSession != null) {
                    server.get().sendToUDP(connection.getID(), new LoginResultS2C(PacketResult.SUCCESS, account));
                } else {
                    connection.close();
                    server.get().sendToUDP(connection.getID(), new LoginResultS2C(PacketResult.FAIL, null));
                }
            }
        } else if (object instanceof RequestCharactersC2S(long accountId)) {
            Account account = server.getDatabase().getAccounts().getById(accountId);
            if (account == null) return;

            List<CharacterDefinition> characters = account.characters().stream().map(integer -> integer == -1L ? null : server.getDatabase().getCharacters().getById(integer)).toList();
            server.get().sendToUDP(connection.getID(), new SendCharactersS2C(characters));
        } else if (object instanceof RequestFactionC2S(long accountId)) {
            Account account = server.getDatabase().getAccounts().getById(accountId);
            if (account == null) {
                server.get().sendToUDP(connection.getID(), new SendFactionS2C(null));
                return;
            }

            Faction faction = server.getDatabase().getFactions().getById(account.factionId());
            if (faction != null) server.get().sendToUDP(connection.getID(), new SendFactionS2C(faction));
        } else if (object instanceof CreateCharacterC2S(long accountId, int index, CharacterClass characterClass)) {
            Account account = server.getDatabase().getAccounts().getById(accountId);
            if (account != null) {
                CharacterDefinition character = server.getDatabase().getCharacters().create(account, characterClass);
                if (character != null) {
                    account.characters().set(index, character.id());
                    server.getDatabase().getAccounts().updateCharacters(account);

                    DungeoneerServer.LOGGER.info("Created character of class '%s' for account: %s", characterClass.id(), account.id());
                    server.get().sendToUDP(connection.getID(), new CreateCharacterResultS2C(PacketResult.SUCCESS, index, character));
                    return;
                }
            }
            server.get().sendToUDP(connection.getID(), new CreateCharacterResultS2C(PacketResult.FAIL, -1, null));
        } else if (object instanceof CreateFactionC2S(Account account, String name, String description)) {
            Faction faction = server.getDatabase().getFactions().create(account.id(), name, description);
            if (faction != null) {
                server.getDatabase().getAccounts().updateFaction(account, faction.id());
                server.getDatabase().getFactions().updateAccounts(faction);
                server.get().sendToUDP(connection.getID(), new CreateFactionResultS2C(PacketResult.SUCCESS, faction));
                return;
            }
            server.get().sendToUDP(connection.getID(), new CreateFactionResultS2C(PacketResult.FAIL, null));
        } else if (object instanceof JoinFactionC2S(Account account, long factionId)) {
            Faction faction = server.getDatabase().getFactions().getById(factionId);
            if (faction != null) {
                faction.accounts().add(account);
                server.getDatabase().getAccounts().updateFaction(account, faction.id());
                server.getDatabase().getFactions().updateAccounts(faction);

                faction.accounts().forEach(account1 -> {
                    if (account1.id() != account.id() && server.getSessionManager().getAccountConnections().containsKey(account1.id())) {
                        server.get().sendToUDP(server.getSessionManager().getAccountConnections().get(account1.id()), new SendFactionS2C(faction));
                    }
                });

                server.get().sendToUDP(connection.getID(), new JoinFactionResultS2C(PacketResult.SUCCESS, faction));
                return;
            }
            server.get().sendToUDP(connection.getID(), new JoinFactionResultS2C(PacketResult.FAIL, null));
        } else if (object instanceof LeaveFactionC2S(Account account)) {
            if (account.factionId() != -1L) {
                Faction faction = server.getDatabase().getFactions().getById(account.factionId());
                if (faction != null) {
                    faction.accounts().remove(account.id());
                    server.getDatabase().getAccounts().updateFaction(account, -1L);
                    server.getDatabase().getFactions().updateAccounts(faction);

                    faction.accounts().forEach(account1 -> {
                        if (account1.id() != account.id() && server.getSessionManager().getAccountConnections().containsKey(account1.id())) {
                            server.get().sendToUDP(server.getSessionManager().getAccountConnections().get(account1.id()), new SendFactionS2C(faction));
                        }
                    });

                    server.get().sendToUDP(connection.getID(), new LeaveFactionResultS2C(PacketResult.SUCCESS));
                    return;
                }
            }
            server.get().sendToUDP(connection.getID(), new LeaveFactionResultS2C(PacketResult.FAIL));
        } else if (object instanceof JoinOrCreateRaidC2S(Account account, CharacterDefinition character, int requiredCharacters)) {
            List<RaidDefinition> availableRaids = server.getDatabase().getRaids().getAvailableRaids(requiredCharacters);
            Collections.shuffle(availableRaids);
            if (!availableRaids.isEmpty()) { // Join an existing raid
                RaidDefinition raidDefinition = availableRaids.getFirst();
                DungeonMap dungeonMap = server.getDatabase().getDungeonMaps().getByAccountId(raidDefinition.target().id());
                if (dungeonMap != null) {
                    ServerCharacter serverCharacter = new ServerCharacter(connection.getID(), character);
                    ServerRaid serverRaid = server.getState().getRaids().get(raidDefinition.id());
                    raidDefinition = serverRaid.get();
                    raidDefinition.attackers().add(account);
                    serverRaid.addCharacter(account.id(), serverCharacter);
                    server.getDatabase().getRaids().updateAttackers(raidDefinition);
                    server.get().sendToTCP(connection.getID(), new SendRaidTargetS2C(raidDefinition, dungeonMap.mapData()));

                    for (ServerCharacter existing : serverRaid.getCharacters().values()) {
                        if (existing.get().accountId() == account.id())
                            continue;
                        server.get().sendToTCP(connection.getID(), new RaidCharacterWaitStatusS2C(existing.get(), existing.get().accountId()));
                    }

                    raidDefinition.attackers().forEach(account1 -> {
                        int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(account1.id(), -1);
                        if (connectionId != -1) {
                            server.get().sendToTCP(connectionId, new SyncRaidWaitingStateS2C(serverRaid.get()));
                            server.get().sendToTCP(connectionId, new RaidCharacterWaitStatusS2C(character, account.id()));
                        }
                    });
                }
            } else { // Create a new raid
                Account target = server.getDatabase().getAccounts().getRandomExcluding(account.id());
                if (target != null) {
                    DungeonMap dungeonMap = server.getDatabase().getDungeonMaps().getByAccountId(target.id());
                    if (dungeonMap != null) {
                        RaidDefinition raid = server.getDatabase().getRaids().create(account, target, requiredCharacters, LocalDateTime.now());
                        if (raid != null) {
                            ServerCharacter serverCharacter = new ServerCharacter(connection.getID(), character);
                            server.get().sendToTCP(connection.getID(), new SendRaidTargetS2C(raid, dungeonMap.mapData()));
                            server.getState().getRaids().put(raid.id(), new ServerRaid(raid.id(), server, new ServerDungeon(dungeonMap.mapData()), serverCharacter, raid));
                        }
                    }
                }
            }
        } else if (object instanceof EndRaidC2S(long raidId)) {
            RaidDefinition raid = server.getDatabase().getRaids().getById(raidId);
            if (raid != null) {
                server.getState().getRaids().remove(raidId);
                server.getDatabase().getRaids().updateEndTime(raidId, LocalDateTime.now());
            }
        } else if (object instanceof DeleteCharacterC2S(long accountId, int index)) {
            Account account = server.getDatabase().getAccounts().getById(accountId);
            if (account != null) {
                account.characters().set(index, -1L);
                server.getDatabase().getAccounts().updateCharacters(account);
                List<CharacterDefinition> characters = account.characters().stream().map(integer -> integer == -1L ? null : server.getDatabase().getCharacters().getById(integer)).toList();
                server.get().sendToUDP(connection.getID(), new SendCharactersS2C(characters));
            }
        } else if (object instanceof SearchFactionC2S(String search)) {
            List<Faction> factions = server.getDatabase().getFactions().search(search);
            if (!factions.isEmpty()) {
                server.get().sendToUDP(connection.getID(), new SearchFactionResultS2C(PacketResult.SUCCESS, factions));
                return;
            }
            server.get().sendToUDP(connection.getID(), new SearchFactionResultS2C(PacketResult.FAIL, factions));
        } else if (object instanceof SelectActiveCharacterC2S(long accountId, long activeCharacterId)) {
            Account account = server.getDatabase().getAccounts().getById(accountId);
            if (account != null) {
                server.getDatabase().getAccounts().updateActiveCharacter(accountId, activeCharacterId);
            }
        } else if (object instanceof CharacterMoveC2S(long raidId, long characterId, int movementFlags)) {
            CharacterDefinition character = server.getDatabase().getCharacters().getById(characterId);
            if (character != null && server.getState().getRaids().containsKey(raidId)) {
                ServerRaid raid = server.getState().getRaids().get(raidId);
                ServerCharacter serverCharacter = raid.getCharacterById(characterId);
                if (serverCharacter == null)
                    return;
                serverCharacter.updateVelocity(serverCharacter.getVelocity(), movementFlags);
            }
        } else if (object instanceof ChatMessageC2S(long localId, Message message)) {
            Faction faction = server.getDatabase().getFactions().getById(message.factionId());
            if (faction == null)
                return;

            Message stored = server.getDatabase().getChatMessages().create(message);

            boolean flagged = "bad".equalsIgnoreCase(stored.text());

            Message finalMessage = flagged ? new Message(stored.messageId(), stored.factionId(), stored.accountId(), "*****", true) : stored;
            if (flagged) {
                server.getDatabase().getChatMessages().setFlagged(stored.messageId());
            }

            faction.recentMessages().put(finalMessage.messageId(), finalMessage);
            if (flagged) {
                server.get().sendToTCP(connection.getID(), new FlagChatMessageS2C(localId, finalMessage));
            }

            faction.accounts().forEach(account -> {
                if (account.id() == finalMessage.accountId())
                    return;

                int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(account.id(), -1);
                if (connectionId != -1) {
                    server.get().sendToTCP(connectionId, new ChatMessageS2C(finalMessage));
                }
            });
        } else if (object instanceof SaveDungeonMapC2S(long accountId, byte[] mapData)) {
            Account account = server.getDatabase().getAccounts().getById(accountId);
            DungeonMap dungeonMap = server.getDatabase().getDungeonMaps().getByAccountId(accountId);
            if (account != null && dungeonMap != null) {
                server.getDatabase().getDungeonMaps().updateLastEditDate(accountId, LocalDateTime.now());
                server.getDatabase().getDungeonMaps().updateDungeonMap(accountId, mapData);
                dungeonMap = new DungeonMap(dungeonMap.id(), accountId, mapData, LocalDateTime.now());
            } else {
                dungeonMap = server.getDatabase().getDungeonMaps().create(accountId, mapData, LocalDateTime.now());
            }

            if (dungeonMap != null) {
                server.get().sendToTCP(connection.getID(), new SendDungeonMapS2C(dungeonMap));
            }
        } else if (object instanceof RequestDungeonMapC2S(long accountId)) {
            DungeonMap dungeonMap = server.getDatabase().getDungeonMaps().getByAccountId(accountId);
            if (dungeonMap != null) {
                server.get().sendToTCP(connection.getID(), new SendDungeonMapS2C(dungeonMap));
            }
        } else if (object instanceof AttackC2S(long raidId, long accountId, float mouseDirX, float mouseDirY)) {
            Account account = server.getDatabase().getAccounts().getById(accountId);
            if (account != null) {
                ServerRaid serverRaid = server.getState().getRaids().get(raidId);
                if (serverRaid != null) {
                    ServerCharacter character = serverRaid.getCharacterByAccountId(accountId);
                    if (character != null) {
                        long currentTime = System.currentTimeMillis();
                        long lastAttackTime = character.getLastAttackTime();
                        long cooldown = (long) StatUtils.getCalculatedAttackSpeed(character.getStats().attackSpeed().value());
                        if (currentTime - lastAttackTime >= cooldown) {
                            Attack attack = DataManager.getAttack(Constants.TEST_ATTACK);

                            character.attack(attack, serverRaid, new float[]{mouseDirX, mouseDirY});

                            character.setLastAttackTime(currentTime);
                            server.get().sendToTCP(connection.getID(), new AttackResultS2C(PacketResult.SUCCESS));
                        } else {
                            server.get().sendToTCP(connection.getID(), new AttackResultS2C(PacketResult.FAIL));
                        }
                    }
                }
            }
        } else if (object instanceof CancelJoinRaidC2S(long accountId, long raidId)) {
            ServerRaid serverRaid = server.getState().getRaids().get(raidId);
            if (serverRaid != null && serverRaid.getStatus() == ServerRaid.Status.WAITING) {
                serverRaid.get().attackers().removeIf(account -> account.id() == accountId);

                if (serverRaid.get().attackers().isEmpty()) {
                    server.getDatabase().getRaids().delete(raidId);
                } else server.getDatabase().getRaids().updateAttackers(serverRaid.get());

                serverRaid.get().attackers().forEach(account1 -> {
                    int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(account1.id(), -1);
                    if (connectionId != -1) {
                        server.get().sendToTCP(connectionId, new SyncRaidWaitingStateS2C(serverRaid.get()));
                        server.get().sendToTCP(connectionId, new RaidCharacterWaitStatusS2C(null, accountId));
                    }
                });
            }
        }
    }
}
