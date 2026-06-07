package dev.creoii.dungeoneer.server;

import java.io.IOException;
import java.nio.file.Path;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        Path runDirectory = Path.of(System.getProperty("user.dir"));
        ServerProperties properties = ServerProperties.read(runDirectory, runDirectory.resolve("server.properties"));
        ServerSecrets secrets = ServerSecrets.read(runDirectory.resolve("secrets.properties"));
        if (secrets == null) {
            secrets = new ServerSecrets();
            secrets.write(runDirectory.resolve("secrets.properties"));
        }

        int tcpPort = properties == null ? DungeoneerServer.DEFAULT_TCP_PORT : properties.tcpPort();
        int udpPort = properties == null ? DungeoneerServer.DEFAULT_UDP_PORT : properties.udpPort();
        boolean debug = false;

        if (args.length > 0) {
            try {
                tcpPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                DungeoneerServer.LOGGER.error("Invalid TCP port. Using default: %s", tcpPort);
            }
        }

        if (args.length > 1) {
            try {
                udpPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                DungeoneerServer.LOGGER.error("Invalid UDP port. Using default: %s", udpPort);
            }
        }

        if (args.length > 2 && "true".equalsIgnoreCase(args[2])) {
            debug = true;
            DungeoneerServer.LOGGER.debug("Set server to debug mode.");
        }

        if (tcpPort == udpPort)
            throw new IllegalArgumentException("TCP & UDP ports cannot be the same");

        DungeoneerServer server = new DungeoneerServer(new ServerProperties(tcpPort, udpPort, runDirectory), secrets, debug);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
