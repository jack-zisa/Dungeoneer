package dev.creoii.dungeoneer.util.provider;

import java.util.Random;

public record TileContext(Random random, int x, int y, long seed) implements ProviderContext {
}
