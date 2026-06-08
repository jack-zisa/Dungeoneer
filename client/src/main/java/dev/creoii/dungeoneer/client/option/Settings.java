package dev.creoii.dungeoneer.client.option;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.client.Dungeoneer;

public record Settings(IntOption upKey, IntOption leftKey, IntOption downKey, IntOption rightKey) {
    public static final Settings DEFAULT = new Settings(
        new IntOption("up_key", Input.Keys.W),
        new IntOption("left_key", Input.Keys.A),
        new IntOption("down_key", Input.Keys.S),
        new IntOption("right_key", Input.Keys.D)
    );
    public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            IntOption.CODEC.fieldOf("up_key").forGetter(settings -> settings.upKey),
            IntOption.CODEC.fieldOf("left_key").forGetter(settings -> settings.leftKey),
            IntOption.CODEC.fieldOf("down_key").forGetter(settings -> settings.downKey),
            IntOption.CODEC.fieldOf("right_key").forGetter(settings -> settings.rightKey)
        ).apply(instance, Settings::new);
    });

    public void load() {
        FileHandle file = Gdx.files.local("settings.json");
        if (file.exists()) {
            Settings loaded = Dungeoneer.GSON.fromJson(file.reader(), Settings.class);
            upKey.setValue(loaded.upKey.value());
            leftKey.setValue(loaded.leftKey.value());
            downKey.setValue(loaded.downKey.value());
            rightKey.setValue(loaded.rightKey.value());
        }
    }

    public void save() {
        Gdx.files.local("settings.json").writeString(Dungeoneer.GSON.toJson(this), false);
    }

    public boolean isMovementKey(int keycode) {
        return keycode == upKey.value() || keycode == leftKey.value() || keycode == downKey.value() || keycode == rightKey.value();
    }
}
