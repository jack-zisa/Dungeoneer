package dev.creoii.dungeoneer.util.provider;

import dev.creoii.dungeoneer.util.context.Context;

public interface Provider<T> {
    T get(Context context);
}
