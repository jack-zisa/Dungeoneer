package dev.creoii.dungeoneer;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Properties;

public record ServerSecrets(String pepper) {
    public ServerSecrets() {
        this(generatePepper());
    }

    public void write(Path path) {
        try {
            DungeoneerServer.LOGGER.info("Writing secrets.properties");
            Files.writeString(path.resolve("secrets.properties"), String.format("""
                pepper=%s
                """, pepper));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static ServerSecrets read(Path path) {
        DungeoneerServer.LOGGER.info("Reading secrets.properties");
        Properties properties = new Properties();

        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);

            String pepper = properties.getProperty("pepper");
            if (pepper == null) {
                return null;
            }

            return new ServerSecrets(pepper);
        } catch (IOException e) {
            DungeoneerServer.LOGGER.error("Error reading secrets.properties: %s", path.toString());
            return null;
        }
    }

    private static String generatePepper() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
