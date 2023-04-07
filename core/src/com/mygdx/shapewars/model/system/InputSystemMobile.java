package com.mygdx.shapewars.model.system;

import static com.mygdx.shapewars.config.GameConfig.MAX_SPEED;
import static com.mygdx.shapewars.config.GameConfig.MAX_TURN_RATE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.shapewars.config.GameConfig;
import com.mygdx.shapewars.controller.Firebutton;
import com.mygdx.shapewars.controller.Joystick;
import com.mygdx.shapewars.network.client.ClientConnector;
import com.mygdx.shapewars.config.Role;

public class InputSystemMobile extends InputSystem {
    private Joystick joystick;
    private Firebutton firebutton;
    private FitViewport fitViewport;
    private final int outerCircleRadius;
    private boolean movingJoystick;
    private static volatile InputSystemMobile instance;

    private InputSystemMobile(Role role, ClientConnector clientConnector, String clientId, Joystick joystick, Firebutton firebutton, FitViewport fitViewport) {
        super(role, clientConnector, clientId);
        this.joystick = joystick;
        this.firebutton = firebutton;
        this.outerCircleRadius = Math.round(joystick.getOuterCircle().radius);
        this.fitViewport = fitViewport;
    }

    // todo: eventuell firebutton entfernen
    public static InputSystemMobile getInstance(Role role, ClientConnector clientConnector, String clientId, Joystick joystick, Firebutton firebutton, FitViewport fitViewport) {
        if (instance == null) {
            synchronized (InputSystemMobile.class) {
                if (instance == null) {
                    instance = new InputSystemMobile(role, clientConnector, clientId, joystick, firebutton, fitViewport);
                }
            }
        }
        return instance;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 worldCoordinates = new Vector2(screenX, screenY);
        fitViewport.unproject(worldCoordinates);

        movingJoystick = joystick.getOuterCircle().contains(worldCoordinates);

        if (firebutton.getOuterCircle().contains(worldCoordinates))
            firing = true;


        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        joystick.getInnerCircle().setPosition(joystick.getOuterCircle().x, joystick.getOuterCircle().y);
        inputValue = 0;
        inputDirection = 0;
        movingJoystick = false;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector2 worldCoordinates = new Vector2(screenX, screenY);
        fitViewport.unproject(worldCoordinates);

        if (!movingJoystick)
            return false;

        float deltaX = worldCoordinates.x - joystick.getOuterCircle().x; // todo sophie why did i need the 275 here? something with the map borders?
        float deltaY = worldCoordinates.y - joystick.getOuterCircle().y;

        deltaX = MathUtils.clamp(deltaX, -outerCircleRadius, outerCircleRadius);
        deltaY = MathUtils.clamp(deltaY, -outerCircleRadius, outerCircleRadius);

        joystick.getInnerCircle().setPosition(joystick.getOuterCircle().x + deltaX, joystick.getOuterCircle().y + deltaY);

        inputDirection = -deltaX / outerCircleRadius * MAX_TURN_RATE;
        inputValue = deltaY / outerCircleRadius * MAX_SPEED;
        return false;
    }

    /*
     * todo: decide on joystick implementation, add firing button
     */
}
