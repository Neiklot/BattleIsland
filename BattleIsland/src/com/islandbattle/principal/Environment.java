package com.islandbattle.principal;

import java.awt.event.ActionEvent;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;

public class Environment extends SimpleApplication  implements ActionListener {
	  private BulletAppState bulletAppState;
	  private RigidBodyControl landscape;
	  private CharacterControl player;
	  private Vector3f walkDirection = new Vector3f();
	  private boolean left = false, right = false, up = false, down = false;
	  private TerrainQuad terrain;
	  private Material mat_terrain;
	  private FilterPostProcessor fpp;
	  private FogFilter fog;
	    
	public static void main(String[] args) {
		Environment app = new Environment();
		app.start(); // start the game
	}

	@Override
	public void simpleInitApp() {
			bulletAppState = new BulletAppState();
			stateManager.attach(bulletAppState);
			Node mainScene=new Node();
			createFog(mainScene);
			
		 	flyCam.setMoveSpeed(25);
		 	setUpKeys();
		    createTerrain();
		    characterAndCollitions();
		    bulletAppState.getPhysicsSpace().add(terrain);
		    bulletAppState.getPhysicsSpace().add(player);
	}

	private void createFog(Node mainScene) {
			mainScene.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
		    rootNode.attachChild(mainScene);

		    fpp=new FilterPostProcessor(assetManager);
		    //fpp.setNumSamples(4);
		    int numSamples = getContext().getSettings().getSamples();
		    if( numSamples > 0 ) {
		        fpp.setNumSamples(numSamples); 
		    }
		    fog=new FogFilter();
		    fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 0.1f));
		    fog.setFogDistance(250);
		    fog.setFogDensity(1.5f);
		    fpp.addFilter(fog);
		    viewPort.addProcessor(fpp);
	}

	private void characterAndCollitions() {
		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
		player = new CharacterControl(capsuleShape, 0.05f);
		player.setJumpSpeed(20);
		player.setFallSpeed(30);
		player.setGravity(30);
		player.setPhysicsLocation(new Vector3f(-10, 10, 10));
	}

	private void createTerrain() {
		/** 1. Create terrain material and load four textures into it. */
		mat_terrain = new Material(assetManager, 
		        "Common/MatDefs/Terrain/Terrain.j3md");
 
		/** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
		mat_terrain.setTexture("Alpha", assetManager.loadTexture(
		        "Textures/Terrain/splat/alphamap.png"));
 
		/** 1.2) Add GRASS texture into the red layer (Tex1). */
		Texture grass = assetManager.loadTexture(
		        "Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex1", grass);
		mat_terrain.setFloat("Tex1Scale", 64f);
 
		/** 1.3) Add DIRT texture into the green layer (Tex2) */
		Texture dirt = assetManager.loadTexture(
		        "Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex2", dirt);
		mat_terrain.setFloat("Tex2Scale", 32f);
 
		/** 1.4) Add ROAD texture into the blue layer (Tex3) */
		Texture rock = assetManager.loadTexture(
		        "Textures/Terrain/splat/road.jpg");
		rock.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex3", rock);
		mat_terrain.setFloat("Tex3Scale", 128f);
 
		/** 2. Create the height map */
		AbstractHeightMap heightmap = null;
		Texture heightMapImage = assetManager.loadTexture(
		        "Textures/Terrain/splat/mountains512.png");
		heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
		heightmap.load();
 
		int patchSize = 65;
		terrain = new TerrainQuad("my terrain", patchSize, 513, heightmap.getHeightMap());
 
		/** 4. We give the terrain its material, position & scale it, and attach it. */
		terrain.setMaterial(mat_terrain);
		terrain.setLocalTranslation(0, -100, 0);
		terrain.setLocalScale(2f, 1f, 2f);
		rootNode.attachChild(terrain);
 
		/** 5. The LOD (level of detail) depends on were the camera is: */
		TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
		terrain.addControl(control);
		
		terrain.addControl(new RigidBodyControl(0));
	}
	
	  private void setUpKeys() {
		    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
		    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
		    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
		    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
		    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		    inputManager.addListener(this, "Left");
		    inputManager.addListener(this, "Right");
		    inputManager.addListener(this, "Up");
		    inputManager.addListener(this, "Down");
		    inputManager.addListener(this, "Jump");
		  }
		 
	
	  public void onAction(String binding, boolean value, float tpf) {
		    if (binding.equals("Left")) {
		      if (value) { left = true; } else { left = false; }
		    } else if (binding.equals("Right")) {
		      if (value) { right = true; } else { right = false; }
		    } else if (binding.equals("Up")) {
		      if (value) { up = true; } else { up = false; }
		    } else if (binding.equals("Down")) {
		      if (value) { down = true; } else { down = false; }
		    } else if (binding.equals("Jump")) {
		      player.jump();
		    }
		  }
	
	 @Override
	  public void simpleUpdate(float tpf) {
	    Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
	    Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
	    walkDirection.set(0, 0, 0);
	    if (left)  { walkDirection.addLocal(camLeft); }
	    if (right) { walkDirection.addLocal(camLeft.negate()); }
	    if (up)    { walkDirection.addLocal(camDir); }
	    if (down)  { walkDirection.addLocal(camDir.negate()); }
	    player.setWalkDirection(walkDirection);
	    cam.setLocation(player.getPhysicsLocation());
	  }

}
