package dev.creoii.dungeoneer.server.command;

import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.util.Identifiable;

public record SimpleCommand(String id, CommandExecutor executor, int minArgs) implements Command {
    @Override
    public Identifiable withId(String id) {
        return new SimpleCommand(id, executor, minArgs);
    }

    @Override
    public Command.Result execute(DungeoneerServer server, long accountId, long raidId, String[] args) {
        return executor.execute(server, accountId, raidId, args);
    }
}
