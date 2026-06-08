package dev.creoii.dungeoneer.client.option;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class IntOption implements Option<Integer> {
    public static final Codec<IntOption> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("key").forGetter(IntOption::key),
            Codec.INT.fieldOf("value").forGetter(IntOption::value)
        ).apply(instance, IntOption::new);
    });
    private final String key;
    private int value;

    public IntOption(String key) {
        this.key = key;
    }

    public IntOption(String key, int defaultValue) {
        this.key = key;
        value = defaultValue;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Integer value() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }
}
