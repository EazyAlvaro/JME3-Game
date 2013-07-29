/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PARclient;

import PARlib.InterpolatedHeightMap;
import PARlib.PlayerCharacter;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridListener;
import com.jme3.terrain.geomipmap.TerrainGridLodControl;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
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
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.List;
import javax.management.JMException;

/**
 *
 * @author bob
 */
public class MapManager {
    
        // terrain
    private Material terrain_single_material; // textures/material for single maps
    private Material terrain_grid_material; // texture/material for quad/testing maps
    private Material terrain_wire;
    public TerrainGrid terrain_grid;
    public TerrainQuad terrain_single;
    private int scale_x = 2; // width
    private int scale_y = 2; //height;
    private int scale_z = scale_x; // depth, but all maps are square
    private int map_multipl = 4;
    private float grassScale = 64;
    private float dirtScale = 32;
    private float rockScale = 128;
    private boolean usePhysics = true;
        
    
    
    public TerrainQuad getTerrainQuad()
    {
        return terrain_single;
    }
    
       public TerrainGrid getTerrainGrid()
    {
        return terrain_grid;
    }
       
    
    /**
     * Initiates a single quad heightmap based on own custom heightmap generator
     * @uses InterpolatedheightMap
     */
    public void initSingleMap(Node rootNode, BulletAppState bulletAppState) {
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

    public void showFrames(boolean showWireframe, CharacterControl player)
    {
        if (!showWireframe) {
            try {
                System.out.println(player.getPhysicsLocation().toString());
                terrain_single.setMaterial(terrain_wire);

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
    /**
     * Initiates the fractal-based grid-map process for 'infinite' terrain
     */
    public void initGridMap(Node rootNode, final BulletAppState bulletAppState, Camera camera) {
        System.out.println("InitGridMap");

        
        FractalSum base = new FractalSum();
        base.setRoughness(0.75f);
        base.setFrequency(0.5f);
        base.setAmplitude(0.75f);
        base.setLacunarity(2.12f);
        base.setOctaves(8);
        base.setScale(0.02125f);
        base.addModulator(new NoiseModulator() {
            @Override
            public float value(float... in) {
                return ShaderUtils.clamp(in[0] * 0.5f + 0.5f, 0, 1);
            }
        });
        System.out.println("ground & perturb");
        FilteredBasis ground = new FilteredBasis(base);

        PerturbFilter perturb = new PerturbFilter();
        perturb.setMagnitude(0.119f);

        OptimizedErode therm = new OptimizedErode();
        therm.setRadius(1);
        therm.setTalus(0.011f);

        SmoothFilter smooth = new SmoothFilter();
        smooth.setRadius(1);
        smooth.setEffect(0.7f);

        IterativeFilter iterate = new IterativeFilter();
        iterate.addPreFilter(perturb);
        iterate.addPostFilter(smooth);
        iterate.setFilter(therm);
        iterate.setIterations(1);

        ground.addPreFilter(iterate);

        //map stuff
        terrain_grid = new TerrainGrid("terrain", 129, 513, new FractalTileLoader(ground, 256f));
        System.out.println("Adding terrain and scale");
        terrain_grid.setMaterial(terrain_grid_material);
        terrain_grid.setLocalTranslation(0, 0, 0);
        terrain_grid.setLocalScale(scale_x, scale_y, scale_z);
        rootNode.attachChild(terrain_grid);

        System.out.println("Collisionshape");
        CollisionShape terrainShape = CollisionShapeFactory.createMeshShape((Node) terrain_grid);
        RigidBodyControl landscape = new RigidBodyControl(terrainShape, 0);
        terrain_grid.addControl(landscape);
        bulletAppState.getPhysicsSpace().add(terrain_grid);

        TerrainLodControl control = new TerrainGridLodControl(terrain_grid, camera);
        control.setLodCalculator(new DistanceLodCalculator(65, 1.7f));
        terrain_grid.addControl(control);

        System.out.println("Adding listners");
        terrain_grid.addListener(new TerrainGridListener() {
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
     * Defines and sets textures for single quad-based terrain based on stock SDK splatmaps (at the moment)
     * @todo make this procedural
     */
    public void initSimpleMapMaterials(AssetManager assetManager) {
        /**
         * 1. Create terrain material and load four textures into it.
         */
        this.terrain_single_material = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        terrain_single_material.setBoolean("useTriPlanarMapping", false);

        terrain_single_material.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alpha1.png"));
        terrain_single_material.setTexture("AlphaMap_1", assetManager.loadTexture("Textures/Terrain/splat/alpha2.png"));

        // GRASS texture
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        terrain_single_material.setTexture("DiffuseMap_1", grass);
        terrain_single_material.setFloat("DiffuseMap_1_scale", grassScale);

        // DIRT texture
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        terrain_single_material.setTexture("DiffuseMap", dirt);
        terrain_single_material.setFloat("DiffuseMap_0_scale", dirtScale);

        // ROCK texture
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg"); 
        rock.setWrap(Texture.WrapMode.Repeat);
        terrain_single_material.setTexture("DiffuseMap_2", rock);
        terrain_single_material.setFloat("DiffuseMap_2_scale", rockScale);

        // BRICK texture
        Texture brick = assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"); 
        brick.setWrap(Texture.WrapMode.Repeat);
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
     * Defines and sets textures for grid-based terrain based on stock SDK splatmaps (at the moment)
     * @todo make this procedural
     */
    public void initGridMaterials(AssetManager assetMan) { // dirt, grass, rock
        System.out.println("InitGridMaterials");
        this.terrain_grid_material = new Material(assetMan, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        terrain_grid_material.setTexture("AlphaMap", assetMan.loadTexture("Textures/Terrain/splat/alpha1.png"));
        terrain_grid_material.setTexture("AlphaMap_1", assetMan.loadTexture("Textures/Terrain/splat/alpha2.png"));

        Texture grass = assetMan.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        Texture dirt = assetMan.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        Texture rock = assetMan.loadTexture("Textures/Terrain/Rock2/rock.jpg");
        rock.setWrap(Texture.WrapMode.MirroredRepeat);
        Texture road = assetMan.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(Texture.WrapMode.Repeat);

        terrain_grid_material.setTexture("DiffuseMap", grass);
        terrain_grid_material.setFloat("DiffuseMap_0_scale", rockScale / 8);

        terrain_grid_material.setTexture("DiffuseMap_1", grass);
        terrain_grid_material.setFloat("DiffuseMap_1_scale", rockScale / 3);

        //diffmap 2: medium-sized land-masses, occasional splotches
        terrain_grid_material.setTexture("DiffuseMap_2", grass);
        terrain_grid_material.setFloat("DiffuseMap_2_scale", grassScale);

        //Diffmap 3 : veiny/roadlike strips and bends
        terrain_grid_material.setTexture("DiffuseMap_3", dirt);
        terrain_grid_material.setFloat("DiffuseMap_3_scale", grassScale * 2);

        //diffmap 4 : seemingly random plotsches
        terrain_grid_material.setTexture("DiffuseMap_4", rock);
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

        Texture normalMapGrass = assetMan.loadTexture("Textures/Terrain/splat/grass_normal.jpg"); //grass_normal.jpg
        normalMapGrass.setWrap(Texture.WrapMode.Repeat);
        Texture normalMapDirt = assetMan.loadTexture("Textures/Terrain/splat/dirt_normal.png"); // 
        normalMapDirt.setWrap(Texture.WrapMode.Repeat);
        Texture normalMapRoad = assetMan.loadTexture("Textures/Terrain/splat/road_normal.png"); //road_normal.jpg
        normalMapRoad.setWrap(Texture.WrapMode.Repeat);

        terrain_grid_material.setTexture("NormalMap", normalMapGrass);
        //terrain_single_material.setTexture("NormalMap_1", normalMapGrass);
        //terrain_single_material.setTexture("NormalMap_2", normalMapGrass);
        //terrain_single_material.setTexture("NormalMap_4", normalMap2);

        this.terrain_grid_material.setName("terrain_grid_material");

        // WIREFRAME material
        terrain_wire = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        terrain_wire.getAdditionalRenderState().setWireframe(true);
        terrain_wire.setColor("Color", ColorRGBA.Red);

        System.out.println(terrain_grid_material.getParams().toString());
    }
    
        /**
     * Enables Level of Detail Controller - which makes distant objects less complex, and thus 'cheaper' to render
     */
    public void initSingleLOD(Camera camera) {
        List<Camera> cameras = new ArrayList<Camera>();
        cameras.add(camera);
        TerrainLodControl control = new TerrainLodControl(terrain_single, cameras);
        control.setLodCalculator(new DistanceLodCalculator((1024 * (map_multipl * 2)) + 1, 1.1f)); // patch size, and a multiplier

        terrain_single.addControl(control);
    }
}
