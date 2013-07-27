package PARlib.Items;

import PARclient.Main;
import PARclient.GameClient;
import com.jme3.bullet.BulletAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.scene.Spatial;
import com.jme3.material.Material;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer.CompareMode;
import com.jme3.shadow.PssmShadowRenderer.FilterMode;

import PARlib.*;
import PARlib.Items.*;
import PARlib.Items.Teapot;
import PARlib.MaterialHelper;
import PARlib.WorldObjectManager;
//import com.apple.eawt.CocoaComponent;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;

/**
 *
 * @author temp
 */
public class ObjectHelper {
    private BulletAppState  bulletAppState;
    private AssetManager assetManager;
    private WorldObjectManager WOM;
    private Spatial node;
    
    public  ObjectHelper(BulletAppState bulletAppStateRef, AssetManager assetManagerRef, WorldObjectManager WOM, Spatial node) {
        this.bulletAppState = bulletAppStateRef;
        this.assetManager = assetManagerRef;
	this.WOM = WOM;
    }
    
    
    public Spatial getSpatial(String model, String material, CollisionShape sh, String name, float Xcoord, float Ycoord, float Zcoord) {
	Spatial s = assetManager.loadModel(model);
	Material m = MaterialHelper.get(assetManager, material);
	s.setMaterial(m);
	
	RigidBodyControl model_phys = new RigidBodyControl(sh, 1f);
	setPhysics(model_phys);
	s.setLocalTranslation(Xcoord, Ycoord, Zcoord); 
        s.addControl(model_phys);
        s.setName(name); 
        s.setShadowMode(ShadowMode.CastAndReceive);
	
	bulletAppState.getPhysicsSpace().add(model_phys);
        bulletAppState.getPhysicsSpace().add(s);
	
	return s;
    }
    
    
    
    
    public Spatial getTeapot(String name, float Xcoord, float Ycoord, float Zcoord) {
        SphereCollisionShape sphere = new SphereCollisionShape(1f);
	Spatial teapot = getSpatial("Models/Teapot/Teapot.obj", "stone", sphere, name, Xcoord, Ycoord, Zcoord) ;	
	Teapot teapotItem = new Teapot(name);
	WOM.addItem(teapotItem);
	
        return teapot;
    }
    
    
    
    private void setPhysics(RigidBodyControl shapeRBC  ) {
        shapeRBC.setCcdMotionThreshold(0.01f);
        shapeRBC.setDamping(0.1f, 0.1f);
        shapeRBC.setMass(0.2f);
        shapeRBC.setFriction(0.1f);
    }   
}