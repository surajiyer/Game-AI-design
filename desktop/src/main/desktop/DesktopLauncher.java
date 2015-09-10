package main.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import main.Basic3DTest;

public class DesktopLauncher {
    public static void main (String[] arg) {
	LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Drop";
        config.width = 1280;
        config.height = 720;
        //config.vSyncEnabled = true;
        config.resizable = false;
        new LwjglApplication(new Basic3DTest(), config);
    }
}
