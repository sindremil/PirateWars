package com.mygdx.shapewars.network.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.shapewars.model.ShapeWarsModel;
import com.mygdx.shapewars.model.components.HealthComponent;
import com.mygdx.shapewars.model.components.IdentityComponent;
import com.mygdx.shapewars.model.components.PositionComponent;
import com.mygdx.shapewars.model.components.VelocityComponent;
import com.mygdx.shapewars.network.ConnectorStrategy;
import com.mygdx.shapewars.network.data.BulletData;
import com.mygdx.shapewars.network.data.GameResponse;
import com.mygdx.shapewars.network.data.InputRequest;
import com.mygdx.shapewars.network.data.LobbyRequest;
import com.mygdx.shapewars.network.data.LobbyResponse;
import com.mygdx.shapewars.network.data.ShipData;
import com.mygdx.shapewars.view.MainMenuView;

import java.io.IOException;

public class ServerConnector implements ConnectorStrategy {

    private Server server;
    private Kryo kryo;
    private ShapeWarsModel model;

    public ServerConnector(ShapeWarsModel model) {
        this.server = new Server();
        this.kryo = new Kryo();
        this.model = model;
    }

    public void startConnection() {
        this.server.start();

        try {
            server.bind(25444, 25666);
        } catch (IOException e) {
            e.printStackTrace();
            this.model.controller.setScreen(new MainMenuView(model.controller));
            return;
        }

        this.kryo = server.getKryo();

        // register messages
        this.kryo.register(InputRequest.class);
        this.kryo.register(GameResponse.class);
        this.kryo.register(LobbyRequest.class);
        this.kryo.register(LobbyResponse.class);

        // register containers
        this.kryo.register(BulletData.class);
        this.kryo.register(ShipData.class);
        this.kryo.register(BulletData[].class);
        this.kryo.register(ShipData[].class);

        // register components
        this.kryo.register(PositionComponent.class);
        this.kryo.register(VelocityComponent.class);
        this.kryo.register(HealthComponent.class);
        this.kryo.register(IdentityComponent.class);
        this.kryo.register(PositionComponent[].class);
        this.kryo.register(VelocityComponent[].class);
        this.kryo.register(HealthComponent[].class);
        this.kryo.register(IdentityComponent[].class);

        // register helper classes
        this.kryo.register(Array.class);
        this.kryo.register(Vector2.class);
        this.kryo.register(float[].class);

        this.server.addListener(new ServerListener(model));
    }
}
