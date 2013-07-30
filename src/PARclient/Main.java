package PARclient;

import com.jme3.system.AppSettings;

public class Main extends GameClient {

    public static void main(String[] args) {
        Main app = new Main();
        
        
        
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280,720);
        settings.setFrameRate(90);
	settings.setTitle("PAR -pre-Alpha");
        //settings.setTitle("Post-Apoc:ren   - the Post Apocalyptic Rennaissance!");
	
        app.setSettings(settings);
	app.setPauseOnLostFocus(true);        
        
        LightManager lightManager = new LightManager(app);
        MapManager mapManager = new MapManager(app);
        app.setMapManager(mapManager);
        app.setLightManager(lightManager);
        
        app.start();
    }
}
