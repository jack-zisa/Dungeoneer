package dev.creoii.dungeoneer.server.command;

import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.item.inventory.EquipmentInventory;
import dev.creoii.dungeoneer.definitions.item.inventory.Slot;
import dev.creoii.dungeoneer.definitions.item.Item;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.network.s2c.character.StatUpdatesS2C;
import dev.creoii.dungeoneer.network.s2c.character.SyncEquipmentS2C;
import dev.creoii.dungeoneer.server.game.ServerCharacter;
import dev.creoii.dungeoneer.server.game.ServerRaid;
import dev.creoii.dungeoneer.util.stat.Stat;
import dev.creoii.dungeoneer.util.stat.StatContainer;

import java.util.List;

public final class Commands {
    public static void register() {
        ParentCommand effectCommand = Command.registerParent("effect");
        effectCommand.registerChild("add", 1, (server, accountId, raidId, args) -> {
            String statusEffectId = args[0];
            StatusEffect effectType = DataManager.getStatusEffect(statusEffectId);

            if (effectType == null) {
                return Command.Result.fail("effect add", args, "Effect " + statusEffectId + " does not exist.");
            }

            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("effect add", args, "Invalid target.");
            }

            int duration = 0;
            int amplifier = 0;

            if (args.length == 2) {
                duration = Integer.parseInt(args[1]);
            } else if (args.length == 3) {
                amplifier = Integer.parseInt(args[2]);
            }

            raid.getCharacterByAccountId(accountId).addStatusEffect(effectType, amplifier, duration);

            return Command.Result.success("effect add", args);
        });

        effectCommand.registerChild("remove", 1, (server, accountId, raidId, args) -> {
            String statusEffectId = args[0];
            StatusEffect effectType = DataManager.getStatusEffect(statusEffectId);

            if (effectType == null) {
                return Command.Result.fail("effect remove", args, "Effect " + statusEffectId + " does not exist.");
            }

            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("effect remove", args, "Invalid target.");
            }

            raid.getCharacterByAccountId(accountId).removeStatusEffect(effectType);

            return Command.Result.success("effect remove", args);
        });

        effectCommand.registerChild("clear", (server, accountId, raidId, args) -> {
            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("effect clear", args, "Invalid target.");
            }

            raid.getCharacterByAccountId(accountId).clearStatusEffects();
            return Command.Result.success("effect clear", args);
        });

        ParentCommand statCommand = Command.registerParent("stat");
        statCommand.registerChild("set", 2, (server, accountId, raidId, args) -> {
            ServerRaid raid = server.getState().getRaids().get(raidId);
            int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(accountId, -1);
            if (raid == null || connectionId == -1 || raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("set stat", args, "Invalid target.");
            }

            Stat.Type type = Stat.Type.valueOf(args[0].toUpperCase());
            int value = Integer.parseInt(args[1]);
            StatContainer stats = raid.getCharacterByAccountId(accountId).getStats();
            stats.setStat(type, value);
            server.get().sendToTCP(connectionId, new StatUpdatesS2C(accountId, stats));
            return Command.Result.success("set stat", args);
        });

        ParentCommand inventoryCommand = Command.registerParent("inventory");
        inventoryCommand.registerChild("add", 1, (server, accountId, raidId, args) -> {
            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("inventory add", args, "Invalid target.");
            }

            Item item = DataManager.getItem(args[0]);
            if (item == null) {
                return Command.Result.fail("inventory add", args, "Item " + args[0] + " does not exist.");
            }

            ServerCharacter character = raid.getCharacterByAccountId(accountId);
            EquipmentInventory equipment = character.getEquipment();
            Slot slot = equipment.getNextAvailableSlot(item);
            if (equipment.addItem(item)) {
                SyncEquipmentS2C syncEquipmentS2C = new SyncEquipmentS2C(accountId, character.get().id(), List.of(slot));
                raid.getCharacters().values().forEach(serverCharacter -> {
                    server.get().sendToTCP(serverCharacter.getConnectionId(), syncEquipmentS2C);
                });
                return Command.Result.success("inventory add", args);
            }
            return Command.Result.fail("inventory add", args, "Failed to add item.");
        });

        inventoryCommand.registerChild("clear", (server, accountId, raidId, args) -> {
            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("inventory clear", args, "Invalid target.");
            }

            ServerCharacter character = raid.getCharacterByAccountId(accountId);
            EquipmentInventory equipment = character.getEquipment();
            if (equipment.isEmpty())
                return Command.Result.fail("inventory clear", args, "Inventory is already empty.");

            SyncEquipmentS2C syncEquipmentS2C = new SyncEquipmentS2C(accountId, character.get().id(), equipment.clearAndGet());
            raid.getCharacters().values().forEach(serverCharacter -> {
                server.get().sendToTCP(serverCharacter.getConnectionId(), syncEquipmentS2C);
            });
            return Command.Result.success("inventory clear", args);
        });
    }
}
