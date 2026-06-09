package dev.creoii.dungeoneer.client.option;

public class BooleanOption implements Option<Boolean> {
    private final String key;
    private boolean value;

    public BooleanOption(String key) {
        this.key = key;
    }

    public BooleanOption(String key, boolean defaultValue) {
        this.key = key;
        value = defaultValue;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Boolean value() {
        return value;
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }
}
