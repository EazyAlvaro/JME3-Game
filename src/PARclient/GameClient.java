package PARclient;

 //Physics and collission
//import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
//import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
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
import com.jme3.terrain.geomipmap.TerrainQuad;

import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection.*;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridListener;
import com.jme3.terrain.geomipmap.TerrainGridLodControl;
import com.jme3.terrain.geomipmap.grid.FractalTileLoader;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.noise.ShaderUtils;
import com.jme3.terrain.noise.basis.FilteredBasis;
import com.jme3.terrain.noise.filter.IterativeFilter;
import com.jme3.terrain.noise.filter.OptimizedErode;
import com.jme3.terrain.noise.filter.PerturbFilter;
import com.jme3.terrain.noise.filter.SmoothFilter;
import com.jme3.terrain.noise.fractal.FractalSum;
import com.jme3.terrain.noise.modulator.NoiseModulator;
import com.jme3.util.SkyFactory;
import java.util.Iterator;
import javax.management.JMException;

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
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.renderer.Camera;
import java.util.ArrayList;
import java.util.List;
import com.jme3.math.ColorRGBA;


public class GameClient extends SimpleApplication
        implements ActionListener {

    // <editor-fold defaultstate="collapsed" desc="Local Properties">
    // terrain
    private Material terrain_single_material; // textures/material for single maps
    private Material terrain_grid_material; // texture/material for quad/testing maps
    private Material terrain_wire;
    private boolean wireframe = false;
    public TerrainGrid terrain_grid;
    public TerrainQuad terrain_single;
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
    private FractalSum base;
    private PerturbFilter perturb;
    private OptimizedErode therm;
    private SmoothFilter smooth;
    private IterativeFilter iterate;
    private int scale_x = 2; // width
    private int scale_y = 2; //height;
    private int scale_z = scale_x; // depth, but all maps are square
    private int map_multipl = 4;
    private float grassScale = 64;
    private float dirtScale = 32;
    private float rockScale = 128;
    private boolean usePhysics = true;
    
    //public LightManager lightManager;
    private GameClient app = null;

    // </editor-fold>
    public static void main(String[] args) {
        GameClient app = new GameClient();
        app.setShowSettings(true);
        app.start();
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
            initSimpleMapMaterials();
            initSingleMap();
            initSingleLOD();
        } else { //for actual in-game testing
            initGridMaterials();
            initGridMap();
        }
        
        initLightning();
        
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
     * Initiates a single quad heightmap based on own custom heightmap generator
     * @uses InterpolatedheightMap
     */
    private void initSingleMap() {
        float sourceMap[][] = InterpolatedHeightMap.getBigSourceMap();

        InterpolatedHeightMap heightMap = null;
        try {
            heightMap = new InterpolatedHeightMap(map_multipl, sourceMap);
        } catch (JMException x) {
            //
        } catch (Exception x2) {
            //
        }
        terrain_single = new TerrainQuad("terrain", 513, heightMap.getSize(), heightMap.getHeightMap());
        //, new LodPerspectiveCalculatorFactory(getCamera(), 4)); // add this in to see it use entropy for LOD calculations

        terrain_single.setMaterial(terrain_single_material);
        terrain_single.setLocalTranslation(0, -200, 0);
        terrain_single.setLocalScale(scale_x, scale_y, scale_z);
        rootNode.attachChild(terrain_single);

        CollisionShape terrainShape =
                CollisionShapeFactory.createMeshShape((Node) terrain_single);
        RigidBodyControl landscape = new RigidBodyControl(terrainShape, 0);
        terrain_single.addControl(landscape);

        bulletAppState.getPhysicsSpace().add(terrain_single);
    }

    /**
     * Initiates the fractal-based grid-map process for 'infinite' terrain
     */
    public void initGridMap() {
        System.out.println("InitGridMap");

        this.base = new FractalSum();
        this.base.setRoughness(0.75f);
        this.base.setFrequency(0.5f);
        this.base.setAmplitude(0.75f);
        this.base.setLacunarity(2.12f);
        this.base.setOctaves(8);
        this.base.setScale(0.02125f);
        this.base.addModulator(new NoiseModulator() {
            @Override
            public float value(float... in) {
                return ShaderUtils.clamp(in[0] * 0.5f + 0.5f, 0, 1);
            }
        });
        System.out.println("ground & perturb");
        FilteredBasis ground = new FilteredBasis(this.base);

        this.perturb = new PerturbFilter();
        this.perturb.setMagnitude(0.119f);

        this.therm = new OptimizedErode();
        this.therm.setRadius(1);
        this.therm.setTalus(0.011f);

        this.smooth = new SmoothFilter();
        this.smooth.setRadius(1);
        this.smooth.setEffect(0.7f);

        this.iterate = new IterativeFilter();
        this.iterate.addPreFilter(this.perturb);
        this.iterate.addPostFilter(this.smooth);
        this.iterate.setFilter(this.therm);
        this.iterate.setIterations(1);

        ground.addPreFilter(this.iterate);

        //map stuff
        this.terrain_grid = new TerrainGrid("terrain", 129, 513, new FractalTileLoader(ground, 256f));
        System.out.println("Adding terrain and scale");
        this.terrain_grid.setMaterial(this.terrain_grid_material);
        this.terrain_grid.setLocalTranslation(0, 0, 0);
        this.terrain_grid.setLocalScale(this.scale_x, this.scale_y, this.scale_z);
        this.rootNode.attachChild(this.terrain_grid);

        System.out.println("Collisionshape");
        CollisionShape terrainShape = CollisionShapeFactory.createMeshShape((Node) terrain_grid);
        RigidBodyControl landscape = new RigidBodyControl(terrainShape, 0);
        this.terrain_grid.addControl(landscape);
        this.bulletAppState.getPhysicsSpace().add(this.terrain_grid);

        TerrainLodControl control = new TerrainGridLodControl(this.terrain_grid, this.getCamera());
        control.setLodCalculator(new DistanceLodCalculator(65, 1.7f));
        this.terrain_grid.addControl(control);

        System.out.println("Adding listners");
        this.terrain_grid.addListener(new TerrainGridListener() {
            //this.usePhysics = true;
            public void gridMoved(Vector3f newCenter) {
                System.out.println("Grid Moved:" + newCenter.toString());
            }

            public void tileAttached(Vector3f cell, TerrainQuad quad) {
                System.out.println("Tile! " + quad.getName());

                if (usePhysics) {
                    quad.addControl(new RigidBodyControl(new HeightfieldCollisionShape(quad.getHeightMap(), terrain_grid.getLocalScale()), 0));
                    bulletAppState.getPhysicsSpace().add(quad);
                }
                //updateMarkerElevations();
            }

            public void tileDetached(Vector3f cell, TerrainQuad quad) {
                System.out.println("Un-Tile!");
                if (usePhysics) {
                    if (quad.getControl(RigidBodyControl.class) != null) {
                        bulletAppState.getPhysicsSpace().remove(quad);
                        quad.removeControl(RigidBodyControl.class);
                    }
                }

            }
        });
    }

    /**
     * Enables Level of Detail Controller - which makes distant objects less complex, and thus 'cheaper' to render
     */
    private void initSingleLOD() {
        List<Camera> cameras = new ArrayList<Camera>();
        cameras.add(getCamera());
        TerrainLodControl control = new TerrainLodControl(terrain_single, cameras);
        control.setLodCalculator(new DistanceLodCalculator((1024 * (map_multipl * 2)) + 1, 1.1f)); // patch size, and a multiplier

        terrain_single.addControl(control);
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
     * Defines and sets textures for grid-based terrain based on stock SDK splatmaps (at the moment)
     * @todo make this procedural
     */
    public void initGridMaterials() { // dirt, grass, rock
        System.out.println("InitGridMaterials");
        this.terrain_grid_material = new Material(this.assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        terrain_grid_material.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alpha1.png"));
        terrain_grid_material.setTexture("AlphaMap_1", assetManager.loadTexture("Textures/Terrain/splat/alpha2.png"));

        Texture grass = this.assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        Texture dirt = this.assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        Texture rock = this.assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg");
        rock.setWrap(WrapMode.MirroredRepeat);
        Texture road = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);

        this.terrain_grid_material.setTexture("DiffuseMap", grass);
        terrain_grid_material.setFloat("DiffuseMap_0_scale", rockScale / 8);

        this.terrain_grid_material.setTexture("DiffuseMap_1", grass);
        terrain_grid_material.setFloat("DiffuseMap_1_scale", rockScale / 3);

        //diffmap 2: medium-sized land-masses, occasional splotches
        this.terrain_grid_material.setTexture("DiffuseMap_2", grass);
        terrain_grid_material.setFloat("DiffuseMap_2_scale", grassScale);

        //Diffmap 3 : veiny/roadlike strips and bends
        terrain_grid_material.setTexture("DiffuseMap_3", dirt);
        terrain_grid_material.setFloat("DiffuseMap_3_scale", grassScale * 2);

        //diffmap 4 : seemingly random plotsches
        this.terrain_grid_material.setTexture("DiffuseMap_4", rock);
        terrain_grid_material.setFloat("DiffuseMap_4_scale", rockScale / 3);

        //diffmap 5 : more splotches
        this.terrain_grid_material.setTexture("DiffuseMap_5", dirt);
        terrain_grid_material.setFloat("DiffuseMap_5_scale", dirtScale);

        //diffmap 6: ?
        // this.terrain_grid_material.setTexture("DiffuseMap_6", grass);
        // terrain_grid_material.setFloat("DiffuseMap_6_scale", dirtScale);

        //diffmap 7: ?
        // this.terrain_grid_material.setTexture("DiffuseMap_7", road);
        // terrain_grid_material.setFloat("DiffuseMap_7_scale", dirtScale);

        Texture normalMapGrass = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg"); //grass_normal.jpg
        normalMapGrass.setWrap(WrapMode.Repeat);
        Texture normalMapDirt = assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png"); // 
        normalMapDirt.setWrap(WrapMode.Repeat);
        Texture normalMapRoad = assetManager.loadTexture("Textures/Terrain/splat/road_normal.png"); //road_normal.jpg
        normalMapRoad.setWrap(WrapMode.Repeat);

        terrain_grid_material.setTexture("NormalMap", normalMapGrass);
        //terrain_single_material.setTexture("NormalMap_1", normalMapGrass);
        //terrain_single_material.setTexture("NormalMap_2", normalMapGrass);
        //terrain_single_material.setTexture("NormalMap_4", normalMap2);

        this.terrain_grid_material.setName("terrain_grid_material");

        // WIREFRAME material
        terrain_wire = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        terrain_wire.getAdditionalRenderState().setWireframe(true);
        terrain_wire.setColor("Color", ColorRGBA.Red);

        System.out.println(terrain_grid_material.getParams().toString());
    }

     /**
     * Defines and sets textures for single quad-based terrain based on stock SDK splatmaps (at the moment)
     * @todo make this procedural
     */
    public void initSimpleMapMaterials() {
        /**
         * 1. Create terrain material and load four textures into it.
         */
        this.terrain_single_material = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        terrain_single_material.setBoolean("useTriPlanarMapping", false);

        terrain_single_material.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alpha1.png"));
        terrain_single_material.setTexture("AlphaMap_1", assetManager.loadTexture("Textures/Terrain/splat/alpha2.png"));

        // GRASS texture
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        terrain_single_material.setTexture("DiffuseMap_1", grass);
        terrain_single_material.setFloat("DiffuseMap_1_scale", grassScale);

        // DIRT texture
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        terrain_single_material.setTexture("DiffuseMap", dirt);
        terrain_single_material.setFloat("DiffuseMap_0_scale", dirtScale);

        // ROCK texture
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg"); 
        rock.setWrap(WrapMode.Repeat);
        terrain_single_material.setTexture("DiffuseMap_2", rock);
        terrain_single_material.setFloat("DiffuseMap_2_scale", rockScale);

        // BRICK texture
        Texture brick = assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"); 
        brick.setWrap(WrapMode.Repeat);
        terrain_single_material.setTexture("DiffuseMap_3", brick);
        terrain_single_material.setFloat("DiffuseMap_3_scale", rockScale);

        // RIVER ROCK texture
        Texture riverRock = assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"); 
        terrain_single_material.setTexture("DiffuseMap_4", riverRock);
        terrain_single_material.setFloat("DiffuseMap_4_scale", rockScale);

        // WIREFRAME material
        terrain_wire = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        terrain_wire.getAdditionalRenderState().setWireframe(true);
        terrain_wire.setColor("Color", ColorRGBA.White);
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
                wireframe = !wireframe;
                if (!wireframe) {
                    try {
                        System.out.println(player.getPhysicsLocation().toString());
                        terrain_single.setMaterial(terrain_wire);
                        System.out.println("single/wire");
                    } catch (Exception x) {
                        terrain_grid.setMaterial(terrain_wire);
                        System.out.println("catch! grid/wire");
                    }
                } else {
                    try {
                        System.out.println(player.getPhysicsLocation().toString());
                        terrain_grid.setMaterial(terrain_grid_material);
                        System.out.println("single/mat");
                    } catch (Exception y) {
                        System.out.println(player.getPhysicsLocation().toString());
                        terrain_grid.setMaterial(terrain_grid_material);
                        System.out.println("catch single/mat " + y.toString() + " " + terrain_grid_material.getName());
                    }
                }
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

    private void initLightning() {
        LightManager lightManager = new LightManager(this);
        lightManager.initGridShadow();
        lightManager.initLighting();
        lightManager.initBloom(2f);
    }
}