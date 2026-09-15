package dev.creoii.dungeoneer.util.action.value;

public enum ValueType {
    HEALTH(DataType.FLOAT),
    ENTITY(DataType.ENTITY),
    POSITION(DataType.VEC2),
    RANDOM(DataType.RANDOM),
    SEED(DataType.LONG);

    private final DataType dataType;

    ValueType(DataType dataType) {
        this.dataType = dataType;
    }

    public DataType getDataType() {
        return dataType;
    }
}
