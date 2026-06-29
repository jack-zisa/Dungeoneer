package dev.creoii.dungeoneer.util.provider;

public interface Provider<T, C extends ProviderContext> {
    T get(C context);
}
