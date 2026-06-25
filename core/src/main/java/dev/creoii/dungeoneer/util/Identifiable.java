package dev.creoii.dungeoneer.util;

public interface Identifiable {
    String id();

    Identifiable withId(String id);
}
