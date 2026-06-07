package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.network.s2c.character.CharacterMoveS2C;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.util.Tickable;

public class ServerRaid implements Tickable {
    private static final float EP2 = .001f * .001f;
    private final DungeoneerServer server;
    private final int connectionId;
    private final ServerDungeon dungeon;
    private final ServerCharacter character;

    public ServerRaid(DungeoneerServer server, int connectionId, ServerDungeon dungeon, ServerCharacter character) {
        this.server = server;
        this.connectionId = connectionId;
        this.dungeon = dungeon;
        this.character = character;
    }

    public ServerDungeon getDungeon() {
        return dungeon;
    }

    public ServerCharacter getCharacter() {
        return character;
    }

    @Override
    public void tick(float dt) {
        if (character.canMove()) {
            float speed = getCharacter().getSpeed();

            float vx = 0f;
            float vy = 0f;

            if ((character.getMovementFlags() & ServerCharacter.LEFT) != 0) vx -= speed;
            if ((character.getMovementFlags() & ServerCharacter.RIGHT) != 0) vx += speed;
            if ((character.getMovementFlags() & ServerCharacter.UP) != 0) vy += speed;
            if ((character.getMovementFlags() & ServerCharacter.DOWN) != 0) vy -= speed;

            character.getVelocity().set(vx, vy);
        } else character.getVelocity().set(0f, 0f);

        if (character.isMoving()) {
            float dx = character.getVelocity().x;
            float dy = character.getVelocity().y;
            if (dx * dx + dy * dy > EP2) {
                character.getPos().mulAdd(character.getVelocity(), dt);
                server.get().sendToTCP(connectionId, new CharacterMoveS2C(character.get().id(), character.getPos().x, character.getPos().y, dx, dy));
            }
        }
    }
}
