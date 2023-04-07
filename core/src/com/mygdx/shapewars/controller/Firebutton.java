package com.mygdx.shapewars.controller;

import com.badlogic.gdx.math.Circle;

public class Firebutton {
    private final Circle outerCircle;

    public Firebutton(float screenX, float screenY, int outerCircleRadius) {
        this.outerCircle = new Circle(screenX, screenY, outerCircleRadius);
    }

    public Circle getOuterCircle() {
        return outerCircle;
    }

    public void setFirebutton(float x, float y, int radius) {
        outerCircle.setPosition(x, y);
        outerCircle.setRadius(radius);
    }


}
