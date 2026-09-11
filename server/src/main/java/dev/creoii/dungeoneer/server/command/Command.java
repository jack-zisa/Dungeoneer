package dev.creoii.dungeoneer.server.command;

import com.badlogic.gdx.graphics.Color;
import dev.creoii.dungeoneer.server.DungeoneerServer;

import java.util.Arrays;
import java.util.function.BiFunction;

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

    public enum Result {
        SUCCESS((commandType, args) -> "[Commands] Successfully executed '/" + commandType + "' with args '" + Arrays.toString(args) + "'"),
        FAIL((commandType, args) -> "[Commands] Execution of '/" + commandType + "' with args '" + Arrays.toString(args) + "' failed");

        private final BiFunction<String, String[], String> message;

        Result(BiFunction<String, String[], String> message) {
            this.message = message;
        }

        public String getResultMessage(String commandType, String[] args) {
            return message.apply(commandType, args);
        }

        public String getResultMessageWithReason(String commandType, String[] args, String reason) {
            return message.apply(commandType, args) + ": " + reason;
        }
    }
}
