package dev.creoii.dungeoneer.util.collision;

import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import dev.creoii.dungeoneer.definitions.sided.Raid;

import java.util.*;
import java.util.stream.Collectors;

public class EntityCollisionManager {
    private static final boolean[][] MATRIX = buildMatrix();
    private final Raid<?, ?, ?, ?> raid;
    private final Map<EntityCollisionLayer, List<Collidable>> collidables;
    private final Set<CollisionPair> activeCollisions;

    public EntityCollisionManager(Raid<?, ?, ?, ?> raid) {
        this.raid = raid;
        collidables = new HashMap<>();
        activeCollisions = new HashSet<>();
    }

    public Map<EntityCollisionLayer, List<Collidable>> getCollidables() {
        return collidables;
    }

    public void update() {
        List<Collidable> characters = raid.getCharacters().values().stream().collect(Collectors.toUnmodifiableList());
        collidables.put(EntityCollisionLayer.CHARACTER, characters);

        List<Collidable> enemyBullets = new ArrayList<>();
        List<Collidable> characterBullets = new ArrayList<>();
        collectBullets(enemyBullets, characterBullets);
        collidables.put(EntityCollisionLayer.ENEMY_BULLET, enemyBullets);
        collidables.put(EntityCollisionLayer.CHARACTER_BULLET, characterBullets);

        Set<CollisionPair> currentCollisions = new HashSet<>();
        EntityCollisionLayer[] layers = EntityCollisionLayer.values();
        for (int i = 0; i < layers.length; i++) {
            EntityCollisionLayer layerA = layers[i];
            List<Collidable> listA = collidables.getOrDefault(layerA, Collections.emptyList());

            for (int j = i; j < layers.length; j++) {
                EntityCollisionLayer layerB = layers[j];
                if (!MATRIX[layerA.ordinal()][layerB.ordinal()]) {
                    continue;
                }

                List<Collidable> listB = collidables.getOrDefault(layerB, Collections.emptyList());
                if (i != j) {
                    for (Collidable a : listA) {
                        for (Collidable b : listB) {
                            if (a != b && a.getBounds().overlaps(b.getBounds())) {
                                CollisionPair pair = new CollisionPair(a, b);

                                currentCollisions.add(pair);
                                if (!activeCollisions.contains(pair)) {
                                    a.onCollisionEnter(b);
                                    b.onCollisionEnter(a);
                                }

                                a.onCollision(b);
                                b.onCollision(a);
                            }
                        }
                    }
                }
            }
        }

        for (CollisionPair pair : activeCollisions) {
            if (!currentCollisions.contains(pair)) {
                pair.a().onCollisionExit(pair.b());
                pair.b().onCollisionExit(pair.a());
            }
        }

        activeCollisions.clear();
        activeCollisions.addAll(currentCollisions);

        collidables.clear();
    }

    private static boolean[][] buildMatrix() {
        boolean[][] matrix = new boolean[EntityCollisionLayer.values().length][EntityCollisionLayer.values().length];

        for (int i = 0; i < EntityCollisionLayer.values().length; ++i) {
            Arrays.fill(matrix[i], false);
        }

        matrix[EntityCollisionLayer.ENEMY_BULLET.ordinal()][EntityCollisionLayer.CHARACTER.ordinal()] = true;
        matrix[EntityCollisionLayer.CHARACTER.ordinal()][EntityCollisionLayer.ENEMY_BULLET.ordinal()] = true;
        matrix[EntityCollisionLayer.CHARACTER_BULLET.ordinal()][EntityCollisionLayer.ENEMY.ordinal()] = true;
        matrix[EntityCollisionLayer.ENEMY.ordinal()][EntityCollisionLayer.CHARACTER_BULLET.ordinal()] = true;

        return matrix;
    }

    private void collectBullets(List<Collidable> enemyBullets, List<Collidable> characterBullets) {
        raid.getEntityManager().getEntities().forEach(entity -> {
            if (entity instanceof Bullet<?> bullet) {
                if (bullet.isEnemy()) enemyBullets.add(bullet);
                else characterBullets.add(bullet);
            } else if (entity instanceof BulletGroup<?> bulletGroup) collectBulletGroup(bulletGroup, enemyBullets, characterBullets);
        });
    }

    private void collectBulletGroup(BulletGroup<?> bulletGroup, List<Collidable> enemyBullets, List<Collidable> characterBullets) {
        bulletGroup.getChildren().forEach(bulletNode -> {
            if (bulletNode instanceof Bullet<?> bullet) {
                if (bullet.isEnemy()) enemyBullets.add(bulletNode);
                else characterBullets.add(bulletNode);
            } else if (bulletNode instanceof BulletGroup<?> bulletGroup1) collectBulletGroup(bulletGroup1, enemyBullets, characterBullets);
        });
    }

    public record CollisionPair(Collidable a, Collidable b) {
    }
}
