package com.mygdx.shapewars.model.components;

import com.badlogic.ashley.core.Component;

public class VelocityComponent implements Component{
  private float value;
  private float direction;

  public VelocityComponent(float value, float direction) {
    this.value = value;
    this.direction = direction;
  }

  public VelocityComponent() {} // needed for kryonet deserialization

  public float getValue() {
    return this.value;
  }

  public float getDirection() {
    return this.direction;
  }

  public void setVelocity(float v, float d) {
    this.value = v;
    this.direction = d;
  }

  public void setDirection(float d) {
    this.setVelocity(value, d);
  }
}
