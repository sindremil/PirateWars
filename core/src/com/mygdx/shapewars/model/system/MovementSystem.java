package com.mygdx.shapewars.model.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.shapewars.model.components.ComponentMappers;
import com.mygdx.shapewars.model.components.IdentityComponent;
import com.mygdx.shapewars.model.components.PositionComponent;
import com.mygdx.shapewars.model.components.SpriteComponent;
import com.mygdx.shapewars.model.components.VelocityComponent;

import java.util.ArrayList;

public class MovementSystem extends EntitySystem {
	private ImmutableArray<Entity> entities;
	private ArrayList<Polygon> obstacles;

	private static volatile MovementSystem instance;

	private MovementSystem(ArrayList<Polygon> obstacles) {
		this.obstacles = obstacles;
	}

	public void addedToEngine(Engine engine) {
		entities = engine.getEntitiesFor(
				Family.all(PositionComponent.class, VelocityComponent.class, SpriteComponent.class, IdentityComponent.class).get());
	}

	// todo remove everything sprite here, move to sprite system
	public void update(float deltaTime) {
		for (int i = 0; i < entities.size(); i++) {
			Entity entity = entities.get(i);
			PositionComponent position = ComponentMappers.position.get(entity);
			VelocityComponent velocity = ComponentMappers.velocity.get(entity);
			SpriteComponent sprite = ComponentMappers.sprite.get(entity);

			// calculate old and new position values
			float radians = MathUtils.degreesToRadians * velocity.getDirection();

			float newX = position.getPosition().x + MathUtils.cos(radians) * velocity.getValue();
			float newY = position.getPosition().y + MathUtils.sin(radians) * velocity.getValue();

			// update position and rotation
			// TODO: hier kommt ein weiterer Parameter dazu
			Vector2 overlapVector = CollisionSystem.getCollisionWithWall(entity, obstacles, newX, newY);
			newX += overlapVector.x;
			newY += overlapVector.y;
			position.setPosition(newX, newY);
			sprite.getHitbox().setPosition(newX, newY);
		}
	}

	public static MovementSystem getInstance(ArrayList<Polygon> obstacles) {
		if (instance == null) {
			synchronized (MovementSystem.class) {
				if (instance == null) {
					instance = new MovementSystem(obstacles);
				}
			}
		}
		return instance;
	}
}
