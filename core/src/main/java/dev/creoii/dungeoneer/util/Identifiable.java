package dev.creoii.dungeoneer.util;

public interface Identifiable<T> {
    T id();

    default Identifiable<T> withId(T id) {
        return this;
    }
}
