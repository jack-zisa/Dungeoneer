package dev.creoii.dungeoneer.client;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import dev.creoii.dungeoneer.client.screen.MainScreen;
import dev.creoii.dungeoneer.client.screen.LoginScreen;
import dev.creoii.dungeoneer.network.c2s.account.AuthenticateC2S;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;

public record ClientListener(Dungeoneer client) implements Listener {
    @Override
    public void connected(Connection connection) {
        client.get().sendUDP(new AuthenticateC2S());
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof AuthenticateS2C) {
            Gdx.app.postRunnable(() -> client.setScreen(new LoginScreen(client)));
        } else if (object instanceof LoginResultS2C(int resultId)) {
            LoginResultS2C.Result result = LoginResultS2C.Result.values()[resultId];
            if (result == LoginResultS2C.Result.SUCCESS) {
                Gdx.app.postRunnable(() -> client.setScreen(new MainScreen()));
            }
            Dungeoneer.LOGGER.info("Login result: %s", result.name());
        }
    }
}
