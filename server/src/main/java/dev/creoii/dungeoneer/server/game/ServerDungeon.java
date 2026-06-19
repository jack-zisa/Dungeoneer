package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.definitions.Tile;
import dev.creoii.dungeoneer.server.util.ServerTiles;
import dev.creoii.dungeoneer.util.Constants;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

public class ServerDungeon {
    private final int[][] ground;
    private final int[][] walls;
    private final int[][] objects;
    private final int[][] overlays;

    public ServerDungeon(byte[] mapData) {
        ground = new int[256][256];
        walls = new int[256][256];
        objects = new int[256][256];
        overlays = new int[256][256];

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(mapData))) {
            for (int i = 0; i < Constants.MAP_LAYERS.length; i++) {
                String layerName = dis.readUTF();

                int len = dis.readInt();
                if (len == 0)
                    continue;

                byte[] layerBlob = new byte[len];
                dis.readFully(layerBlob);

                int[][] target = switch (layerName) {
                    case Constants.MAP_LAYER_GROUND -> ground;
                    case Constants.MAP_LAYER_WALL -> walls;
                    case Constants.MAP_LAYER_OBJECT -> objects;
                    case Constants.MAP_LAYER_OVERLAY -> overlays;
                    default -> throw new IOException("Unknown layer: " + layerName);
                };

                deserializeLayer(layerBlob, target);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deserializeLayer(byte[] blob, int[][] tiles) throws IOException {
        try (DataInputStream dis = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(blob)))) {

            int width = dis.readInt();
            int height = dis.readInt();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    tiles[x][y] = dis.readInt();
                }
            }
        }
    }

    public int[][] getGround() {
        return ground;
    }

    public int[][] getWalls() {
        return walls;
    }

    public int[][] getObjects() {
        return objects;
    }

    public int[][] getOverlays() {
        return overlays;
    }

    @Nullable
    public Tile getTileAt(String layer, int tileX, int tileY) {
        int id = switch (layer) {
            case Constants.MAP_LAYER_GROUND -> ground[tileX][tileY];
            case Constants.MAP_LAYER_OBJECT -> objects[tileX][tileY];
            case Constants.MAP_LAYER_WALL -> walls[tileX][tileY];
            case Constants.MAP_LAYER_OVERLAY -> overlays[tileX][tileY];
            default -> throw new IllegalArgumentException("Unknown tile layer: " + layer);
        };
        if (id == 0)
            return null;
        return ServerTiles.TILES_BY_ID.getOrDefault(id, null);
    }

    public boolean isSolid(int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= walls.length || tileY >= walls[0].length) {
            return true;
        }

        Tile tile = getTileAt(Constants.MAP_LAYER_WALL, tileX, tileY);
        return tile != null;
    }
}
