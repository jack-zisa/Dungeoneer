package dev.creoii.dungeoneer;

import java.io.IOException;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        int tcpPort = 54555;
        int udpPort = 54777;
        boolean debug = false;

        if (args.length > 0) {
            try {
                tcpPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                DungeoneerServer.LOGGER.error("Invalid TCP port. Using default: " + tcpPort);
            }
        }

        if (args.length > 1) {
            try {
                udpPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                DungeoneerServer.LOGGER.error("Invalid UDP port. Using default: " + udpPort);
            }
        }

        if (args.length > 2 && "true".equalsIgnoreCase(args[2])) {
            debug = true;
        }

        if (tcpPort == udpPort)
            throw new IllegalArgumentException("TCP & UDP ports cannot be the same");

        new DungeoneerServer(new ServerProperties(tcpPort, udpPort, debug));
    }
}
