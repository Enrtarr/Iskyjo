package com.neuilleprime.gui.utils;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Manages named {@link Scene}s and switches between them on a single
 * {@link Stage}.
 * <p>
 * Screens are registered with {@link #register(String, Scene)} and then
 * displayed with {@link #show(String)}, which also updates the window title.
 * </p>
 */
public class ScreenManager {

    /** The primary stage on which scenes are displayed. */
    private final Stage stage;

    /** Map from screen name to its corresponding {@link Scene}. */
    private final Map<String, Scene> scenes = new HashMap<>();

    /**
     * Constructs a {@code ScreenManager} bound to the given stage.
     *
     * @param stage the primary stage to manage
     */
    public ScreenManager(Stage stage) {
        this.stage = stage;
    }

    /**
     * Registers a scene under the given name so it can later be shown.
     *
     * @param name  the unique identifier for this scene (e.g. {@code "menu"})
     * @param scene the {@link Scene} to register
     */
    public void register(String name, Scene scene) {
        this.scenes.put(name, scene);
    }

    /**
     * Switches the stage to the scene registered under the given name and
     * updates the window title to {@code "Iskyjo - <name>"}.
     *
     * @param name the name of the scene to display
     * @throws IllegalArgumentException if no scene is registered under {@code name}
     */
    public void show(String name) {
        Scene scene = this.scenes.get(name);
        if (scene == null) {throw new IllegalArgumentException("Scène inconnue : " + name);}
        this.stage.setScene(scene);
        this.stage.setTitle("Iskyjo - " + name);
    }
}
