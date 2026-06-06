package dev.creoii.dungeoneer.definitions;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public record CharacterClass(String id) {
    private static final Random RANDOM = new Random();
    public static final CharacterClass KNIGHT = new CharacterClass("knight");
    public static final CharacterClass WIZARD = new CharacterClass("wizard");
    public static final CharacterClass ROGUE = new CharacterClass("rogue");
    public static final CharacterClass PRIEST = new CharacterClass("priest");
    public static final CharacterClass NINJA = new CharacterClass("ninja");
    public static final CharacterClass ARCHER = new CharacterClass("archer");

    public static CharacterClass random() {
        List<CharacterClass> classes = List.of(KNIGHT, WIZARD, ROGUE, PRIEST, NINJA, ARCHER);
        return classes.get(RANDOM.nextInt(classes.size()));
    }

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
}
