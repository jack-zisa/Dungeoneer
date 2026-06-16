package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.network.s2c.raid.SyncRaidTimerS2C;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Tickable;
import dev.creoii.dungeoneer.util.stat.StatUtils;

public class ServerRaid implements Tickable {
    private static final float SYNC_INTERVAL = 5f; // 5 seconds
    private final DungeoneerServer server;
    private final ServerDungeon dungeon;
    private final ServerCharacter character;
    private final long endTime;
    private float timer;

    public ServerRaid(DungeoneerServer server, ServerDungeon dungeon, ServerCharacter character) {
        this.server = server;
        this.dungeon = dungeon;
        this.character = character;
        timer = SYNC_INTERVAL;
        endTime = System.currentTimeMillis() + Constants.RAID_DURATION_MS;
    }

    public ServerDungeon getDungeon() {
        return dungeon;
    }

    public ServerCharacter getCharacter() {
        return character;
    }

    public long getRemainingTimeMs() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    @Override
    public void tick(float dt) {
        timer -= dt;

        if (timer <= 0f) {
            timer += SYNC_INTERVAL;
            server.get().sendToUDP(server.getSessionManager().getAccountConnections().get(character.get().accountId()), new SyncRaidTimerS2C(getRemainingTimeMs()));
        }

        float speed = StatUtils.getCalculatedSpeed(character.getStats().speed().value()) * dt;
        character.setPos(character.getX() + character.getVelocity()[0] * speed, character.getY() + character.getVelocity()[1] * speed);
    }
}
