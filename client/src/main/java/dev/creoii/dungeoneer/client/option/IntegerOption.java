package dev.creoii.dungeoneer.client.option;

public class IntegerOption implements Option<Integer> {
    private final String key;
    private int value;

    public IntegerOption(String key) {
        this.key = key;
    }

    public IntegerOption(String key, int defaultValue) {
        this.key = key;
        value = defaultValue;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Integer value() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }
}
