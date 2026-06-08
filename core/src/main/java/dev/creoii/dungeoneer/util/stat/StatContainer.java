package dev.creoii.dungeoneer.util.stat;

public record StatContainer(Stat speed) {
    public static final StatContainer DEFAULT_STAT_CONTAINER = new StatContainer(100);

    public StatContainer() {
        this(0);
    }

    public StatContainer(int speed) {
        this(new Stat(Stat.Type.SPEED, speed));
    }

    public void setSpeed(int speed) {
        this.speed.set(speed);
    }

    public void set(StatContainer other) {
        setSpeed(other.speed.base());
    }

    public StatContainer copy() {
        return new StatContainer(speed);
    }
}
