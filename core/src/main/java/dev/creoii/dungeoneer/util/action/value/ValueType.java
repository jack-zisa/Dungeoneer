package dev.creoii.dungeoneer.util.action.value;

public enum ValueType {
    HEALTH(DataType.FLOAT),
    CHARACTER(DataType.ENTITY);

    private final DataType dataType;

    ValueType(DataType dataType) {
        this.dataType = dataType;
    }

    public DataType getDataType() {
        return dataType;
    }
}
