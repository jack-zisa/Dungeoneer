package dev.creoii.dungeoneer.util.collision;

import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.sided.Raid;

import java.util.*;
import java.util.stream.Collectors;

public class CollisionManager {
    private static final boolean[][] MATRIX = buildMatrix();
    private final Raid<?, ?, ?> raid;
    private final Map<CollisionLayer, List<Collidable>> collidables;
    private final Set<CollisionPair> activeCollisions;

    public CollisionManager(Raid<?, ?, ?> raid) {
        this.raid = raid;
        collidables = new HashMap<>();
        activeCollisions = new HashSet<>();
    }

    public Map<CollisionLayer, List<Collidable>> getCollidables() {
        return collidables;
    }

    public void update() {
        List<Collidable> characters = raid.getCharacters().values().stream().collect(Collectors.toUnmodifiableList());
        collidables.put(CollisionLayer.CHARACTER, characters);

        List<Collidable> enemyBullets = raid.getBullets().values().stream().filter(bullet -> bullet instanceof Bullet bullet1 && bullet1.isEnemy()).collect(Collectors.toUnmodifiableList());
        collidables.put(CollisionLayer.ENEMY_BULLET, enemyBullets);

        List<Collidable> characterBullets = raid.getBullets().values().stream().filter(bullet -> bullet instanceof Bullet bullet1 && !bullet1.isEnemy()).collect(Collectors.toUnmodifiableList());
        collidables.put(CollisionLayer.CHARACTER_BULLET, characterBullets);

        Set<CollisionPair> currentCollisions = new HashSet<>();

        CollisionLayer[] layers = CollisionLayer.values();
        for (int i = 0; i < layers.length; i++) {
            CollisionLayer layerA = layers[i];
            List<Collidable> listA = collidables.getOrDefault(layerA, Collections.emptyList());

            for (int j = i; j < layers.length; j++) {
                CollisionLayer layerB = layers[j];
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
        boolean[][] matrix = new boolean[CollisionLayer.values().length][CollisionLayer.values().length];

        for (int i = 0; i < CollisionLayer.values().length; ++i) {
            Arrays.fill(matrix[i], false);
        }

        matrix[CollisionLayer.ENEMY_BULLET.ordinal()][CollisionLayer.CHARACTER.ordinal()] = true;
        matrix[CollisionLayer.CHARACTER.ordinal()][CollisionLayer.ENEMY_BULLET.ordinal()] = true;
        matrix[CollisionLayer.CHARACTER_BULLET.ordinal()][CollisionLayer.ENEMY.ordinal()] = true;
        matrix[CollisionLayer.ENEMY.ordinal()][CollisionLayer.CHARACTER_BULLET.ordinal()] = true;

        return matrix;
    }

    public record CollisionPair(Collidable a, Collidable b) {
    }
}
