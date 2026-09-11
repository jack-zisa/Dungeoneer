package dev.creoii.dungeoneer.server.command;

import com.badlogic.gdx.utils.ObjectMap;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.network.s2c.character.StatUpdatesS2C;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.server.game.ServerCharacter;
import dev.creoii.dungeoneer.server.game.ServerRaid;
import dev.creoii.dungeoneer.util.logging.Logger;
import dev.creoii.dungeoneer.util.stat.Stat;
import dev.creoii.dungeoneer.util.stat.StatContainer;

import javax.annotation.Nullable;

public final class Commands {
    public static final Logger LOGGER = new Logger(Commands.class.getSimpleName());
    static final ObjectMap<String, Command> ALL = new ObjectMap<>();

    @Nullable
    public static Command.Result tryExecute(DungeoneerServer server, long accountId, long raidId, String commandType, String[] args) {
        if (Commands.ALL.containsKey(commandType)) {
            try {
                Command command = Commands.ALL.get(commandType);
                if (args.length > command.minArgs() - 1) {
                    Command.Result result = command.execute(server, accountId, raidId, args);
                    LOGGER.info(result.getResultMessage(commandType, args));
                    return result;
                } else {
                    LOGGER.error("Command '/" + commandType + "' failed to execute: Not enough arguments.");
                }
            } catch (Exception e) {
                LOGGER.error(Command.Result.FAIL.getResultMessageWithReason(commandType, args, e.toString()));
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Command '/" + commandType + "' not found");
        }
        return null;
    }

    static {
        Command.register("addeffect", 1, (server, accountId, raidId, args) -> {
            String statusEffectId = args[0];
            StatusEffect effectType = DataManager.getStatusEffect(statusEffectId);

            if (effectType == null || raidId == -1) {
                return Command.Result.FAIL;
            }

            Account account = server.getDatabase().getAccounts().getById(accountId);
            ServerRaid raid = server.getState().getRaids().get(raidId);

            if (account == null || raid == null || !raid.getCharacters().containsKey(account.activeCharacterId())) {
                return Command.Result.FAIL;
            }

            int duration = 0;
            int amplifier = 0;

            if (args.length == 2) {
                duration = Integer.parseInt(args[1]);
            } else if (args.length == 3) {
                amplifier = Integer.parseInt(args[2]);
            }

            raid.getCharacterByAccountId(account.id()).addStatusEffect(effectType, amplifier, duration);

            return Command.Result.SUCCESS;
        });

        Command.register("removeeffect", 1, (server, accountId, raidId, args) -> {
            String statusEffectId = args[0];
            StatusEffect effectType = DataManager.getStatusEffect(statusEffectId);

            if (effectType == null || raidId == -1) {
                return Command.Result.FAIL;
            }

            Account account = server.getDatabase().getAccounts().getById(accountId);
            ServerRaid raid = server.getState().getRaids().get(raidId);

            if (account == null || raid == null || !raid.getCharacters().containsKey(account.activeCharacterId())) {
                return Command.Result.FAIL;
            }

            raid.getCharacterByAccountId(account.id()).removeStatusEffect(effectType);

            return Command.Result.SUCCESS;
        });

        Command.register("cleareffects", (server, accountId, raidId, _) -> {
            if (raidId == -1) {
                return Command.Result.FAIL;
            }

            Account account = server.getDatabase().getAccounts().getById(accountId);
            ServerRaid raid = server.getState().getRaids().get(raidId);

            if (account == null || raid == null || !raid.getCharacters().containsKey(account.activeCharacterId())) {
                return Command.Result.FAIL;
            }

            raid.getCharacterByAccountId(account.id()).clearStatusEffects();

            return Command.Result.SUCCESS;
        });

        Command.register("setstat", 2, (server, accountId, raidId, args) -> {
            if (raidId == -1)
                return Command.Result.FAIL;

            Account account = server.getDatabase().getAccounts().getById(accountId);
            ServerRaid raid = server.getState().getRaids().get(raidId);
            int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(accountId, -1);
            if (account == null || raid == null || connectionId == -1 || raid.getCharacters().containsKey(account.activeCharacterId())) {
                return Command.Result.FAIL;
            }

            Stat.Type type = Stat.Type.valueOf(args[0].toUpperCase());
            int value = Integer.parseInt(args[1]);
            StatContainer stats = raid.getCharacterByAccountId(account.id()).getStats();
            stats.setStat(type, value);
            server.get().sendToTCP(connectionId, new StatUpdatesS2C(accountId, stats));
            return Command.Result.SUCCESS;
        });
    }
}
