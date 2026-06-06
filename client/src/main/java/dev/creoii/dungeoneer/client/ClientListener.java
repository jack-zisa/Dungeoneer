package dev.creoii.dungeoneer.client;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import dev.creoii.dungeoneer.client.screen.MainScreen;
import dev.creoii.dungeoneer.client.screen.LoginScreen;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.network.PacketSerializer;
import dev.creoii.dungeoneer.network.c2s.RequestCharactersC2S;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.s2c.*;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;
import dev.creoii.dungeoneer.network.PacketResult;
import org.jspecify.annotations.Nullable;

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

        if (object instanceof AuthenticateS2C) {
            Gdx.app.postRunnable(() -> client.setScreen(new LoginScreen(client)));
        } else if (object instanceof LoginResultS2C(PacketResult result, @Nullable Account account)) {
            if (result == PacketResult.SUCCESS) {
                client.get().sendUDP(new RequestCharactersC2S());

                client.getState().setAccount(account);
                Gdx.app.postRunnable(() -> client.setScreen(new MainScreen(client)));
            }
            Dungeoneer.LOGGER.info("Login result: %s", result.name());
        } else if (object instanceof SendCharactersS2C(List<Character> characters)) {
            client.getState().setCharacters(characters);
            client.getState().setSelectedCharacter(characters.getFirst());

            Gdx.app.postRunnable(() -> {
                if (client.getScreen() instanceof MainScreen screen) {
                    if (screen.getSelectedTab() instanceof MainScreen.VaultThroneTab vaultThroneTab) {
                        vaultThroneTab.select();
                    } else if (screen.getSelectedTab() instanceof MainScreen.PlayTab playTab) {
                        playTab.select();
                    }
                }
            });
        } else if (object instanceof CreateCharacterResultS2C(PacketResult result, @Nullable Character character)) {
            if (result == PacketResult.SUCCESS) {
                client.getState().addCharacter(character);

                Dungeoneer.LOGGER.info("Created new character of class: %s", character.characterClass().id());

                Gdx.app.postRunnable(() -> {
                    if (client.getScreen() instanceof MainScreen screen) {
                        if (screen.getSelectedTab() instanceof MainScreen.VaultThroneTab vaultThroneTab) {
                            vaultThroneTab.select();
                        }
                    }
                });
            }
        } else if (object instanceof CreateFactionResultS2C(PacketResult result, @Nullable Faction faction)) {
            if (result == PacketResult.SUCCESS) {
                client.getState().setFaction(faction);
            }
        } else if (object instanceof JoinFactionResultS2C(PacketResult result, @Nullable Faction faction)) {
            if (result == PacketResult.SUCCESS) {
                client.getState().setFaction(faction);
            }
        } else if (object instanceof LeaveFactionResultS2C(PacketResult result)) {
            if (result == PacketResult.SUCCESS) {
                client.getState().setFaction(null);
            }
        }
    }
}
