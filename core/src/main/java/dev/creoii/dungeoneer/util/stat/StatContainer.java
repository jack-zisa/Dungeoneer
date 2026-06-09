package dev.creoii.dungeoneer.util.stat;

public record StatContainer(Stat health, Stat speed) {
    public static final StatContainer DEFAULT_STAT_CONTAINER = new StatContainer(200, 100);
    public static final StatContainer ZERO = new StatContainer();

    public StatContainer() {
        this(0, 0);
    }

    public StatContainer(int health, int speed) {
        this(new Stat(Stat.Type.HEALTH, health), new Stat(Stat.Type.SPEED, speed));
    }

    public void setSpeed(int speed) {
        this.speed.set(speed);
    }

    public void setHealth(int health) {
        this.health.set(health);
    }

    public void set(StatContainer other) {
        setHealth(other.health.base());
        setSpeed(other.speed.base());
    }

    public StatContainer copy() {
        return new StatContainer(health, speed);
    }
}
