package dev.creoii.dungeoneer.util.provider;

import java.util.Random;

public interface Provider<T> {
    T get(Random random);
}
