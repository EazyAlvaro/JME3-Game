package PARclient;

import PARtest.*;
import com.jme3.system.AppSettings;
import com.jme3.system.AppSettings.*;


/**
 * test
 * @author normenhansen
 */
public class Main extends GameClient {

    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280,720);
        //settings.setFrameRate(30);
	settings.setTitle("PAR -pre-Alpha");
        //settings.setTitle("Post-Apoc:ren   - the Post Apocalyptic Rennaissance!");
	
        app.setSettings(settings);
	app.setPauseOnLostFocus(true);
        app.start();
    }

   
}
