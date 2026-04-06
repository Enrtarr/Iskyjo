package com.neuilleprime.gui.utils;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScreenManager {
    private final Stage stage;
    private final Map<String, Scene> scenes = new HashMap<>();

    public ScreenManager(Stage stage) {
        this.stage = stage;
    }

    public void register(String name, Scene scene) {
        this.scenes.put(name, scene);
    }

    public void show(String name) {
        Scene scene = this.scenes.get(name);
        if (scene == null) {throw new IllegalArgumentException("Scène inconnue : " + name);}
        this.stage.setScene(scene);
        this.stage.setTitle("Iskyjo - " + name);
    }
}
