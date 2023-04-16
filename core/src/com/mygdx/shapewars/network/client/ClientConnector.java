package com.mygdx.shapewars.network.client;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
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

public class ClientConnector implements ConnectorStrategy {

    private Client client;
    private Kryo kryo;
    private ShapeWarsModel model;
    private String ipAddress;

    public ClientConnector(ShapeWarsModel model, String ipAddress) {
        this.client = new com.esotericsoftware.kryonet.Client();
        this.kryo = new Kryo();
        this.model = model;
        this.ipAddress = ipAddress;
    }

    public void startConnection() {
        this.client.start();

        try {
            client.connect(5000, this.ipAddress, 25444, 25666);
        } catch (IOException e) {
            e.printStackTrace();
            this.model.controller.setScreen(new MainMenuView(model.controller));
            return;
        }

        kryo = client.getKryo();

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

        client.addListener(new ClientListener(model));
    }
    public void sendInputRequest(String clientId, float valueInput, float directionInput, boolean firingFlag) {
        client.sendUDP(new InputRequest(clientId, valueInput, directionInput, firingFlag));
    }

    public void sendLobbyRequest(String clientId) {
        client.sendUDP(new LobbyRequest(clientId));
    }
}
