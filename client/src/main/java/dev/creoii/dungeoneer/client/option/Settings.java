package dev.creoii.dungeoneer.client.option;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.client.Dungeoneer;

public record Settings(
    IntegerOption upKey, IntegerOption leftKey, IntegerOption downKey, IntegerOption rightKey,
    IntegerOption rotateLeftKey, IntegerOption rotateRightKey, IntegerOption resetRotationKey, IntegerOption cameraRotationSpeed,
    IntegerOption favoriteCharacter,
    IntegerOption debugKey, IntegerOption commandKey
) {
    public static final Settings DEFAULT = new Settings(
        new IntegerOption("up_key", Input.Keys.W),
        new IntegerOption("left_key", Input.Keys.A),
        new IntegerOption("down_key", Input.Keys.S),
        new IntegerOption("right_key", Input.Keys.D),
        new IntegerOption("rotate_left_key", Input.Keys.Q),
        new IntegerOption("rotate_right_key", Input.Keys.E),
        new IntegerOption("reset_rotation_key", Input.Keys.Z),
        new IntegerOption("camera_rotation_speed", 100),
        new IntegerOption("favorite_character", 0),
        new IntegerOption("debug_key", Input.Keys.F3),
        new IntegerOption("command_key", Input.Keys.SLASH)
    );
    public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("up_key").orElse(DEFAULT.upKey.value()).forGetter(s -> s.upKey.value()),
                Codec.INT.fieldOf("left_key").orElse(DEFAULT.leftKey.value()).forGetter(s -> s.leftKey.value()),
                Codec.INT.fieldOf("down_key").orElse(DEFAULT.downKey.value()).forGetter(s -> s.downKey.value()),
                Codec.INT.fieldOf("right_key").orElse(DEFAULT.rightKey.value()).forGetter(s -> s.rightKey.value()),
                Codec.INT.fieldOf("rotate_left_key").orElse(DEFAULT.rotateLeftKey.value()).forGetter(s -> s.rotateLeftKey.value()),
                Codec.INT.fieldOf("rotate_right_key").orElse(DEFAULT.rotateRightKey.value()).forGetter(s -> s.rotateRightKey.value()),
                Codec.INT.fieldOf("reset_rotation_key").orElse(DEFAULT.resetRotationKey.value()).forGetter(s -> s.resetRotationKey.value()),
                Codec.INT.fieldOf("camera_rotation_speed").orElse(DEFAULT.cameraRotationSpeed.value()).forGetter(s -> s.cameraRotationSpeed.value()),
                Codec.INT.fieldOf("favorite_character").orElse(DEFAULT.favoriteCharacter.value()).forGetter(s -> s.favoriteCharacter.value()),
                Codec.INT.fieldOf("debug_key").orElse(DEFAULT.debugKey.value()).forGetter(s -> s.debugKey.value()),
                Codec.INT.fieldOf("command_key").orElse(DEFAULT.commandKey.value()).forGetter(s -> s.commandKey.value())
            ).apply(instance, (up, left, down, right, rotateLeft, rotateRight, resetRotation, cameraRotationSpeed, favoriteCharacter, debugKey, commandKey) ->
                new Settings(
                    new IntegerOption("up_key", up),
                    new IntegerOption("left_key", left),
                    new IntegerOption("down_key", down),
                    new IntegerOption("right_key", right),
                    new IntegerOption("rotate_left_key", rotateLeft),
                    new IntegerOption("rotate_right_key", rotateRight),
                    new IntegerOption("reset_rotation_key", resetRotation),
                    new IntegerOption("camera_rotation_speed", cameraRotationSpeed),
                    new IntegerOption("favorite_character", favoriteCharacter),
                    new IntegerOption("debug_key", debugKey),
                    new IntegerOption("command_key", commandKey)
                )
            )
        );

    public void load() {
        FileHandle file = Gdx.files.local("settings.json");
        if (file.exists()) {
            JsonElement json = Dungeoneer.GSON.fromJson(file.reader(), JsonElement.class);
            Settings loaded = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(s -> {
                Dungeoneer.LOGGER.error(s);
                return new RuntimeException();
            });
            upKey.setValue(loaded.upKey.value());
            leftKey.setValue(loaded.leftKey.value());
            downKey.setValue(loaded.downKey.value());
            rightKey.setValue(loaded.rightKey.value());
            rotateLeftKey.setValue(loaded.rotateLeftKey.value());
            rotateRightKey.setValue(loaded.rotateRightKey.value());
            resetRotationKey.setValue(loaded.resetRotationKey.value());
            cameraRotationSpeed.setValue(loaded.cameraRotationSpeed.value());
            favoriteCharacter.setValue(loaded.favoriteCharacter.value());
            debugKey.setValue(loaded.debugKey.value());
            commandKey.setValue(loaded.commandKey.value());
        }
    }

    public void save() {
        CODEC.encodeStart(JsonOps.INSTANCE, this).resultOrPartial(Dungeoneer.LOGGER::error).ifPresent(json -> Gdx.files.local("settings.json").writeString(Dungeoneer.GSON.toJson(json), false));
    }
}
