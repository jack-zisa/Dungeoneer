package dev.creoii.dungeoneer.client.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.esotericsoftware.kryonet.Connection;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.EntityManager;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.*;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.render.ui.screen.LoginScreen;
import dev.creoii.dungeoneer.client.render.ui.editor.ClientTiles;
import dev.creoii.dungeoneer.client.render.ui.element.DeathDialog;
import dev.creoii.dungeoneer.client.render.ui.screen.game.GameScreen;
import dev.creoii.dungeoneer.client.render.ui.screen.game.RaidLoadingScreen;
import dev.creoii.dungeoneer.client.render.ui.screen.main.FactionTab;
import dev.creoii.dungeoneer.client.render.ui.screen.main.MainScreen;
import dev.creoii.dungeoneer.client.render.ui.screen.main.PlayTab;
import dev.creoii.dungeoneer.client.render.ui.screen.main.VaultThroneTab;
import dev.creoii.dungeoneer.definitions.*;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.attack.Attack;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.attack.bullet.SingleBulletType;
import dev.creoii.dungeoneer.definitions.item.inventory.Inventory;
import dev.creoii.dungeoneer.definitions.item.inventory.Slot;
import dev.creoii.dungeoneer.definitions.item.WeaponItem;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.network.NetworkHandler;
import dev.creoii.dungeoneer.network.PacketResult;
import dev.creoii.dungeoneer.network.PacketSerializer;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.c2s.character.RequestCharactersC2S;
import dev.creoii.dungeoneer.network.c2s.character.RequestFactionC2S;
import dev.creoii.dungeoneer.network.c2s.dungeon.RequestDungeonMapC2S;
import dev.creoii.dungeoneer.network.data.BulletPacketData;
import dev.creoii.dungeoneer.network.s2c.LoadDataS2C;
import dev.creoii.dungeoneer.network.s2c.SyncDataS2C;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;
import dev.creoii.dungeoneer.network.s2c.character.*;
import dev.creoii.dungeoneer.network.s2c.dungeon.SendDungeonMapS2C;
import dev.creoii.dungeoneer.network.s2c.faction.*;
import dev.creoii.dungeoneer.network.s2c.raid.*;
import dev.creoii.dungeoneer.util.RemovalReason;
import dev.creoii.dungeoneer.util.event.AttackEvents;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClientNetworkHandler extends NetworkHandler {
    private final Dungeoneer client;

    public ClientNetworkHandler(Dungeoneer client) {
        super();
        this.client = client;
        client.get().addListener(this);
        PacketSerializer.registerDefault(client.get().getKryo());
    }

    @Override
    public void connected(Connection connection) {
        client.get().sendTCP(new RequestLoginC2S());
    }

    @Override
    public void handlePacket(Connection connection, Object object) {
        if (!PacketSerializer.INSTANCE.isValidPacket(object))
            return;

        if (client.isDebug()) Dungeoneer.LOGGER.debug("%s | Connection %s | %s", connection.getRemoteAddressTCP(), connection.getID(), object.getClass().getSimpleName());

        switch (object) {
            case AuthenticateS2C _ -> Gdx.app.postRunnable(() -> client.setScreen(new LoginScreen(client)));
            case LoginResultS2C(PacketResult result, @Nullable Account account) -> {
                if (result == PacketResult.SUCCESS) {
                    client.getState().setAccount(account);
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
                client.getState().setActiveCharacter(0);

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
                    if (client.getState().getCharacters().containsKey(index)) {
                        client.getState().getCharacters().get(index).set(character);
                        client.getState().getActiveCharacter().set(character);

                        Dungeoneer.LOGGER.info("Created new character of class: %s", character.characterClass().id());

                        Gdx.app.postRunnable(() -> {
                            if (client.getScreen() instanceof MainScreen screen) {
                                if (screen.getSelectedTab() instanceof VaultThroneTab vaultThroneTab) {
                                    vaultThroneTab.select(index);
                                } else if (screen.getSelectedTab() instanceof PlayTab playTab) {
                                    playTab.select();
                                }
                            }
                        });
                    }
                } else Dungeoneer.LOGGER.error("Failed to create character.");
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
                DataManager.setDebug(client.isDebug()); // TODO: Sync to settings option changes
                Gdx.app.postRunnable(() -> ClientTiles.load(client));
            }
            case AttackResultS2C(PacketResult result, long clientId, List<Long> entityIds) -> {
                if (entityIds.isEmpty()) return;
                ClientCharacter character = client.getState().getActiveCharacter();
                if (!character.isNull()) {
                    character.setAttackPending(false);
                    Map<Integer, BulletNode<?, ?>> map = character.getPredictedBullets().row(clientId);
                    if (result == PacketResult.SUCCESS) {
                        int size = map.size();

                        if (size != entityIds.size())
                            throw new IllegalStateException("Mismatch between server attack & client attack bullet counts: " + entityIds.size() + " vs " + size);

                        ClientRaid raid = client.getState().getCurrentRaid();
                        for (int i = 0; i < size; ++i) {
                            long entityId = entityIds.get(i);
                            BulletNode<?, ?> bulletNode = character.getPredictedBullets().remove(clientId, i);
                            raid.getEntityManager().add((Entity<ClientRaid>) bulletNode, entityId);

                            if (bulletNode.getType() instanceof SingleBulletType) {
                                raid.getBullets().put(raid.getAndIncrementNextBulletId(), (ClientBullet) bulletNode);
                            } else raid.getBulletGroups().put(raid.getAndIncrementNextBulletId(), (ClientBulletGroup) bulletNode);
                        }

                        character.setLastAttackTime(System.currentTimeMillis());
                    } else {
                        character.getPredictedBullets().rowMap().remove(clientId);
                    }
                    character.freeClientId(clientId);
                }
            }
            case SyncRaidTimerS2C(long timeRemaining) -> {
                ClientRaid raid = client.getState().getCurrentRaid();
                if (!raid.isNull() && timeRemaining != raid.getRemainingTimeMs()) {
                    if (client.isDebug()) Dungeoneer.LOGGER.debug("Synced remaining raid time from %s to %s.", raid.getRemainingTimeMs(), timeRemaining);
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
                    Vector2 spawnPos = raid.getDungeonMap().getTemplate().spawnPos();
                    raid.updateSpawnPositions(spawnPos.x * 8f, spawnPos.y * 8f);
                    Gdx.app.postRunnable(() -> {
                        client.getState().setStatus(ClientState.Status.RAIDING);
                        client.getState().getCurrentRaid().setStatus(Raid.Status.ACTIVE);
                        if (client.getScreen() instanceof RaidLoadingScreen raidLoadingScreen) {
                            raidLoadingScreen.fadeToBlack(1f, 2f, new Action() {
                                @Override
                                public boolean act(float delta) {
                                    client.setScreen(new GameScreen(client));
                                    return true;
                                }
                            });
                        }
                    });
                }
            }
            case MoveCharactersS2C(List<MoveCharactersS2C.Entry> entries) -> {
                ClientRaid raid = client.getState().getCurrentRaid();
                if (raid == null)
                    return;

                for (MoveCharactersS2C.Entry entry : entries) {
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
            case JoinRaidS2C(CharacterDefinition characterDefinition) -> {
                ClientRaid raid = client.getState().getCurrentRaid();
                if (raid.isNull())
                    return;
                raid.addCharacter(characterDefinition.accountId(), new ClientCharacter(client, characterDefinition));
            }
            case AttacksS2C(List<AttacksS2C.Entry> entries) -> entries.forEach(entry -> {
                if (entry.accountId() == client.getState().getAccount().id())
                    return;

                ClientCharacter character = client.getState().getCurrentRaid().getCharacters().get(entry.accountId());
                if (character == null)
                    return;

                WeaponItem weapon = character.getEquipment().getWeapon();
                if (weapon == null)
                    return;
                Attack attack = weapon.attack();
                character.tryAttack(attack, client.getState().getCurrentRaid(), new float[]{entry.mouseDirX(), entry.mouseDirY()});
                AttackEvents.POST.invoker().onPostAttack(character, attack, client.getState().getCurrentRaid());

                character.setAnimationState(AnimationState.toAttacking(character.getAnimationState()));
            });
            case DamageCharactersS2C(List<DamageCharactersS2C.Entry> entries) -> {
                entries.forEach(entry -> {
                    if (entry.accountId() == client.getState().getAccount().id()) {
                        ClientCharacter character = client.getState().getActiveCharacter();
                        if (character.isNull()) return;
                        if (character.damage(entry.damage())) {
                            Gdx.app.postRunnable(() -> {
                                if (client.getScreen() instanceof GameScreen gameScreen) {
                                    gameScreen.getHealthBar().update();
                                }
                            });
                        }
                    } else {
                        ClientCharacter character = client.getState().getCurrentRaid().getCharacters().get(entry.accountId());
                        if (character == null || character.isNull()) return;
                        character.damage(entry.damage());
                    }
                });
            }
            case StatusEffectsS2C(List<StatusEffectsS2C.Entry> entries) -> {
                for (StatusEffectsS2C.Entry entry : entries) {
                    ClientCharacter character;
                    if (entry.accountId() == client.getState().getAccount().id()) {
                        character = client.getState().getActiveCharacter();
                        if (character.isNull()) return;
                    } else {
                        character = client.getState().getCurrentRaid().getCharacters().get(entry.accountId());
                        if (character == null || character.isNull()) return;
                    }
                    character.updateStatusEffects(entry.add(), entry.remove());
                }
            }
            case LeaveRaidS2C(long raidId, long accountId, RemovalReason reason) -> { // TODO: Announce removal reason to the nonexistent chat
                ClientRaid raid = client.getState().getCurrentRaid();
                if (!raid.isNull() && raid.get().id() == raidId && (raid.getCharacters().containsKey(accountId) || accountId == client.getState().getAccount().id())) {
                    if (reason == RemovalReason.DEATH) {
                        if (accountId == client.getState().getAccount().id()) {
                            raid.setStatus(Raid.Status.END);
                            client.getState().setStatus(ClientState.Status.RAID_END);
                            Gdx.app.postRunnable(() -> {
                                if (client.getScreen() instanceof GameScreen gameScreen) {
                                    gameScreen.fadeToBlack(.8f, 2f, new Action() {
                                        @Override
                                        public boolean act(float delta) {
                                            new DeathDialog(client, AbstractScreen.SKIN).show(gameScreen.getStage());
                                            return true;
                                        }
                                    });
                                }
                            });
                        } else {
                            raid.getCharacters().get(accountId).die();
                        }
                    }
                    else raid.removeCharacter(accountId, reason);
                }
            }
            case StatUpdatesS2C(long accountId, StatContainer stats) -> {
                ClientRaid raid = client.getState().getCurrentRaid();
                if (!raid.isNull()) {
                    raid.getCharacters().get(accountId).getStats().set(stats);
                }
            }
            case SyncEquipmentS2C(long accountId, long characterId, List<Slot> slots) -> {
                if (accountId == client.getState().getAccount().id()) {
                    if (client.getScreen() instanceof GameScreen gameScreen) {
                        ClientCharacter character = client.getState().getCharacterById((int) characterId);
                        if (character == null || character.isNull()) {
                            gameScreen.getInventory().refresh(new Inventory(4));
                        } else {
                            slots.forEach(slot -> character.getEquipment().setItem(slot.getIndex(), slot.getItem(), slot.getCount()));
                            gameScreen.getInventory().refresh(character.getEquipment());
                        }
                    }
                } else {
                    ClientCharacter character = client.getState().getCurrentRaid().getCharacters().get(accountId);
                    slots.forEach(slot -> character.getEquipment().setItem(slot.getIndex(), slot.getItem(), slot.getCount()));
                }
            }
            case KillCharacterS2C(CharacterDefinition character) -> {
                if (character.accountId() == client.getState().getAccount().id()) {
                    ClientCharacter character1 = client.getState().getCharacterById((int) character.id());
                    if (character1 != null && !character1.isNull()) {
                        character1.set(character);
                    }
                }
            }
            case MoveEntitiesS2C(List<MoveEntitiesS2C.Entry> entries) -> {
                ClientRaid raid = client.getState().getCurrentRaid();
                if (raid.isNull()) return;
                entries.forEach(entry -> {
                    EntityManager<ClientRaid> entityManager = raid.getEntityManager();
                    if (entityManager.contains(entry.entityId())) {
                        Entity<?> entity = entityManager.get(entry.entityId());
                        entity.setPos(entry.x(), entry.y());
                    }
                });
            }
            case AddEntitiesS2C(List<AddEntitiesS2C.Entry> entries) -> {
                ClientRaid raid = client.getState().getCurrentRaid();
                if (raid.isNull()) return;
                for (AddEntitiesS2C.Entry entry : entries) {
                    switch (entry.data().type()) {
                        case BULLET -> {
                            if (raid.getEntityManager().contains(entry.entityId())) return;

                            BulletPacketData data = (BulletPacketData) entry.data();

                            BulletType bullet = DataManager.getBullet(data.bulletType());

                            BulletNode<?, ?> poolBullet = raid.createHierarchy(data.damage(), entry.x(), entry.y(), data.dirX(), data.dirY(), bullet, data.index(), data.enemy(), 1);
                            raid.getEntityManager().add((Entity<ClientRaid>) poolBullet, entry.entityId());
                            if (bullet instanceof SingleBulletType) {
                                raid.getBullets().put(raid.getAndIncrementNextBulletId(), (ClientBullet) poolBullet);
                            } else raid.getBulletGroups().put(raid.getAndIncrementNextBulletId(), (ClientBulletGroup) poolBullet);

                            Entity<ClientRaid> entity = raid.getEntityManager().get(entry.entityId());
                            if (entity != null)
                                entity.setPos(entry.x(), entry.y());
                        }
                    }
                }
            }
            default -> {
            }
        }
    }
}
