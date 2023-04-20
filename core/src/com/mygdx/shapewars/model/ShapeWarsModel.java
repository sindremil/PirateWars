package com.mygdx.shapewars.model;

import static com.mygdx.shapewars.config.GameConfig.BULLET_OBSTACLE_LAYER;
import static com.mygdx.shapewars.config.GameConfig.ENEMY_FULL_HEALTH;
import static com.mygdx.shapewars.config.GameConfig.PLAYER_FULL_HEALTH;
import static com.mygdx.shapewars.config.GameConfig.SHIP_HEIGHT;
import static com.mygdx.shapewars.config.GameConfig.SHIP_OBSTACLE_LAYER;
import static com.mygdx.shapewars.config.GameConfig.SHIP_WIDTH;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.shapewars.config.Launcher;
import com.mygdx.shapewars.controller.Joystick;
import com.mygdx.shapewars.controller.ShapeWarsController;
import com.mygdx.shapewars.model.components.HealthComponent;
import com.mygdx.shapewars.model.components.IdentityComponent;
import com.mygdx.shapewars.model.components.PositionComponent;
import com.mygdx.shapewars.model.components.SpriteComponent;
import com.mygdx.shapewars.model.components.VelocityComponent;
import com.mygdx.shapewars.config.Role;
import com.mygdx.shapewars.model.helperSystems.PirateWarsSystem;
import com.mygdx.shapewars.model.helperSystems.UpdateSystem;
import com.mygdx.shapewars.model.systems.UpdateSystemClient;
import com.mygdx.shapewars.model.systems.UpdateSystemServer;
import com.mygdx.shapewars.network.ConnectorStrategy;
import com.mygdx.shapewars.network.client.ClientConnector;
import com.mygdx.shapewars.network.server.ServerConnector;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.shapewars.controller.Firebutton;
import com.mygdx.shapewars.view.ShapeWarsView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShapeWarsModel {
    public int shipId, numPlayers;
    public boolean isGameActive, createEntitiesFlag, isWorldGenerated;
    public static Engine engine;
    private static TiledMap map;
    public Role role;
    public ConnectorStrategy connectorStrategy;
    public HashMap<String, Integer> deviceShipMapping = new HashMap<>();
    public Joystick joystickShip, joystickGun;
    public Firebutton firebutton;
    public ArrayList<Polygon> shipObstacles, bulletObstacles;
    public FitViewport shapeWarsViewport;
    public GameModel gameModel;
    public ShapeWarsController controller;
    public UpdateSystem updateSystemStrategy;
    public String selectedMap;
    public InputMultiplexer multiplexer;
    public Sprite aimHelp;
    public List<PirateWarsSystem> systems;

    public ShapeWarsModel(ShapeWarsController controller, GameModel gameModel, Role role, String serverIpAddress, String selectedMap) {
        this.controller = controller;
        this.role = role;
        this.gameModel = gameModel;
        this.selectedMap = selectedMap;
        this.engine = new Engine();

        if (this.role == Role.Server) {
            this.shipId = 0;
            this.deviceShipMapping = new HashMap<>();
            deviceShipMapping.put(this.gameModel.deviceId, shipId);
            this.connectorStrategy = new ServerConnector(this);
            this.generateWorld(); // this happens in update() for the client as it needs to wait for the map
        } else {
            this.connectorStrategy = new ClientConnector(this, serverIpAddress);
        }
    }

    public void generateWorld() {
        this.multiplexer = new InputMultiplexer();

        TmxMapLoader loader = new TmxMapLoader();
        map = loader.load(selectedMap.isEmpty() ? "maps/pirateMap.tmx" : this.selectedMap);

        OrthographicCamera camera = new OrthographicCamera();
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);
        camera.setToOrtho(false, mapWidth, mapHeight);
        camera.update();
        shapeWarsViewport = new FitViewport(mapWidth, mapHeight, camera);

        if (gameModel.launcher == Launcher.Mobile) {
            firebutton = new Firebutton(shapeWarsViewport.getWorldWidth() - 180, 480, 120);
            joystickShip = new Joystick(180, 180, 120, 50);
            joystickGun = new Joystick((int) shapeWarsViewport.getWorldWidth() - 180, 180, 120, 50);
        }

        shipObstacles = getLayerObstacles(SHIP_OBSTACLE_LAYER);
        bulletObstacles = getLayerObstacles(BULLET_OBSTACLE_LAYER);
    }

    public void generateEntities() {
        if (this.role == Role.Server) {
            numPlayers = deviceShipMapping.size();
            // todo maybe move this further up?
            this.updateSystemStrategy = UpdateSystemServer.getInstance(this);
            isGameActive = true;
        } else {
            this.updateSystemStrategy = UpdateSystemClient.getInstance(this);
        }
        engine.addSystem(updateSystemStrategy);

        TiledMapTileLayer spawnLayer = (TiledMapTileLayer) map.getLayers().get(3);
        List<Vector2> spawnCells = new ArrayList<>();
        for (int y = 0; y < spawnLayer.getHeight(); y++) {
            for (int x = 0; x < spawnLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = spawnLayer.getCell(x, y);
                if (cell != null) {
                    spawnCells.add(new Vector2(x, y));
                }
            }
        }
        for (int i = 0; i < numPlayers; i++) {
            Entity ship = new Entity();
            Vector2 cell = spawnCells.get(i);
            ship.add(new PositionComponent(cell.x * spawnLayer.getTileWidth(), cell.y * spawnLayer.getTileHeight()));
            ship.add(new VelocityComponent(0, 0, 0));
            ship.add(new SpriteComponent(i == shipId ? PLAYER_FULL_HEALTH : ENEMY_FULL_HEALTH, SHIP_WIDTH, SHIP_HEIGHT)); // todo give own ship its own color
            ship.add(new HealthComponent(100));
            ship.add(new IdentityComponent(i));
            engine.addEntity(ship);
        }

        this.systems = SystemFactory.generateSystems(this);
        for (EntitySystem system : systems) {
            engine.addSystem(system);
        }

        this.aimHelp = new Sprite(new Texture("player_flag.png"));
        aimHelp.setSize(50, 20);
        aimHelp.setOrigin(-100, 0);
    }

    public void update() {
        if (role == Role.Client) {
            if (!isGameActive) {
                connectorStrategy.sendLobbyRequest(gameModel.deviceId);
            }

            if (!this.selectedMap.isEmpty() && !this.isWorldGenerated) {
                System.out.println(this.selectedMap);
                generateWorld();
                isWorldGenerated = true;
            }

            if (createEntitiesFlag) {
                createEntitiesFlag = false;
                controller.getScreen().dispose();
                controller.setScreen(new ShapeWarsView(controller));
                generateEntities();
            }
        }
        engine.update(Gdx.graphics.getDeltaTime());
    }

    public static void addToEngine(Entity entity) {
      engine.addEntity(entity);
    }

    public static TiledMap getMap() {
        return map;
    }

    public Joystick getJoystickShip() {
        return joystickShip;
    }

    public Joystick getJoystickGun() {
        return joystickGun;
    }

    public Firebutton getFirebutton() {
        return firebutton;
    }

    public ArrayList<Polygon> getLayerObstacles(int layer) {
        ArrayList<Polygon> layerObstacles = new ArrayList<>();
        for (MapObject object : map.getLayers().get(layer).getObjects()) {
            if (object instanceof PolygonMapObject) {
                Polygon rect = ((PolygonMapObject) object).getPolygon();
                layerObstacles.add(rect);
            }
        }
        return layerObstacles;
    }

    public void dispose() {
        for (PirateWarsSystem system : systems) {
            system.dispose();
        }
        engine.removeAllSystems();
        engine.removeAllEntities();
        try {
            connectorStrategy.dispose();
        } catch (IOException e) {}
    }
}
