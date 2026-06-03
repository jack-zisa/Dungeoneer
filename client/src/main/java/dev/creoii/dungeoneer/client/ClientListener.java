package dev.creoii.dungeoneer.client;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.s2c.account.AllowLoginS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;

import java.util.Scanner;

public record ClientListener(Dungeoneer client) implements Listener {
    @Override
    public void connected(Connection connection) {
        client.get().sendUDP(new RequestLoginC2S());
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof AllowLoginS2C) {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            System.out.print("Enter your password: ");
            String password = scanner.nextLine();

            client.get().sendUDP(new LoginC2S(username, password));
        } else if (object instanceof LoginResultS2C(int resultId)) {
            System.out.print("Login result: " + LoginResultS2C.Result.values()[resultId]);
        }
    }
}
