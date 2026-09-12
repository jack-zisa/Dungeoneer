package dev.creoii.dungeoneer.server.command;

import dev.creoii.dungeoneer.server.DungeoneerServer;

import java.util.Arrays;

public record Command(CommandExecutor executor, int minArgs) {
    public Result execute(DungeoneerServer world, long accountId, long raidId, String[] args) {
        return executor.execute(world, accountId, raidId, args);
    }

    static void register(String id, int minArgs, CommandExecutor executor) {
        Commands.ALL.put(id, new Command(executor, minArgs));
    }

    static void register(String id, CommandExecutor executor) {
        register(id, 0, executor);
    }

    public record Result(String message, boolean success) {
        public static Result success(String commandType, String[] args) {
            return new Result("Successfully executed '/" + commandType + "' with args '" + Arrays.toString(args) + "'", true);
        }

        public static Result fail(String commandType, String[] args, String error) {
            return new Result("Execution of '/" + commandType + "' with args '" + Arrays.toString(args) + "' failed: " + error, false);
        }
    }
}
