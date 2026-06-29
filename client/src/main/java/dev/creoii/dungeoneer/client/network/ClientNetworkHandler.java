package dev.creoii.dungeoneer.client.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.AnimationState;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.client.render.screen.LoginScreen;
import dev.creoii.dungeoneer.client.render.screen.editor.ClientTiles;
import dev.creoii.dungeoneer.client.render.screen.game.GameScreen;
import dev.creoii.dungeoneer.client.render.screen.main.FactionTab;
import dev.creoii.dungeoneer.client.render.screen.main.MainScreen;
import dev.creoii.dungeoneer.client.render.screen.main.PlayTab;
import dev.creoii.dungeoneer.client.render.screen.main.VaultThroneTab;
import dev.creoii.dungeoneer.definitions.*;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.attack.Attack;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.network.NetworkQueue;
import dev.creoii.dungeoneer.network.PacketResult;
import dev.creoii.dungeoneer.network.PacketSerializer;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.c2s.character.RequestCharactersC2S;
import dev.creoii.dungeoneer.network.c2s.character.RequestFactionC2S;
import dev.creoii.dungeoneer.network.c2s.dungeon.RequestDungeonMapC2S;
import dev.creoii.dungeoneer.network.s2c.LoadDataS2C;
import dev.creoii.dungeoneer.network.s2c.SyncDataS2C;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;
import dev.creoii.dungeoneer.network.s2c.character.CharacterMoveS2C;
import dev.creoii.dungeoneer.network.s2c.character.CreateCharacterResultS2C;
import dev.creoii.dungeoneer.network.s2c.character.SendCharactersS2C;
import dev.creoii.dungeoneer.network.s2c.character.SendFactionS2C;
import dev.creoii.dungeoneer.network.s2c.dungeon.SendDungeonMapS2C;
import dev.creoii.dungeoneer.network.s2c.faction.*;
import dev.creoii.dungeoneer.network.s2c.raid.*;
import dev.creoii.dungeoneer.util.Constants;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClientNetworkHandler implements Listener {
    private final Dungeoneer client;
    private final NetworkQueue networkQueue;

    public ClientNetworkHandler(Dungeoneer client) {
        this.client = client;
        networkQueue = new NetworkQueue();
        client.get().addListener(this);

        PacketSerializer.registerDefault(client.get().getKryo());
    }

    public void update(float dt) {
        NetworkQueue.QueuedPacket packet;
        while ((packet = networkQueue.queue().poll()) != null && PacketSerializer.INSTANCE.isValidPacket(packet.data())) {
            handlePacket(packet.connection(), packet.data());
        }
    }

    @Override
    public void connected(Connection connection) {
        client.get().sendTCP(new RequestLoginC2S());
    }

    @Override
    public void received(Connection connection, Object object) {
        networkQueue.queuePacket(connection, object);
    }

    public void handlePacket(Connection connection, Object object) {
        if (!PacketSerializer.INSTANCE.isValidPacket(object))
            return;

        if (client.getSettings().debug().value()) Dungeoneer.LOGGER.debug("%s | Connection %s | %s", connection.getRemoteAddressTCP(), connection.getID(), object.getClass().getSimpleName());

        switch (object) {
            case AuthenticateS2C _ -> Gdx.app.postRunnable(() -> client.setScreen(new LoginScreen(client)));
            case LoginResultS2C(PacketResult result, @Nullable Account account) -> {
                if (result == PacketResult.SUCCESS) {
                    client.getState().setAccount(account);
                    client.getState().fillCharacters(account.characterSlots());
                    client.get().sendTCP(new RequestCharactersC2S(account.id()));
                    client.get().sendTCP(new RequestDungeonMapC2S(account.id()));
                    client.get().sendTCP(new RequestFactionC2S(account.id()));
                    Gdx.app.postRunnable(() -> client.setScreen(new MainScreen(client)));
                    client.getState().setStatus(ClientState.Status.LOBBY);
                }
                Dungeoneer.LOGGER.info("Login result: %s", result.name());
            }
            case SendCharactersS2C(List<CharacterDefinition> characters) -> {
                if (characters.isEmpty())
                    return;

                client.getState().setCharacters(characters);
                client.getState().setActiveCharacter(characters.getFirst());

                Gdx.app.postRunnable(() -> {
                    if (client.getScreen() instanceof MainScreen screen) {
                        if (screen.getSelectedTab() instanceof VaultThroneTab vaultThroneTab) {
                            vaultThroneTab.select();
                        } else if (screen.getSelectedTab() instanceof PlayTab playTab) {
                            playTab.select();
                        }
                    }
                });
            }
            case SendFactionS2C(Faction faction) -> {
                if (faction == null)
                    return;

                client.getState().setFaction(faction);

                Gdx.app.postRunnable(() -> {
                    if (client.getScreen() instanceof MainScreen screen) {
                        if (screen.getSelectedTab() instanceof FactionTab factionTab) {
                            factionTab.select();
                        }
                    }
                });
            }
            case CreateCharacterResultS2C(PacketResult result, int index, @Nullable CharacterDefinition character) -> {
                if (result == PacketResult.SUCCESS) {
                    client.getState().getCharacters().set(index, character);
                    client.getState().setActiveCharacter(character);

                    Dungeoneer.LOGGER.info("Created new character of class: %s", character.characterClass().id());

                    Gdx.app.postRunnable(() -> {
                        if (client.getScreen() instanceof MainScreen screen) {
                            if (screen.getSelectedTab() instanceof VaultThroneTab vaultThroneTab) {
                                vaultThroneTab.select();
                            } else if (screen.getSelectedTab() instanceof PlayTab playTab) {
                                playTab.select();
                            }
                        }
                    });
                }
            }
            case CreateFactionResultS2C(PacketResult result, @Nullable Faction faction) -> {
                if (result == PacketResult.SUCCESS) {
                    client.getState().setFaction(faction);
                    client.getState().setAccount(client.getState().getAccount().copyWithFaction(faction.id(), LocalDateTime.now()));

                    Gdx.app.postRunnable(() -> {
                        if (client.getScreen() instanceof MainScreen screen) {
                            if (screen.getSelectedTab() instanceof FactionTab factionTab) {
                                factionTab.select();
                            }
                        }
                    });
                }
            }
            case JoinFactionResultS2C(PacketResult result, @Nullable Faction faction) -> {
                if (result == PacketResult.SUCCESS) {
                    client.getState().setFaction(faction);
                    client.getState().setAccount(client.getState().getAccount().copyWithFaction(faction.id(), LocalDateTime.now()));

                    Gdx.app.postRunnable(() -> {
                        if (client.getScreen() instanceof MainScreen screen) {
                            if (screen.getSelectedTab() instanceof FactionTab factionTab) {
                                factionTab.select();
                            }
                        }
                    });
                }
            }
            case LeaveFactionResultS2C(PacketResult result) -> {
                if (result == PacketResult.SUCCESS) {
                    client.getState().setFaction(null);
                    client.getState().setAccount(client.getState().getAccount().copyWithFaction(-1L, null));

                    Gdx.app.postRunnable(() -> {
                        if (client.getScreen() instanceof MainScreen screen) {
                            if (screen.getSelectedTab() instanceof FactionTab factionTab) {
                                factionTab.select();
                            }
                        }
                    });
                }
            }
            case SendRaidTargetS2C(RaidDefinition raid, String templateId, String tilesetId) -> {
                client.getState().setCurrentRaid(raid, templateId, tilesetId);
                client.getState().syncRaid(raid);
            }
            case SearchFactionResultS2C(PacketResult result, List<Faction> factions) -> {
                if (result == PacketResult.SUCCESS) {
                    Gdx.app.postRunnable(() -> {
                        if (client.getScreen() instanceof MainScreen screen) {
                            if (screen.getSelectedTab() instanceof FactionTab factionTab) {
                                factionTab.refreshSearchResults(factions);
                                factionTab.select();
                            }
                        }
                    });
                }
            }
            case CharacterMoveS2C(long characterId, float x, float y) -> {
                if (!client.getState().getActiveCharacter().isNull() && characterId == client.getState().getActiveCharacter().get().id()) {
                    ClientCharacter character = client.getState().getActiveCharacter();

                    character.setPos(x, y);

                    float errorX = character.getX() - x;
                    float errorY = character.getY() - y;

                    character.getCorrection()[0] += errorX;
                    character.getCorrection()[1] += errorY;
                }
            }
            case ChatMessageS2C(Message message) -> {
                LinkedHashMap<Long, Message> messages = client.getState().getFaction().recentMessages();
                messages.put(message.messageId(), message);

                Gdx.app.postRunnable(() -> {
                    if (client.getScreen() instanceof MainScreen mainScreen && mainScreen.getSelectedTab() instanceof FactionTab factionTab) {
                        factionTab.refreshChat();
                    }
                });
            }
            case FlagChatMessageS2C(long localId, Message message) -> {
                LinkedHashMap<Long, Message> messages = client.getState().getFaction().recentMessages();

                Message message1 = messages.get(localId);
                if (message1 != null) {
                    messages.remove(localId);
                    messages.put(message.messageId(), new Message(message.messageId(), message1.factionId(), message1.accountId(), "*****", true));

                    Gdx.app.postRunnable(() -> {
                        if (client.getScreen() instanceof MainScreen mainScreen && mainScreen.getSelectedTab() instanceof FactionTab factionTab) {
                            factionTab.refreshChat();
                        }
                    });
                }
            }
            case SendDungeonMapS2C(DungeonMapDefinition definition) -> client.getState().setDungeonMap(definition);
            case SyncDataS2C(String schema, byte[] data) -> {
                Path cacheRoot = Paths.get(System.getProperty("user.dir"), "cache", "data");
                Path schemaRoot = cacheRoot.resolve(schema);
                try {
                    if (Files.exists(schemaRoot)) {
                        try (Stream<Path> stream = Files.walk(schemaRoot)) {
                            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(data))) {
                    ZipEntry entry;
                    while ((entry = zipIn.getNextEntry()) != null) {
                        Path filePath = cacheRoot.resolve(entry.getName()).normalize();
                        if (!filePath.startsWith(cacheRoot)) {
                            throw new IOException("Invalid zip entry: " + entry.getName());
                        }

                        if (entry.isDirectory()) {
                            Files.createDirectories(filePath);
                        } else {
                            Files.createDirectories(filePath.getParent());
                            Files.write(filePath, zipIn.readAllBytes());
                        }

                        zipIn.closeEntry();
                    }
                } catch (IOException e) {
                    Dungeoneer.LOGGER.error("Client failed to sync data: " + e);
                }
            }
            case LoadDataS2C() -> {
                DataManager.load(Paths.get(System.getProperty("user.dir"), "cache", "data"));
                DataManager.setDebug(client.getSettings().debug().value()); // TODO: Sync to settings option changes
                Gdx.app.postRunnable(() -> ClientTiles.load(client));
            }
            case AttackResultS2C(PacketResult result) -> {
                ClientCharacter character = client.getState().getActiveCharacter();
                if (!character.isNull()) {
                    character.setAttackPending(false);
                    if (result == PacketResult.SUCCESS) character.setLastAttackTime(System.currentTimeMillis());
                }
            }
            case SyncRaidTimerS2C(long timeRemaining) -> {
                ClientRaid raid = client.getState().getCurrentRaid();
                if (!raid.isNull() && timeRemaining != raid.getRemainingTimeMs()) {
                    if (client.getSettings().debug().value()) Dungeoneer.LOGGER.debug("Synced remaining raid time from %s to %s.", raid.getRemainingTimeMs(), timeRemaining);
                    raid.syncTimer(timeRemaining);
                }
            }
            case SyncRaidWaitingStateS2C(RaidDefinition raidDefinition) -> {
                ClientRaid raid = client.getState().getCurrentRaid();
                if (raid.isNull() || raid.get().id() != raidDefinition.id())
                    return;

                raid.set(raidDefinition);
                client.getState().syncRaid(raidDefinition);
                if (raidDefinition.characters().isEmpty()) {
                    Gdx.app.postRunnable(() -> {
                        client.getState().setStatus(ClientState.Status.LOBBY);
                        client.setScreen(new MainScreen(client));
                    });
                } else if (raid.get().characters().size() == raid.get().requiredCharacters()) {
                    Gdx.app.postRunnable(() -> {
                        client.getState().setStatus(ClientState.Status.RAIDING);
                        client.getState().getCurrentRaid().setStatus(Raid.Status.ACTIVE);
                        client.setScreen(new GameScreen(client));
                    });
                }
            }
            case MoveRaidCharactersS2C(List<MoveRaidCharactersS2C.Entry> entries) -> {
                ClientRaid raid = client.getState().getCurrentRaid();
                if (raid == null)
                    return;

                for (MoveRaidCharactersS2C.Entry entry : entries) {
                    if (entry.accountId() == client.getState().getAccount().id())
                        continue;

                    ClientCharacter character = raid.getCharacters().get(entry.accountId());
                    if (character != null && !character.isNull()) {
                        character.setPos(entry.x(), entry.y());

                        float errorX = character.getX() - entry.x();
                        float errorY = character.getY() - entry.y();

                        character.getCorrection()[0] += errorX;
                        character.getCorrection()[1] += errorY;
                    }
                }
            }
            case RaidCharacterWaitStatusS2C(@Nullable CharacterDefinition characterDefinition, long accountId) -> {
                ClientRaid raid = client.getState().getCurrentRaid();
                if (raid == null)
                    return;

                if (accountId == -1L) raid.addCharacter(characterDefinition.accountId(), new ClientCharacter(client, characterDefinition));
                else raid.getCharacters().remove(accountId);
            }
            case AttacksS2C(List<AttacksS2C.Entry> entries) -> entries.forEach(entry -> {
                if (entry.accountId() == client.getState().getAccount().id())
                    return;

                ClientCharacter character = client.getState().getCurrentRaid().getCharacters().get(entry.accountId());
                if (character == null)
                    return;

                AnimationState animationState = character.getAnimationState();

                Attack attack = DataManager.getAttack(Constants.TEST_ATTACK);
                character.attack(attack, client.getState().getCurrentRaid(), new float[]{entry.mouseDirX(), entry.mouseDirY()}, (integer, integer2) -> client.getState().getCurrentRaid().getDungeonMap().isSolid(integer, integer2, false));

                character.setAnimationState(AnimationState.toAttacking(animationState));
            });
            case DamageCharacterS2C(long accountId, int damage) -> {
                if (accountId == client.getState().getAccount().id()) {
                    ClientCharacter character = client.getState().getActiveCharacter();
                    if (character.isNull()) return;
                    character.damage(damage);
                    Gdx.app.postRunnable(() -> {
                        if (client.getScreen() instanceof GameScreen gameScreen) {
                            gameScreen.getHealthBar().update();
                        }
                    });
                } else {
                    ClientCharacter character = client.getState().getCurrentRaid().getCharacters().get(accountId);
                    if (character == null || character.isNull()) return;
                    character.damage(damage);
                }
            }
            default -> {
            }
        }
    }
}
