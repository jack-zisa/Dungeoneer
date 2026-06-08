package dev.creoii.dungeoneer.definitions;

import dev.creoii.dungeoneer.util.stat.StatContainer;

import java.util.Locale;

public record CharacterClass(String id, StatContainer baseStats, StatContainer maxStats) {
    public static final CharacterClass KNIGHT = new CharacterClass("knight", new StatContainer(17), new StatContainer(50));
    public static final CharacterClass WIZARD = new CharacterClass("wizard", new StatContainer(17), new StatContainer(50));
    public static final CharacterClass ROGUE = new CharacterClass("rogue", new StatContainer(26), new StatContainer(65));
    public static final CharacterClass PRIEST = new CharacterClass("priest", new StatContainer(22), new StatContainer(55));
    public static final CharacterClass NINJA = new CharacterClass("ninja", new StatContainer(27), new StatContainer(60));
    public static final CharacterClass ARCHER = new CharacterClass("archer", new StatContainer(22), new StatContainer(55));

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
