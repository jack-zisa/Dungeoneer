package dev.creoii.dungeoneer.client.option;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.client.Dungeoneer;

public record Settings(IntegerOption upKey, IntegerOption leftKey, IntegerOption downKey, IntegerOption rightKey, IntegerOption favoriteCharacter, BooleanOption debug) {
    public static final Settings DEFAULT = new Settings(
        new IntegerOption("up_key", Input.Keys.W),
        new IntegerOption("left_key", Input.Keys.A),
        new IntegerOption("down_key", Input.Keys.S),
        new IntegerOption("right_key", Input.Keys.D),
        new IntegerOption("favorite_character", 0),
        new BooleanOption("debug", true)
    );
    public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("up_key").forGetter(s -> s.upKey.value()),
                Codec.INT.fieldOf("left_key").forGetter(s -> s.leftKey.value()),
                Codec.INT.fieldOf("down_key").forGetter(s -> s.downKey.value()),
                Codec.INT.fieldOf("right_key").forGetter(s -> s.rightKey.value()),
                Codec.INT.fieldOf("favorite_character").forGetter(s -> s.favoriteCharacter.value()),
                Codec.BOOL.fieldOf("debug").forGetter(s -> s.debug.value())
            ).apply(instance, (up, left, down, right, favoriteCharacter, debug) ->
                new Settings(
                    new IntegerOption("up_key", up),
                    new IntegerOption("left_key", left),
                    new IntegerOption("down_key", down),
                    new IntegerOption("right_key", right),
                    new IntegerOption("favorite_character", favoriteCharacter),
                    new BooleanOption("debug", debug)
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
            favoriteCharacter.setValue(loaded.favoriteCharacter.value());
            debug.setValue(loaded.debug.value());
        }
    }

    public void save() {
        CODEC.encodeStart(JsonOps.INSTANCE, this).resultOrPartial(Dungeoneer.LOGGER::error).ifPresent(json -> Gdx.files.local("settings.json").writeString(Dungeoneer.GSON.toJson(json), false));
    }
}
