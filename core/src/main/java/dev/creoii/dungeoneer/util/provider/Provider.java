package dev.creoii.dungeoneer.util.provider;

import dev.creoii.dungeoneer.util.Context;

public interface Provider<T> {
    T get(Context context);
}
