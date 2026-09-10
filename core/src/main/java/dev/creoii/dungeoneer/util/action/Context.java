package dev.creoii.dungeoneer.util.action;

import dev.creoii.dungeoneer.util.action.value.ObjectValue;
import dev.creoii.dungeoneer.util.action.value.Value;
import dev.creoii.dungeoneer.util.action.value.ValueType;

import java.util.HashMap;
import java.util.Map;

public class Context {
    public final Map<ValueType, Value<?>> values;

    public Context() {
        values = new HashMap<>();
    }

    public boolean has(ValueType valueType) {
        return values.containsKey(valueType);
    }

    public boolean has(ValueType... valueTypes) {
        for (ValueType valueType : valueTypes) {
            if (!values.containsKey(valueType)) return false;
        }
        return true;
    }

    public Context set(ValueType valueType, Object value) {
        Object converted = valueType.getDataType().convert(value);
        values.put(valueType, new ObjectValue<>(converted));
        return this;
    }

    public Context remove(ValueType valueType) {
        values.remove(valueType);
        return this;
    }

    @SuppressWarnings("unchecked") // TODO: Don't do hacky shit like this, or at least do some safety checks
    public <T> T get(ValueType valueType) {
        return (T) values.get(valueType).get();
    }
}
