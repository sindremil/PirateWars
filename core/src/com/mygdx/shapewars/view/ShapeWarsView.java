package com.mygdx.shapewars.view;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.shapewars.config.Launcher;
import com.mygdx.shapewars.model.ShapeWarsModel;
import com.mygdx.shapewars.controller.ShapeWarsController;
import com.mygdx.shapewars.model.components.ComponentMappers;
import com.mygdx.shapewars.model.components.SpriteComponent;
import com.badlogic.gdx.maps.tiled.TiledMap;

public class ShapeWarsView implements Screen {
    private OrthogonalTiledMapRenderer mapRenderer;
    private ShapeRenderer shapeRenderer;
    private final Stage stage;
    private final ShapeWarsModel model;
    private final TiledMap map;
    private FitViewport fitViewport;
    private Sprite backgroundSprite;
    private ExtendViewport extendViewport;
    private ShapeWarsController controller;

    public ShapeWarsView(ShapeWarsModel model) {
        this.model = model;
        this.stage = new Stage(); // todo check if we need to change that
        map = model.getMap();
        fitViewport = model.fitViewport;
    }

    public void setController(ShapeWarsController controller) {
        this.controller = controller;
    }

    @Override
    public void show() {
        // create a render object to easily render all layers, objects, etc. of our TileMap
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        shapeRenderer = new ShapeRenderer();

        // creation and setting of map to make sure dimensions are set right and whole map is shown
        //OrthographicCamera camera = new OrthographicCamera();
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);
        /*camera.setToOrtho(false, mapWidth, mapHeight);
        camera.update();

        // fitViewport scales the game world to fit on screen with the correct dimensions
        fitViewport = new FitViewport(mapWidth, mapHeight, camera);*/
        model.getJoystick().setJoystick(100, 100, 100, 50);
        model.getFirebutton().setFirebutton(fitViewport.getWorldWidth()-100, 100, 50);
        // extendViewport allows for a scalable background that shows when fitViewport doesn't use the whole screen
        extendViewport = new ExtendViewport(mapWidth, mapHeight);


        // Background that shows around the actual playing field
        Texture background = new Texture(Gdx.files.internal("maps/mapExpansionGrass.png"));
        backgroundSprite = new Sprite(background);

        Gdx.input.setInputProcessor(stage);
        render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int numLayers = map.getLayers().size();
        int[] layers = new int[numLayers];
        for (int i = 0; i < numLayers; i++) {
            layers[i] = i;
        }

        // drawing of the background, first sets the view to the extendViewport
        extendViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mapRenderer.setView((OrthographicCamera) extendViewport.getCamera());
        mapRenderer.getBatch().begin();
        backgroundSprite.setSize(extendViewport.getWorldWidth(), extendViewport.getWorldHeight());
        backgroundSprite.setPosition(-extendViewport.getWorldWidth() / 2, -extendViewport.getWorldHeight() / 2);
        backgroundSprite.draw(mapRenderer.getBatch());
        mapRenderer.getBatch().end();

        // drawing of actual map, therefore setting view back to fitViewpoint
        fitViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mapRenderer.setView((OrthographicCamera) fitViewport.getCamera());
        mapRenderer.render(layers);
        mapRenderer.getBatch().begin();
        for (int i = 0; i < model.engine.getEntities().size(); i++) {
            // TODO access components without entities
            Entity entity = model.engine.getEntities().get(i);
            SpriteComponent spriteComponent = ComponentMappers.sprite.get(entity);
            spriteComponent.getSprite().draw(mapRenderer.getBatch());
        }
        mapRenderer.getBatch().end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (model.launcher == Launcher.Mobile) {
            // draw joystick
            shapeRenderer.setProjectionMatrix(fitViewport.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(new Color(135 / 255f, 206 / 255f, 235 / 255f, 0.3f));
            shapeRenderer.circle(model.getJoystick().getOuterCircle().x, model.getJoystick().getOuterCircle().y, model.getJoystick().getOuterCircle().radius);
            shapeRenderer.setColor(new Color(0, 0, 139 / 255f, 0.3f));
            shapeRenderer.circle(model.getJoystick().getInnerCircle().x, model.getJoystick().getInnerCircle().y, model.getJoystick().getInnerCircle().radius);

            // draw fireButton
            shapeRenderer.circle(model.getFirebutton().getOuterCircle().x, model.getFirebutton().getOuterCircle().y, model.getFirebutton().getOuterCircle().radius);
        }
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        fitViewport.update(width, height);
        extendViewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
    }
}
