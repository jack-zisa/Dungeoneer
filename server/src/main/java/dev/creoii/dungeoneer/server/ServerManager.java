package dev.creoii.dungeoneer.server;

import dev.creoii.dungeoneer.server.game.ServerCharacter;
import dev.creoii.dungeoneer.server.game.ServerRaid;
import dev.creoii.dungeoneer.server.game.ServerRaidManager;
import dev.creoii.dungeoneer.util.ObjectManager;

import java.util.Iterator;

public class ServerManager extends ObjectManager<String, ObjectManager<?, ?>> {
    private final DungeoneerServer server;
    private final ServerRaidManager raids;

    protected ServerManager(DungeoneerServer server, int capacity) {
        super(capacity);
        this.server = server;
        add(raids = new ServerRaidManager(server, 1));
    }

    public ServerRaidManager getRaids() {
        return raids;
    }

    public DungeoneerServer getServer() {
        return server;
    }

    @Override
    public void tick(float dt) {
        raids.tick(dt);
    }

    @Override
    public String id() {
        return "ServerManager";
    }
}
