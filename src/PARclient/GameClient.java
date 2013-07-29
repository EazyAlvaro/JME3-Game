package PARclient;

 //Physics and collission
//import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
//import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;

//For item Picking and ray-casting
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;

//Terrain, assets,  and textures
import com.jme3.terrain.geomipmap.TerrainLodControl;

import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection.*;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.noise.filter.IterativeFilter;
import com.jme3.terrain.noise.filter.OptimizedErode;
import com.jme3.terrain.noise.filter.PerturbFilter;
import com.jme3.terrain.noise.filter.SmoothFilter;
import com.jme3.terrain.noise.fractal.FractalSum;
import com.jme3.util.SkyFactory;
import java.util.Iterator;

//Misc & Controls
import PARlib.*;
import PARlib.Items.Item;
import com.jme3.input.FlyByCamera;
import com.jme3.scene.Node;
import com.jme3.scene.CameraNode;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.font.BitmapText;
import PARlib.Items.ObjectHelper;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.renderer.Camera;
import java.util.ArrayList;
import java.util.List;
import com.jme3.math.ColorRGBA;


public class GameClient extends SimpleApplication
        implements ActionListener {

    // <editor-fold defaultstate="collapsed" desc="Local Properties">

    //Physics
    private BulletAppState bulletAppState;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
    //Controls & GUI
    private CharacterControl player;
    private Geometry mark;
    public BitmapText pickText;// = new BitmapText(guiFont, false);
    public BitmapText inventorytext;
    private CameraNode camNode;
    //custom stuff
    private ArrayList WorldObjects = new ArrayList();
    private ArrayList Inventory = new ArrayList(); //TODO Inventory manager maken, invoegen in WorldObjectManager 
    private WorldObjectManager WOM = new WorldObjectManager(WorldObjects, rootNode);
    private InventoryManager INV = new InventoryManager(Inventory);
    private PlayerCharacter playerCharacter = new PlayerCharacter(WOM, INV);
    public Node pickNode = new Node("pickNode");
    //custom map stuff

    private PerturbFilter perturb;
    private OptimizedErode therm;
    private SmoothFilter smooth;
    private IterativeFilter iterate;
    
    
    private boolean showWireframe = false;
    
    private MapManager mapMan;
    
    //public LightManager lightManager;
    private GameClient app = null;

    
    // </editor-fold>
    public static void main(String[] args) {
        
        GameClient app = new GameClient();;
        // terrain here?
        LightManager lightManager = new LightManager(app);
        MapManager mapManager = new MapManager();
        app.setMapManager(mapManager);
        //lightManager.initLightning(mapManager);
        app.setShowSettings(true);
        app.start();
    }
    

    public void setMapManager(MapManager mapManager)
    {
        mapMan = mapManager;
    }
    
    
    /**
     * Initiates the app. is called by app.start()
     */
    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().enableDebug(assetManager);


        WOM = new WorldObjectManager(WorldObjects, rootNode);
        INV = new InventoryManager(Inventory);
        ObjectHelper objHelper = new ObjectHelper(bulletAppState, assetManager, WOM, rootNode);

        
        rootNode.attachChild(SkyFactory.createSky(
                assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

        // <editor-fold defaultstate="collapsed" desc="Initialisations">
        boolean gridTest = true;
        
        if (gridTest == false) { // for testing simple things without the complexity of grid-tiles around
            mapMan.initSimpleMapMaterials(assetManager);
            mapMan.initSingleMap(rootNode, bulletAppState);
            mapMan.initSingleLOD(getCamera());
        } else { //for actual in-game testing
            mapMan.initGridMaterials(assetManager);
            mapMan.initGridMap(rootNode, bulletAppState, getCamera());
        }
        
        LightManager lightManager = new LightManager(this);
        lightManager.initLightning(mapMan);
        
        initPlayer();
        initCrossHairs();
       // initMark();
        initKeys();
        // </editor-fold>

        //TODO abstract all this away to a separate function
        Spatial tp1 = objHelper.getTeapot("teapot 1", -10, 475, 0);
        Spatial tp2 = objHelper.getTeapot("teapot 2", 11, 450, -10);
        Spatial tp3 = objHelper.getTeapot("teapot 3", 5, 425, -15);

        WOM.addItem(tp1);
        WOM.addItem(tp2);
        WOM.addItem(tp3);

        rootNode.attachChild(tp1);
        rootNode.attachChild(tp2);
        rootNode.attachChild(tp3);
    }


    /**
     * configure settings and physics for the player perspective and camera
     */
    private void initPlayer() {
        System.out.println("InitPlayer");
        this.getCamera().setLocation(new Vector3f(0, 500, 0));

        CapsuleCollisionShape playerShape = new CapsuleCollisionShape(2f, 2f, 2);
        player = new CharacterControl(playerShape, 0.5f);


        player.setJumpSpeed(90);
        player.setFallSpeed(45);
        player.setGravity(30);
        player.setPhysicsLocation(new Vector3f(0, 500, 0));
        bulletAppState.getPhysicsSpace().add(player);


        flyCam = new FlyByCamera(cam);
        flyCam.setMoveSpeed(120);
        flyCam.registerWithInput(inputManager);
        flyCam.setEnabled(true);
        //cam.setLocation(player.getPhysicsLocation());

        //camNode = new CameraNode("CamNode", cam);
        //camNode.setControlDir(ControlDirection.SpatialToCamera);
        //camNode.setLocalTranslation(new Vector3f(0, 500, 0));
        //camNode.lookAt(player.getViewDirection(), Vector3f.UNIT_Y); 
    }

    /**
     * actually handles the mouseclicks, and where they clicked in the world
     * @todo in need of obvios refactor/renaming
     * @param name "left_click" or "right_ click" event names
     * @param keyPressed whther or not said button/event was triggered
     */
    private void raycastMark(String name, boolean keyPressed) {
        String hit; // = "Nothing";
        Vector3f pt = null;
        float dist = 0;

        //if (name.equals("left_click") /*&& !keyPressed*/) {
        // 1. Reset results list.

        CollisionResults results = new CollisionResults();
        // 2. Aim the ray from cam loc to cam direction.
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        // 3. Collect intersections between Ray and Shootables in results list.
        rootNode.collideWith(ray, results);
        // 4. Print the results.
        //System.out.println("----- Collisions? " + results.size() + "-----");

        for (int i = 0; i < results.size(); i++) {
            // For each hit, we know distance, impact point, name of geometry.
            dist = results.getCollision(i).getDistance();
            pt = results.getCollision(i).getContactPoint();
        }

        // 5. Use the results (we mark the hit object)
        if (results.size() > 0) {
            // The closest collision point is what was truly hit:
            CollisionResult closest = results.getClosestCollision();
            hit = closest.getGeometry().getName();

            if (name.equals("left_click") && !keyPressed) {
                System.out.println("You Left-Clicked: " + hit + "   [at " + pt + ", " + dist + " wu away.)");
                try {
                    WOM.leftClickObject(hit);
                } catch (Exception x) {
                }
            } else if (name.equals("right_click") && !keyPressed) {
                // No hits? Then remove the red mark.

                System.out.println("You Right-Clicked: " + hit + "   at " + pt + ", " + dist + " wu away.");

                try {
                    WOM.rightClickObject(hit, playerCharacter);
                } catch (Exception x) {
                }
            }
        }
    }

    /**
     * Maps keytriggers to inputListeners to move the player and take other input.
     */
    public void initKeys() {
        // Mappings
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jumps", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("left_click", new MouseButtonTrigger(mouseInput.BUTTON_LEFT));
        inputManager.addMapping("right_click", new MouseButtonTrigger(mouseInput.BUTTON_RIGHT));

        inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_R));


        // Listeners
        inputManager.addListener(actionListener, "wireframe");


        inputManager.addListener(actionListener, "left_click");
        inputManager.addListener(actionListener, "right_click");

        inputManager.addListener(actionListener, "Lefts");
        inputManager.addListener(actionListener, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Jumps");
    }
   
    /**
     * Overrides the abstract class
     * @param binding The KeyBinding that was(or was not) triggered
     * @param value whether or not said key was pressed
     * @param tpf dont know/care
     */
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Lefts")) {
            left = value;
        } else if (binding.equals("Rights")) {
            right = value;
        } else if (binding.equals("Ups")) {
            up = value;
        } else if (binding.equals("Downs")) {
            down = value;
        } else if (binding.equals("Jumps")) {
            player.jump();
        }
  
    }


    /**
     * Updates the Camera position in the world
     * @param tpf currently unknown/forgot what this was for
     */
    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(1f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.5f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        try {
            player.setWalkDirection(walkDirection);
            cam.setLocation(player.getPhysicsLocation());
        } catch (Exception ex) {
        }
    }

    /**
     * Define and draw the crosshairs in the middle of the screen
     */
    protected void initCrossHairs() {
        //guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() /* * 2  */);
        ch.setText("[ > < ]"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

    /**
     * @note not used yet, and is temporary even when we DO use it
     * @todo  actually implement this
     */
    private void setInventoryText() {

        //guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        inventorytext = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"), false);


        Iterator<Item> itr = playerCharacter.getInventoryAsArrayList().iterator();
        while (itr.hasNext()) {
            Item i = itr.next();

            rootNode.attachChild(inventorytext);
        }
    }
    
    //TODO make separate file/class
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String binding, boolean value, float tpf) {

            if (binding.equals("Lefts")) {
                left = value;
            } else if (binding.equals("Rights")) {
                right = value;
            } else if (binding.equals("Ups")) {
                up = value;
            } else if (binding.equals("Downs")) {
                down = value;
            } else if (binding.equals("Jumps")) {
                player.jump();
            } else if (binding.equals("left_click")) {
                raycastMark(binding, value);
            } else if (binding.equals("right_click")) {
                raycastMark(binding, value);
            } else if (binding.equals("wireframe") && !value) {
                showWireframe = !showWireframe;
                mapMan.showFrames(showWireframe, player);
            }
            simpleUpdate(tpf);
        }
    };

    /**
     * @deprecated no longer used
     */
    protected void initMark() {
        Sphere sphere = new Sphere(30, 30, 0.2f);
        mark = new Geometry("Red Marker", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);

        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        pickText = new BitmapText(guiFont, false);

        pickText.setSize(guiFont.getCharSet().getRenderedSize());
        pickText.setText("Location" + this.player.getPhysicsLocation().toString());
        pickText.setLocalTranslation(300, pickText.getLineHeight(), 0);
        guiNode.attachChild(pickText); // write on the clean slate
    }


}