package dev.creoii.dungeoneer.client.game;

import dev.creoii.dungeoneer.EntityManager;
import dev.creoii.dungeoneer.definitions.sided.Entity;

import java.util.HashMap;
import java.util.Map;

public class ClientEntityManager extends EntityManager<ClientRaid> {
    private final Map<Long, Entity<ClientRaid>> predictionIdToObj = new HashMap<>();
    private long nextPredictionId;

    public ClientEntityManager(ClientRaid raid, int capacity) {
        super(raid, capacity);
    }

    public long nextPredictionId() {
        return nextPredictionId++;
    }

    public boolean addPredicted(Entity<ClientRaid> entity) {
        long predictionId = nextPredictionId();
        if (!(entity instanceof ClientEntity)) return false;
        predictionIdToObj.put(predictionId, entity);
        return getEntities().add(entity);
    }

    public Entity<ClientRaid> getPredicted(long predictionId) {
        return predictionIdToObj.get(predictionId);
    }

    public boolean authorize(long predictionId, long serverId) {
        Entity<ClientRaid> entity = predictionIdToObj.remove(predictionId);
        if (entity == null || contains(serverId)) return false;
        entity.setId(serverId);
        getIdToObj().put(serverId, entity);
        return true;
    }
}
