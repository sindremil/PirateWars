package com.mygdx.shapewars.model.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.shapewars.config.Launcher;
import com.mygdx.shapewars.config.Role;
import com.mygdx.shapewars.controller.Firebutton;
import com.mygdx.shapewars.controller.Joystick;
import com.mygdx.shapewars.network.client.ClientConnector;
import java.util.ArrayList;
import java.util.List;

public class SystemFactory {
    public static List<EntitySystem> generateSystems(Role role, Launcher launcher, Joystick joystick, Firebutton fireButton, ClientConnector clientConnector, String clientId, ArrayList<Polygon> obstacles, FitViewport fitViewport) {
        List<EntitySystem> systems = new ArrayList<>();

        systems.add(SpriteSystem.getInstance());
        systems.add(launcher == Launcher.Desktop ?
                InputSystemDesktop.getInstance(role, clientConnector, clientId) :
                InputSystemMobile.getInstance(role, clientConnector, clientId, joystick, fireButton, fitViewport));
        // TODO vielleicht firebutton entfernen als Klasse

        if (role == Role.Server) {
            systems.add(MovementSystem.getInstance(obstacles));
            systems.add(RicochetSystem.getInstance(obstacles));
            systems.add(DeathSystem.getInstance());
        }
        return systems;
    }
}
