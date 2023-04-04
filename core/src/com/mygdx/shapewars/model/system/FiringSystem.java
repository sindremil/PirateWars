package com.mygdx.shapewars.model.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.mygdx.shapewars.model.ShapeWarsModel;
import com.mygdx.shapewars.model.components.ComponentMappers;
import com.mygdx.shapewars.model.components.HealthComponent;
import com.mygdx.shapewars.model.components.PositionComponent;
import com.mygdx.shapewars.model.components.SpriteComponent;
import com.mygdx.shapewars.model.components.VelocityComponent;

public class FiringSystem extends EntitySystem {
  
  private static volatile FiringSystem instance;

  private FiringSystem() {};

  public static void spawnBullet(Entity entity) {
    SpriteComponent spriteComponent = ComponentMappers.sprite.get(entity);
    PositionComponent positionComponent = ComponentMappers.position.get(entity);
    VelocityComponent velocityComponent = ComponentMappers.velocity.get(entity);
    Entity bullet = new Entity();
    int distanceFromTank = 50;
    float rotation = (float) Math.toRadians(spriteComponent.getSprite().getRotation());
    float x = (float) (positionComponent.getPosition().x + (spriteComponent.getSprite().getWidth() / 2)
        + (distanceFromTank * Math.cos(rotation)));
    float y = (float) (positionComponent.getPosition().y + (spriteComponent.getSprite().getHeight() / 2)
        + (distanceFromTank * Math.sin(rotation)));
    bullet.add(new PositionComponent(x, y));
    bullet.add(new VelocityComponent(10, velocityComponent.getDirection()));
    bullet.add(new SpriteComponent("tank_graphics.png", 10, 10));
    bullet.add(new HealthComponent(3));
    ShapeWarsModel.addToEngine(bullet);
  }

  public static FiringSystem getInstance() {
		if (instance == null) {
			synchronized (FiringSystem.class) {
				if (instance == null) {
					instance = new FiringSystem();
				}
			}
		}
		return instance;
	}
}