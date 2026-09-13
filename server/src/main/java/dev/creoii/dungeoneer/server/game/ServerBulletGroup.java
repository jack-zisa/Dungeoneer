package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;

public class ServerBulletGroup extends BulletGroup<ServerRaid> {
    private final ServerRaid raid;

    public ServerBulletGroup(ServerRaid raid) {
        this.raid = raid;
    }

    @Override
    public ServerRaid getRaid() {
        return raid;
    }

    @Override
    public void setRaid(ServerRaid raid) {
    }
}
