package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.util.ObjectManager;

import java.util.Iterator;

public class ServerRaidManager extends ObjectManager<Long, ServerRaid> {
    private final DungeoneerServer server;

    public ServerRaidManager(DungeoneerServer server, int capacity) {
        super(capacity);
        this.server = server;
    }

    @Override
    public String id() {
        return "ServerRaidManager";
    }

    @Override
    public void tick(float dt) {
        Iterator<ServerRaid> iterator = getValues().iterator();
        while (iterator.hasNext()) {
            ServerRaid raid = iterator.next();

            Iterator<ServerCharacter> characterIterator = raid.getCharacters().values().iterator();
            while (characterIterator.hasNext()) {
                ServerCharacter character = characterIterator.next();
                int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(character.get().accountId(), -1);
                if (connectionId == -1) characterIterator.remove();
            }

            if (raid.getCharacters().isEmpty()) {
                iterator.remove();
                continue;
            }

            raid.tick(DungeoneerServer.DT);
        }
    }
}
