package dev.creoii.dungeoneer.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;

import java.util.List;
import java.util.UUID;

public final class Codecs {
    public static final Codec<UUID> UUID = Codec.STRING.xmap(java.util.UUID::fromString, java.util.UUID::toString);

    public static final Codec<Color> COLOR = Codec.STRING.xmap(Color::valueOf, Color::toString);

    public static final Codec<Vector2> VECTOR_2 = Codec.FLOAT.listOf(2, 2).xmap(floats -> new Vector2(floats.getFirst(), floats.get(1)), vector2 -> List.of(vector2.x, vector2.y));
}
