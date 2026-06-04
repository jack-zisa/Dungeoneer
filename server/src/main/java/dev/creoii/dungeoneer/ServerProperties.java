package dev.creoii.dungeoneer;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record ServerProperties(
    int tcpPort, int udpPort,
    Path runDirectory
) {
    public void write(Path path) {
        try {
            DungeoneerServer.LOGGER.info("Writing server.properties");
            Files.writeString(path.resolve("server.properties"), String.format("""
                tcp_port=%s
                udp_port=%s
                run_directory=%s
                """, tcpPort, udpPort, runDirectory.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static ServerProperties read(Path runPath, Path path) {
        DungeoneerServer.LOGGER.info("Reading server.properties");
        Properties properties = new Properties();

        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);

            int tcpPort;
            try {
                tcpPort = Integer.parseInt(properties.getProperty("tcp_port"));
            } catch (NumberFormatException e) {
                tcpPort = DungeoneerServer.DEFAULT_TCP_PORT;
            }

            int udpPort;
            try {
                udpPort = Integer.parseInt(properties.getProperty("udp_port"));
            } catch (NumberFormatException e) {
                udpPort = DungeoneerServer.DEFAULT_UDP_PORT;
            }

            return new ServerProperties(tcpPort, udpPort, runPath);
        } catch (IOException e) {
            DungeoneerServer.LOGGER.error("Error reading server.properties: %s", path.toString());
            return null;
        }
    }
}
