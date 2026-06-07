package dev.creoii.dungeoneer.client.game;

import dev.creoii.dungeoneer.definitions.Raid;
import org.jspecify.annotations.Nullable;

public class ClientRaid {
    private Raid raid;
    private final ClientDungeon dungeon;

    public ClientRaid(Raid raid) {
        this.raid = raid;
        dungeon = new ClientDungeon();
    }

    public Raid get() {
        return raid;
    }

    public void set(@Nullable Raid raid) {
        this.raid = raid;
    }

    public ClientDungeon getDungeon() {
        return dungeon;
    }

    public boolean isNull() {
        return raid == null;
    }
}
