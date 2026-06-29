package dev.creoii.dungeoneer.definitions;

import java.time.LocalDateTime;

public record DungeonMapDefinition(long id, long accountId, String templateId, String tilesetId, LocalDateTime lastEditDate) {
    public DungeonMapDefinition withTemplateId(String templateId) {
        return new DungeonMapDefinition(id, accountId, templateId, tilesetId, lastEditDate);
    }
    public DungeonMapDefinition withTilesetId(String tilesetId) {
        return new DungeonMapDefinition(id, accountId, templateId, tilesetId, lastEditDate);
    }
}
