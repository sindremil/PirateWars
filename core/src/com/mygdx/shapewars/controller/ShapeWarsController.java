package com.mygdx.shapewars.controller;

import com.badlogic.gdx.Game;
import com.mygdx.shapewars.config.Launcher;
import com.mygdx.shapewars.config.Role;
import com.mygdx.shapewars.model.GameModel;
import com.mygdx.shapewars.model.ShapeWarsModel;
import com.mygdx.shapewars.view.MainMenuView;
import com.mygdx.shapewars.view.ShapeWarsView;

public class ShapeWarsController extends Game {

    public ShapeWarsModel shapeWarsModel;
    public GameModel gameModel;
    public Launcher launcher;

    public ShapeWarsController(Launcher launcher) {
        this.launcher = launcher;
    }

    public void create() {
        this.gameModel = new GameModel(launcher);
        this.setScreen(new MainMenuView(this));
    }

    public void render() {
        if (this.screen instanceof ShapeWarsView) {
            shapeWarsModel.update();
        }
        super.render();
    }

    public void generateShapeWarsModel(Role role, String serverIpAddress, String selectedMap) {
        this.shapeWarsModel = new ShapeWarsModel(this, this.gameModel, role, serverIpAddress, selectedMap);
    }

    public void dispose() {
        gameModel.batch.dispose();
    }
}
