package dev.creoii.dungeoneer.network.c2s;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Arrays;

public record ExecuteCommandC2S(long accountId, long raidId, String commandType, String[] args) {
    public static final Codec<ExecuteCommandC2S> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.LONG.fieldOf("account_id").forGetter(ExecuteCommandC2S::accountId),
            Codec.LONG.fieldOf("raid_id").forGetter(ExecuteCommandC2S::raidId),
            Codec.STRING.fieldOf("command_type").forGetter(ExecuteCommandC2S::commandType),
            Codec.STRING.listOf().fieldOf("args").forGetter(executeCommandC2S -> Arrays.asList(executeCommandC2S.args))
        ).apply(instance, (accountId, raidId, commandType, args) -> new ExecuteCommandC2S(accountId, raidId, commandType, args.toArray(new String[]{})));
    });

    public static void write(Output output, ExecuteCommandC2S o) {
        output.writeLong(o.accountId);
        output.writeLong(o.raidId);
        output.writeString(o.commandType);
        output.writeInt(o.args.length, true);
        for (String s : o.args) {
            output.writeString(s);
        }
    }

    public static ExecuteCommandC2S read(Input input) {
        long accountId = input.readLong();
        long raidId = input.readLong();
        String commandType = input.readString();
        int argCount = input.readInt(true);
        String[] args = new String[argCount];
        for (int i = 0; i < argCount; i++) {
            args[i] = input.readString();
        }
        return new ExecuteCommandC2S(accountId, raidId, commandType, args);
    }
}
