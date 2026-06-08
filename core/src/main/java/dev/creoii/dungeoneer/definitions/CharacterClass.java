package dev.creoii.dungeoneer.definitions;

import dev.creoii.dungeoneer.util.stat.StatContainer;

import java.util.Locale;

public record CharacterClass(String id, StatContainer stats) {
    public static final CharacterClass KNIGHT = new CharacterClass("knight", new StatContainer(60));
    public static final CharacterClass WIZARD = new CharacterClass("wizard", new StatContainer(100));
    public static final CharacterClass ROGUE = new CharacterClass("rogue", new StatContainer(140));
    public static final CharacterClass PRIEST = new CharacterClass("priest", new StatContainer(100));
    public static final CharacterClass NINJA = new CharacterClass("ninja", new StatContainer(140));
    public static final CharacterClass ARCHER = new CharacterClass("archer", new StatContainer(140));

    public static final CharacterClass[] VALUES = new CharacterClass[]{KNIGHT, WIZARD, ROGUE, PRIEST, NINJA, ARCHER};

    public static CharacterClass parse(String s) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "knight" -> CharacterClass.KNIGHT;
            case "wizard" -> CharacterClass.WIZARD;
            case "priest" -> CharacterClass.PRIEST;
            case "archer" -> CharacterClass.ARCHER;
            case "rogue" -> CharacterClass.ROGUE;
            case "ninja" -> CharacterClass.NINJA;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return id;
    }
}
