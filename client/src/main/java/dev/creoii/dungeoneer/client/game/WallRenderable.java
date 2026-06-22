package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.OrthographicCamera;

public sealed interface WallRenderable permits ClientDungeonMap.WallFace, ClientDungeonMap.WallTop {
    float depth(float rotation, OrthographicCamera camera);
}
