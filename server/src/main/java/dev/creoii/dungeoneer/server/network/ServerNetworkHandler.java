package dev.creoii.dungeoneer.server.network;

import com.esotericsoftware.kryonet.Connection;
import com.password4j.Password;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.attack.Attack;
import dev.creoii.dungeoneer.definitions.item.WeaponItem;
import dev.creoii.dungeoneer.definitions.item.inventory.Inventory;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.network.NetworkHandler;
import dev.creoii.dungeoneer.network.PacketResult;
import dev.creoii.dungeoneer.network.PacketSerializer;
import dev.creoii.dungeoneer.network.c2s.ExecuteCommandC2S;
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
import dev.creoii.dungeoneer.network.s2c.raid.*;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.server.command.Command;
import dev.creoii.dungeoneer.server.command.SimpleCommand;
import dev.creoii.dungeoneer.server.database.Database;
import dev.creoii.dungeoneer.definitions.*;
import dev.creoii.dungeoneer.server.database.definitions.ClientSession;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.c2s.raid.LeaveRaidC2S;
import dev.creoii.dungeoneer.network.c2s.raid.JoinOrCreateRaidC2S;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;
import dev.creoii.dungeoneer.network.s2c.character.CreateCharacterResultS2C;
import dev.creoii.dungeoneer.network.s2c.character.SendCharactersS2C;
import dev.creoii.dungeoneer.network.s2c.character.SendFactionS2C;
import dev.creoii.dungeoneer.server.database.repository.CharacterRepository;
import dev.creoii.dungeoneer.server.game.ServerCharacter;
import dev.creoii.dungeoneer.server.game.ServerDungeonMap;
import dev.creoii.dungeoneer.server.game.ServerRaid;
import dev.creoii.dungeoneer.util.RemovalReason;
import dev.creoii.dungeoneer.util.event.AttackEvents;
import dev.creoii.dungeoneer.util.stat.StatUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServerNetworkHandler extends NetworkHandler {
    private final DungeoneerServer server;

    public ServerNetworkHandler(DungeoneerServer server) {
        super();
        this.server = server;
        server.get().addListener(this);
        PacketSerializer.registerDefault(server.get().getKryo());
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

        if (server.getSessionManager().hasSession(connection.getID())) {
            long accountId = server.getSessionManager().getAccountConnections().inverse().get(connection.getID());
            ServerCharacter character = null;
            for (ServerRaid raid : server.getState().getRaids().values()) {
                if ((character = raid.removeCharacter(accountId, RemovalReason.DISCONNECTED)) != null)
                    break;
            }

            if (character != null) {
                CharacterRepository characterRepository = server.getDatabase().getCharacters();
                characterRepository.updateEquipment(accountId, character.get().id(), character.getEquipment());
            }

            server.getSessionManager().endClientSession(connection);

            if (server.getStatus() == DungeoneerServer.Status.RUNNING && server.get().getConnections().isEmpty()) {
                server.setStatus(DungeoneerServer.Status.PAUSED);
            }
        }
    }

    @Override
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
                CharacterDefinition character = server.getDatabase().getCharacters().create(account, characterClass, new Inventory(4));
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

                Message message = server.getDatabase().getChatMessages().create(account.factionId(), -1L, String.format("%s joined the faction", account.username()));
                faction.recentMessages().put(message.messageId(), message);

                SendFactionS2C sendFactionS2C = new SendFactionS2C(faction);
                ChatMessageS2C chatMessageS2C = new ChatMessageS2C(message);
                faction.accounts().forEach(account1 -> {
                    if (account1.id() != account.id() && server.getSessionManager().getAccountConnections().containsKey(account1.id())) {
                        int connectionId = server.getSessionManager().getAccountConnections().get(account1.id());
                        server.get().sendToUDP(connectionId, sendFactionS2C);
                        server.get().sendToTCP(connectionId, chatMessageS2C);
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

                    Message message = server.getDatabase().getChatMessages().create(account.factionId(), -1L, String.format("%s left the faction", account.username()));
                    faction.recentMessages().put(message.messageId(), message);

                    SendFactionS2C sendFactionS2C = new SendFactionS2C(faction);
                    ChatMessageS2C chatMessageS2C = new ChatMessageS2C(message);
                    faction.accounts().forEach(account1 -> {
                        if (account1.id() != account.id() && server.getSessionManager().getAccountConnections().containsKey(account1.id())) {
                            int connectionId = server.getSessionManager().getAccountConnections().get(account1.id());
                            server.get().sendToUDP(connectionId, sendFactionS2C);
                            server.get().sendToTCP(connectionId, chatMessageS2C);
                        }
                    });

                    server.get().sendToUDP(connection.getID(), new LeaveFactionResultS2C(PacketResult.SUCCESS));
                    return;
                }
            }
            server.get().sendToUDP(connection.getID(), new LeaveFactionResultS2C(PacketResult.FAIL));
        } else if (object instanceof JoinOrCreateRaidC2S(Account account, CharacterDefinition character, int requiredCharacters)) {
            ServerCharacter serverCharacter = new ServerCharacter(connection.getID(), character);

            List<ServerRaid> availableRaids = server.getState().getRaids().values().stream().filter(raid -> raid.getStatus() != ServerRaid.Status.ACTIVE && raid.get().requiredCharacters() == requiredCharacters && raid.get().attackers().size() < raid.get().requiredCharacters()).collect(Collectors.toList());
            if (!availableRaids.isEmpty()) { // Join an existing raid
                Collections.shuffle(availableRaids);
                ServerRaid serverRaid = availableRaids.getFirst();
                DungeonMapDefinition dungeonMap = server.getDatabase().getDungeonMaps().getByAccountId(serverRaid.get().target().id());
                if (dungeonMap != null) { // This should never be null, hopefully!
                    serverRaid.get().attackers().add(account);
                    serverRaid.get().characters().add(character);
                    serverRaid.addCharacter(account.id(), serverCharacter);
                    server.getDatabase().getRaids().updateAttackers(serverRaid.get());
                    server.get().sendToTCP(connection.getID(), new SendRaidTargetS2C(serverRaid.get(), dungeonMap.templateId(), dungeonMap.tilesetId()));

                    for (ServerCharacter existing : serverRaid.getCharacters().values()) {
                        if (existing.get().accountId() == account.id())
                            continue;
                        server.get().sendToTCP(connection.getID(), new JoinRaidS2C(existing.get()));
                    }

                    JoinRaidS2C joinRaidS2C = new JoinRaidS2C(character);
                    serverRaid.get().attackers().forEach(account1 -> {
                        int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(account1.id(), -1);
                        if (connectionId != -1) {
                            if (account1.id() == account.id())
                                return;
                            server.get().sendToTCP(connectionId, joinRaidS2C);
                        }
                    });

                    SyncRaidWaitingStateS2C syncRaidWaitingStateS2C = new SyncRaidWaitingStateS2C(serverRaid.get());
                    serverRaid.get().attackers().forEach(account1 -> {
                        int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(account1.id(), -1);
                        if (connectionId != -1) {
                            server.get().sendToTCP(connectionId, syncRaidWaitingStateS2C);
                        }
                    });
                }
            } else { // Create a new raid
                Account target = server.getDatabase().getAccounts().getRaidTarget(account.id());
                if (target != null) {
                    DungeonMapDefinition dungeonMap = server.getDatabase().getDungeonMaps().getByAccountId(target.id());
                    if (dungeonMap != null) {
                        RaidDefinition raid = server.getDatabase().getRaids().create(account, character, target, requiredCharacters, LocalDateTime.now());
                        if (raid != null) {
                            server.get().sendToTCP(connection.getID(), new SendRaidTargetS2C(raid, dungeonMap.templateId(), dungeonMap.tilesetId()));
                            server.getState().getRaids().put(raid.id(), new ServerRaid(server, new ServerDungeonMap(dungeonMap, raid.target().id()), serverCharacter, raid));
                        }
                    }
                }
            }
        } else if (object instanceof LeaveRaidC2S(long raidId, long accountId, RemovalReason reason)) {
            RaidDefinition raid = server.getDatabase().getRaids().getById(raidId);
            if (raid != null) {
                server.getState().getRaids().get(raidId).removeCharacter(accountId, reason);
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
        } else if (object instanceof CharacterMoveC2S(long raidId, long characterId, int movementFlags, float rotation)) {
            if (server.getState().getRaids().containsKey(raidId)) {
                ServerRaid raid = server.getState().getRaids().get(raidId);
                ServerCharacter serverCharacter = raid.getCharacterById(characterId);
                if (serverCharacter == null)
                    return;
                serverCharacter.updateVelocity(serverCharacter.getVelocity(), movementFlags, rotation);
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

            ChatMessageS2C packet = new ChatMessageS2C(finalMessage);
            faction.accounts().forEach(account -> {
                if (account.id() == finalMessage.accountId())
                    return;

                int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(account.id(), -1);
                if (connectionId != -1) {
                    server.get().sendToTCP(connectionId, packet);
                }
            });
        } else if (object instanceof SaveDungeonMapC2S(long accountId, String templateId, String tilesetId)) {
            Account account = server.getDatabase().getAccounts().getById(accountId);
            DungeonMapDefinition dungeonMap = server.getDatabase().getDungeonMaps().getByAccountId(accountId);
            if (account != null && dungeonMap != null) {
                server.getDatabase().getDungeonMaps().updateLastEditDate(accountId, LocalDateTime.now());
                server.getDatabase().getDungeonMaps().updateDungeonMap(accountId, templateId, tilesetId);
                dungeonMap = new DungeonMapDefinition(dungeonMap.id(), accountId, templateId, tilesetId, LocalDateTime.now());
            } else {
                dungeonMap = server.getDatabase().getDungeonMaps().create(accountId, templateId, tilesetId, LocalDateTime.now());
            }

            if (dungeonMap != null) {
                server.get().sendToTCP(connection.getID(), new SendDungeonMapS2C(dungeonMap));
            }
        } else if (object instanceof RequestDungeonMapC2S(long accountId)) {
            DungeonMapDefinition dungeonMap = server.getDatabase().getDungeonMaps().getByAccountId(accountId);
            if (dungeonMap != null) {
                server.get().sendToTCP(connection.getID(), new SendDungeonMapS2C(dungeonMap));
            }
        } else if (object instanceof AttackC2S(long raidId, long accountId, float mouseDirX, float mouseDirY, long clientId, int attackIndex, int attackId)) {
            ServerRaid serverRaid = server.getState().getRaids().get(raidId);
            if (serverRaid != null) {
                ServerCharacter character = serverRaid.getCharacterByAccountId(accountId);
                if (character != null) {
                    WeaponItem weapon = character.getEquipment().getWeapon();
                    if (weapon == null)
                        return;
                    List<Attack> leaves = new ArrayList<>();
                    character.collectBulletAttacks(weapon.attack(), leaves);
                    Attack attack = leaves.get(attackIndex);

                    long currentTime = System.currentTimeMillis();
                    if (attackId != character.getCurrentAttackId()) {
                        long lastAttackTime = character.getLastAttackTime();
                        long cooldown = (long) StatUtils.getCalculatedDexterity(character, character.getStats().dexterity().value());

                        if (currentTime - lastAttackTime < cooldown) {
                            server.get().sendToTCP(connection.getID(), new AttackResultS2C(PacketResult.FAIL, clientId, List.of()));
                            return;
                        }

                        if (!AttackEvents.PRE.invoker().onPreAttack(character, weapon.attack(), serverRaid)) {
                            server.get().sendToTCP(connection.getID(), new AttackResultS2C(PacketResult.FAIL, clientId, List.of()));
                            return;
                        }

                        character.setCurrentAttackId(attackId);
                        character.setLastAttackTime(currentTime);
                    }

                    List<BulletNode<?, ?>> bulletNodes = character.tryAttackLeaf(attack, serverRaid, new float[]{mouseDirX, mouseDirY}, attackIndex);
                    server.get().sendToTCP(connection.getID(), new AttackResultS2C(PacketResult.SUCCESS, clientId, bulletNodes.stream().map(Entity::id).toList()));

                    serverRaid.getAttackEntries().add(new AttacksS2C.Entry(accountId, mouseDirX, mouseDirY));
                    AttackEvents.POST.invoker().onPostAttack(character, weapon.attack(), serverRaid);
                }
            }
        } else if (object instanceof CancelJoinRaidC2S(long accountId, long raidId)) {
            ServerRaid serverRaid = server.getState().getRaids().get(raidId);
            if (serverRaid != null && serverRaid.getStatus() == ServerRaid.Status.WAITING) {
                serverRaid.get().attackers().removeIf(account -> account.id() == accountId);
                serverRaid.get().characters().removeIf(character -> character.accountId() == accountId);

                if (serverRaid.get().attackers().isEmpty()) {
                    server.getDatabase().getRaids().delete(raidId);
                    server.getState().getRaids().remove(raidId);
                } else server.getDatabase().getRaids().updateAttackers(serverRaid.get());

                SyncRaidWaitingStateS2C syncRaidWaitingStateS2C = new SyncRaidWaitingStateS2C(serverRaid.get());
                LeaveRaidS2C leaveRaidS2C = new LeaveRaidS2C(serverRaid.get().id(), accountId, RemovalReason.CANCEL);
                serverRaid.get().attackers().forEach(account1 -> {
                    int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(account1.id(), -1);
                    if (connectionId != -1) {
                        server.get().sendToTCP(connectionId, syncRaidWaitingStateS2C);
                        server.get().sendToTCP(connectionId, leaveRaidS2C);
                    }
                });
            }
        } else if (object instanceof ExecuteCommandC2S(long accountId, long raidId, String commandType, String[] args)) {
            SimpleCommand.Result result = Command.tryExecute(server, accountId, raidId, commandType, args);
            if (result.success()) {
                Command.LOGGER.info(result.message());
            } else Command.LOGGER.error(result.message());
            // TODO: Send result to client
        }
    }
}
