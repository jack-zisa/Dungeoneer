package dev.creoii.dungeoneer.util.provider;

import java.util.Random;

public record MapObjectContext(Random random, int x, int y) implements ProviderContext {
}
