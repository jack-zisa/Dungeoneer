package dev.creoii.dungeoneer.util;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.mojang.datafixers.kinds.Const;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class DungeonMapUtils {
    public static byte[] serializeMap(TiledMap map) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            if (map.getLayers().get(Constants.MAP_LAYER_GROUND) instanceof TiledMapTileLayer tileLayer) {
                byte[] layerBlob = serializeLayer(tileLayer);
                dos.writeInt(layerBlob.length);
                dos.write(layerBlob);
            }
        }
        return baos.toByteArray();
    }

    public static byte[] serializeLayer(TiledMapTileLayer layer) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(new DeflaterOutputStream(baos))) {
            dos.writeInt(layer.getWidth());
            dos.writeInt(layer.getHeight());
            for (int y = 0; y < layer.getHeight(); ++y) {
                for (int x = 0; x < layer.getWidth(); ++x) {
                    TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                    int tileId = cell == null || cell.getTile() == null ? 0 : cell.getTile().getId();
                    dos.writeInt(tileId);
                }
            }
        }
        return baos.toByteArray();
    }

    public static TiledMap deserializeMap(byte[] blob, TiledMapTileSet tileSet) throws IOException {
        TiledMap map = new TiledMap();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(blob))) {
            byte[] layerBlob = new byte[dis.readInt()];
            dis.readFully(layerBlob);
            TiledMapTileLayer layer = deserializeLayer(layerBlob, tileSet);
            layer.setName(Constants.MAP_LAYER_GROUND);
            map.getLayers().add(layer);
        }

        map.getTileSets().addTileSet(tileSet);
        return map;
    }

    public static TiledMapTileLayer deserializeLayer(byte[] blob, TiledMapTileSet tileSet) throws IOException {
        try (DataInputStream dis = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(blob)))) {
            int width = dis.readInt();
            int height = dis.readInt();
            TiledMapTileLayer layer = new TiledMapTileLayer(width, height, 8, 8);
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    int tileId;
                    if ((tileId = dis.readInt()) == 0)
                        continue;

                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(tileSet.getTile(tileId));
                    layer.setCell(x, y, cell);
                }
            }
            return layer;
        }
    }
}
