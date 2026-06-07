package dev.creoii.dungeoneer.client;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import dev.creoii.dungeoneer.definitions.Raid;
import dev.creoii.dungeoneer.client.screen.game.GameScreen;
import dev.creoii.dungeoneer.client.screen.main.FactionTab;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.client.screen.LoginScreen;
import dev.creoii.dungeoneer.client.screen.main.PlayTab;
import dev.creoii.dungeoneer.client.screen.main.VaultThroneTab;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.network.PacketSerializer;
import dev.creoii.dungeoneer.network.c2s.character.RequestCharactersC2S;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.s2c.*;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;
import dev.creoii.dungeoneer.network.PacketResult;
import dev.creoii.dungeoneer.network.s2c.faction.CreateFactionResultS2C;
import dev.creoii.dungeoneer.network.s2c.faction.JoinFactionResultS2C;
import dev.creoii.dungeoneer.network.s2c.faction.LeaveFactionResultS2C;
import dev.creoii.dungeoneer.network.s2c.raid.SendRaidS2C;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public record ClientListener(Dungeoneer client) implements Listener {
    @Override
    public void connected(Connection connection) {
        client.get().sendUDP(new RequestLoginC2S());
    }

    @Override
    public void received(Connection connection, Object object) {
        if (!PacketSerializer.INSTANCE.isValidPacket(object))
            return;

        Dungeoneer.LOGGER.debug("%s | Connection %s | %s", connection.getRemoteAddressTCP(), connection.getID(), object.getClass().getSimpleName());

        switch (object) {
            case AuthenticateS2C _ ->
                Gdx.app.postRunnable(() -> client.setScreen(new LoginScreen(client)));
            case LoginResultS2C(PacketResult result, @Nullable Account account) -> {
                if (result == PacketResult.SUCCESS) {
                    client.getState().setAccount(account);
                    client.getState().fillCharacters(account.characterSlots());
                    client.get().sendUDP(new RequestCharactersC2S(account.id()));
                    Gdx.app.postRunnable(() -> client.setScreen(new MainScreen(client)));
                    client.getState().setStatus(ClientState.Status.LOBBY);
                }
                Dungeoneer.LOGGER.info("Login result: %s", result.name());
            }
            case SendCharactersS2C(List<Character> characters) -> {
                if (characters.isEmpty())
                    return;

                client.getState().setCharacters(characters);
                client.getState().setSelectedCharacter(characters.getFirst());

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
            case CreateCharacterResultS2C(PacketResult result, int index, @Nullable Character character) -> {
                if (result == PacketResult.SUCCESS) {
                    client.getState().getCharacters().set(index, character);
                    client.getState().setSelectedCharacter(character);

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
            case SendRaidS2C(Raid raid) -> {
                client.setCurrentRaid(raid);
                Gdx.app.postRunnable(() -> {
                    client.getState().setStatus(ClientState.Status.RAIDING);
                    client.setScreen(new GameScreen(client));
                });
            }
            default -> {
            }
        }
    }
}
