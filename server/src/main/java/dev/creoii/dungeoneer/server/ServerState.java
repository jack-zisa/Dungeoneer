package dev.creoii.dungeoneer.server;

import dev.creoii.dungeoneer.server.game.ServerRaid;

import java.util.HashMap;
import java.util.Map;

public class ServerState {
    private final Map<Long, ServerRaid> raids;

    public ServerState(DungeoneerServer server) {
        this.raids = new HashMap<>();
    }

    public Map<Long, ServerRaid> getRaids() {
        return raids;
    }
}
