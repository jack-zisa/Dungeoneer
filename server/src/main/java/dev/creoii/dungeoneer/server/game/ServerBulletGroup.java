package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import dev.creoii.dungeoneer.network.s2c.raid.MoveEntitiesS2C;

public class ServerBulletGroup extends BulletGroup<ServerRaid> implements ServerEntity {
    private ServerRaid raid;

    public ServerBulletGroup(ServerRaid raid) {
        super();
        this.raid = raid;
    }

    @Override
    public ServerRaid getRaid() {
        return raid;
    }

    @Override
    public void setRaid(ServerRaid raid) {
        this.raid = raid;
    }

    @Override
    public boolean applyTransform() {
        if (super.applyTransform()) {
            raid.getMoveEntityEntries().add(new MoveEntitiesS2C.Entry(id(), getX(), getY()));
            return true;
        }
        return false;
    }
}
