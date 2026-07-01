package dev.creoii.dungeoneer.util;

public final class Constants {
    public static final int CHARACTER_MOVEMENT_FLAG_LEFT = 1;
    public static final int CHARACTER_MOVEMENT_FLAG_RIGHT = 2;
    public static final int CHARACTER_MOVEMENT_FLAG_UP = 4;
    public static final int CHARACTER_MOVEMENT_FLAG_DOWN = 8;

    public static final int MAP_HEIGHT = 256;
    public static final int MAP_WIDTH = 256;

    public static final long DEFAULT_MAP_SEED = 1337L;

    public static final String MAP_LAYER_GROUND = "ground";
    public static final String MAP_LAYER_OBJECT = "object";
    public static final String MAP_LAYER_WALL = "wall";
    public static final String MAP_LAYER_OVERLAY = "overlay";
    public static final String[] MAP_LAYERS = {MAP_LAYER_GROUND, MAP_LAYER_OBJECT, MAP_LAYER_WALL, MAP_LAYER_OVERLAY};

    private static final long RAID_DURATION_MINUTES = 5L;
    public static final long RAID_DURATION_MS = RAID_DURATION_MINUTES * 60L * 1000L;

    public static final String TEST_ATTACK = "staff";
    public static final String TEST_BULLET = "ice_magic_blade";
}
