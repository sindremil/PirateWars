package com.mygdx.shapewars.model.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.shapewars.model.ShapeWarsModel;
import com.mygdx.shapewars.model.components.ComponentMappers;
import com.mygdx.shapewars.model.components.IdentityComponent;
import com.mygdx.shapewars.model.components.SpriteComponent;
import com.mygdx.shapewars.model.components.VelocityComponent;
import java.util.ArrayList;

public class CollisionSystem extends EntitySystem {

    private static volatile CollisionSystem instance;

    private CollisionSystem() {
    }

    /**
     * Gets the bounds of an Entity.
     *
     * @param entity the entity one wants the bounds for.
     * @param newX   the new X value from the MovementSystem.
     * @param newY   the new Y value from the MovementSystem.
     * @return the bounds of the Entity.
     */
    private static Polygon getEntityBounds(Entity entity, float newX, float newY) {
        SpriteComponent spriteComponent = ComponentMappers.sprite.get(entity);
        VelocityComponent velocityComponent = ComponentMappers.velocity.get(entity);
        // calculate the tank's bounding box
        Polygon entityBounds = new Polygon(new float[] {
                0, 0,
                spriteComponent.getSprite().getWidth(), 0,
                spriteComponent.getSprite().getWidth(), spriteComponent.getSprite().getHeight(),
                0, spriteComponent.getSprite().getHeight()
        });
        entityBounds.setOrigin(spriteComponent.getSprite().getOriginX(), spriteComponent.getSprite().getOriginY());
        entityBounds.setPosition(newX, newY);
        entityBounds.setRotation(velocityComponent.getDirection());
        return entityBounds;
    }

    /**
     * Calculates all the entire area of a tile and returns it.
     *
     * @param x              the tile's x value.
     * @param y              the tile's y value.
     * @return the bounds of the tile as a Polygon.
     */
    private static Polygon getTileBounds(int x, int y) {
        TiledMapTileLayer collisionLayer = ShapeWarsModel.getLayer(1);
        float tileSize = collisionLayer.getTileWidth();
        Rectangle rect = new Rectangle(x * tileSize, y * tileSize, tileSize, tileSize);
        Polygon tileBounds = new Polygon(new float[] {
                rect.x, rect.y, // bottom left corner
                rect.x + rect.width, rect.y, // bottom right corner
                rect.x + rect.width, rect.y + rect.height, // top right corner
                rect.x, rect.y + rect.height // top left corner
        });
        return tileBounds;
    }

    /**
     * Makes a rectangle of the area an Entity covers.
     *
     * @param entity the entity to make a rectangle of.
     * @param x      the x position of the entity.
     * @param y      the y position of the entity.
     * @return the Rectangle of the area covered.
     */
    private static Rectangle getEntityRectangle(Entity entity, float x, float y) {
        SpriteComponent spriteComponent = ComponentMappers.sprite.get(entity);
        return new Rectangle(x, y, spriteComponent.getSprite().getWidth(), spriteComponent.getSprite().getHeight());
    }

    /**
     * Returns the minimum translation vector to separate the two Polygons.
     *
     * @param entityBounds the bounds of an entity aka perimeter of sprite.
     * @param tileBounds   the bounds of the tile.
     * @return the overlap between something... Please tell us Sophie.
     */
    private static Vector2 getOverlapVector(Polygon entityBounds, Polygon tileBounds) {
        Vector2 overlapVector = new Vector2();
        if (Intersector.overlapConvexPolygons(entityBounds, tileBounds)) {
            // tank collides with wall, adjust position
            Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
            Intersector.overlapConvexPolygons(entityBounds, tileBounds, mtv);
            overlapVector.set(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth);
        }
        return overlapVector;
    }

    /**
     * Checks for a collision between an entity and a wall.
     *
     * @param <T>            the type you want to return which must be a Rectangle
     *                       or Vector2.
     * @param entity         the entity to check collision on.
     * @param obstacles      the obstacles of the map.
     * @param newX           the new X value from the MovementSystem.
     * @param newY           the new Y value from the MovementSystem.
     * @return the overlap between something... Please tell us Sophie.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCollisionWithWall(Entity entity, ArrayList<Polygon> obstacles, float newX, float newY) {
        // check for collision with obstacles
        Polygon entityBounds = getEntityBounds(entity, newX, newY);
        Vector2 overlapVector = new Vector2();
        Rectangle entityRectangle = getEntityRectangle(entity, newX, newY);

        for (Polygon obstacle : obstacles) {
            if (obstacle != null) {
                // Checks if the entity is tank or not
                if (entity.getComponent(IdentityComponent.class) != null) {
                    Vector2 tempVector = getOverlapVector(entityBounds, obstacle);
                    overlapVector.add(tempVector.x, tempVector.y);
                } else {
                    Rectangle wallRect = new Rectangle();
                    wallRect.set(obstacle.getBoundingRectangle());
                    if (wallRect.overlaps(entityRectangle)) {
                        return (T) obstacle;
                    }
                }
            }
        }
        if (entity.getComponent(IdentityComponent.class) != null) {
            return (T) overlapVector;
        } else {
            return (T) new Polygon();
        }
    }

    public static CollisionSystem getInstance() {
        if (instance == null) {
            synchronized (CollisionSystem.class) {
                if (instance == null) {
                    instance = new CollisionSystem();
                }
            }
        }
        return instance;
    }
}
