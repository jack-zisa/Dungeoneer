package dev.creoii.dungeoneer.util.action.value;

import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.definitions.sided.Entity;

import java.util.Random;

/**
 * Stop! Don't look at this! Let's just trust that it will work, okay! No exceptions here!
 */
public enum DataType implements Converter {
    INTEGER {
        @Override
        public Integer convert(Object input) {
            return Integer.parseInt(input.toString());
        }
    },
    STRING {
        @Override
        public String convert(Object input) {
            return input.toString();
        }
    },
    BYTE {
        @Override
        public Byte convert(Object input) {
            return Byte.parseByte(input.toString());
        }
    },
    SHORT {
        @Override
        public Short convert(Object input) {
            return Short.parseShort(input.toString());
        }
    },
    FLOAT {
        @Override
        public Float convert(Object input) {
            return Float.parseFloat(input.toString());
        }
    },
    DOUBLE {
        @Override
        public Double convert(Object input) {
            return Double.parseDouble(input.toString());
        }
    },
    LONG {
        @Override
        public Long convert(Object input) {
            return Long.parseLong(input.toString());
        }
    },
    BOOLEAN {
        @Override
        public Boolean convert(Object input) {
            return Boolean.parseBoolean(input.toString());
        }
    },
    ENTITY {
        @Override
        public Entity convert(Object input) {
            return (Entity) input;
        }
    },
    CHARACTER {
        @Override
        public Character convert(Object input) {
            return (Character) input;
        }
    },
    VEC2 {
        @Override
        public Vector2 convert(Object input) {
            return (Vector2) input;
        }
    },
    RANDOM {
        @Override
        public Random convert(Object input) {
            return (Random) input;
        }
    }
}
