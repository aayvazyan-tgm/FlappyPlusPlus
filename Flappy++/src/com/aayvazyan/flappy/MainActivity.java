package com.aayvazyan.flappy;

import android.app.*;
import android.content.Context;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.particle.SpriteParticleSystem;
import org.andengine.entity.particle.emitter.PointParticleEmitter;
import org.andengine.entity.particle.initializer.AccelerationParticleInitializer;
import org.andengine.entity.particle.initializer.BlendFunctionParticleInitializer;
import org.andengine.entity.particle.initializer.ColorParticleInitializer;
import org.andengine.entity.particle.initializer.RotationParticleInitializer;
import org.andengine.entity.particle.initializer.VelocityParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.ColorParticleModifier;
import org.andengine.entity.particle.modifier.ExpireParticleInitializer;
import org.andengine.entity.particle.modifier.ScaleParticleModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;

import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 *
 * @author Ari Ayvazyan ariay@live.at
 * @version 16.02.2014
 * 
 */
public class MainActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener, IOnAreaTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	public static int CAMERA_WIDTH;// = 720;
	public static int CAMERA_HEIGHT = 380;
	
	// ===========================================================
	// Fields
	// ===========================================================
	private ITextureRegion mParallaxLayerBack;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private BitmapTextureAtlas m2BitmapTextureAtlas;
	private ITextureRegion mParallaxLayerFront;
	
	private TiledTextureRegion mBoxFaceTextureRegion;
	public TiledTextureRegion mTubeTextureRegion;
	public TiledTextureRegion mLongTubeTextureRegion;

	private PhysicsWorld mPhysicsWorld;
	private Font mFont;
	public Scene mScene;
	private AnimatedSprite mainChar;
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	private TextureRegion mParallaxLayerMiddle;
	public Text elapsedText;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {
		//Toast.makeText(this, "Touch the screen to add objects. Touch an object to shoot it up into the air.", Toast.LENGTH_LONG).show();
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int cw = displayMetrics.widthPixels;
        int ch = displayMetrics.heightPixels;
        double ratio=(((double)ch)/((double)cw));
        CAMERA_WIDTH =(int)((double)CAMERA_HEIGHT/(double)ratio);
        System.out.println("cw:"+cw+"ch"+ch+"r"+ratio);
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		//return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy((float) (1d/ratio)), camera);

	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "birdy.png", 0, 0, 1,1);
		//this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_circle_tiled.png", 0, 32, 2, 1); // 64x32
		this.mBitmapTextureAtlas.load();
		
		//this.mParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "particle_fire.png", 0, 0);
		//this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
		
		this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
		//this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_front.png", 0, 0);
		this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "Hintergrund2.png", 0, 0);
		this.mParallaxLayerMiddle = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "hintergrund.png", 0, 0);
		this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "ScrollingBG.png", 0, CAMERA_HEIGHT);
		this.mAutoParallaxBackgroundTexture.load();
		
		this.m2BitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),1024, 1024, TextureOptions.BILINEAR);
		this.mTubeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.m2BitmapTextureAtlas, this, "Tube.png", 0, 0, 1, 1);
		this.mLongTubeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.m2BitmapTextureAtlas, this, "Longtube.png", 0, 0, 1, 1);
		//this.mObstacleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.m2BitmapTextureAtlas, this, "face_circle_tiled.png", 0, 0, 2, 1);
		this.m2BitmapTextureAtlas.load();
		
		
		this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 12);
		this.mFont.load();
		
		
		
	}

	@Override
	public Scene onCreateScene() {
		//this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH*4), false);

		this.mScene = new Scene();
		//this.mScene.setBackground(new Background(0, 0, 0));
		this.mScene.setOnSceneTouchListener(this);

		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
        //Borders
		final Rectangle ground = new Rectangle(-2, CAMERA_HEIGHT, CAMERA_WIDTH-2, 0, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(-2, -2, CAMERA_WIDTH+2, 0, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(-2, -2, 0, CAMERA_HEIGHT-2, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 4,-2, 0, CAMERA_HEIGHT-2, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		//this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);

		// Create World objects
		this.createBird(100, 200);
		//final Rectangle centerRectangle = new Rectangle(centerX - 50, centerY - 16, 32, 32, this.getVertexBufferObjectManager());

		//Craete Text
		elapsedText = new Text(100, 50, this.mFont, "Debug:", "Debug: XXXXXXXXXXXXXXXXXXXXX".length(), this.getVertexBufferObjectManager());
		elapsedText.setColor(Color.RED);
		mScene.attachChild(elapsedText);
		
		
		//Create animated background
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-1.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerBack.getHeight(), this.mParallaxLayerBack, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerMiddle.getHeight(), this.mParallaxLayerMiddle, vertexBufferObjectManager)));
		//autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, this.mParallaxLayerMid, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager)));
		mScene.setBackground(autoParallaxBackground);
		
		//start listeners
		
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		this.mScene.setOnAreaTouchListener(this);
		//
		// Create the Tubes
		//
		createObstacle(this.mLongTubeTextureRegion,this.mainChar);

		return this.mScene;
	}

	public void createObstacle(TiledTextureRegion texReg,
			AnimatedSprite collideWith) {
		final Tube tube = new Tube(CAMERA_WIDTH, 0, texReg, this.getVertexBufferObjectManager(),collideWith,this,true);
		
		final Tube tube2 = new Tube(CAMERA_WIDTH, CAMERA_HEIGHT-texReg.getHeight(), texReg, this.getVertexBufferObjectManager(),collideWith,this,true);
		tube.setFlipped(false, true);
		
		//set physics for the both tubes
		/*final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
		Body b1 = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tube, BodyType.DynamicBody, objectFixtureDef);
		Body b2 = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tube2, BodyType.DynamicBody, objectFixtureDef);*/	
		//this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(tube, b1, true, true));
		//this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(tube2, b2, true, true));
		
		
		mScene.attachChild(tube2);
		mScene.attachChild(tube);
		
	}

	@Override
	public boolean onAreaTouched( final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		/*if(pSceneTouchEvent.isActionDown()) {
			final AnimatedSprite face = (AnimatedSprite) pTouchArea;
			this.jumpFace(face);
			return true;
		}*/

		return false;
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		/*if(this.mPhysicsWorld != null) {
			if(pSceneTouchEvent.isActionDown()) {
				this.addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				return true;
			}
		}
		return false;*/
		if(pSceneTouchEvent.isActionDown()) {
			this.jumpFace(this.mainChar);
			return true;
		}
		
		
		return false;
	}

	@Override
	public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {

	}

	@Override
	public void onAccelerationChanged(final AccelerationData pAccelerationData) {
		/*this.mGravityX = pAccelerationData.getX();
		this.mGravityY = pAccelerationData.getY();

		final Vector2 gravity = Vector2Pool.obtain(this.mGravityX, this.mGravityY);
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);*/
	}

	//	@Override
	//	public void onResumeGame() {
	//		super.onResumeGame();
	//
	//		this.enableAccelerationSensor(this);
	//	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void createBird(final float pX, final float pY) {

		final AnimatedSprite face;
		final Body body;

		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

		//if(this.mFaceCount % 2 == 1){
			face = new AnimatedSprite(pX, pY, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
			body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
		/*} else {
			face = new AnimatedSprite(pX, pY, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager());
			body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
		}*/

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));

		//face.animate(new long[]{1200,200}, 0, 1, true);
		face.setUserData(body);
		this.mainChar=face;
		
		/*this.mScene.registerTouchArea(face);*/
		this.mScene.attachChild(face);
	}

	private void jumpFace(final AnimatedSprite face) {
		final Body faceBody = (Body)face.getUserData();
		final Vector2 velocity = Vector2Pool.obtain(0,-10.5f);
		faceBody.setLinearVelocity(velocity);
		faceBody.setAngularVelocity(faceBody.getAngularVelocity()+0.5f);
		Vector2Pool.recycle(velocity);
		/*//Particle effects
		{
			final SpriteParticleSystem particleSystem = new SpriteParticleSystem(new PointParticleEmitter(faceBody.getPosition().x, faceBody.getPosition().y), 2, 3, 6, this.mParticleTextureRegion, this.getVertexBufferObjectManager());
			particleSystem.addParticleInitializer(new BlendFunctionParticleInitializer<Sprite>(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE));
			particleSystem.addParticleInitializer(new VelocityParticleInitializer<Sprite>(15, 22, -60, -90));
			particleSystem.addParticleInitializer(new AccelerationParticleInitializer<Sprite>(5, 15));
			particleSystem.addParticleInitializer(new RotationParticleInitializer<Sprite>(0.0f, 360.0f));
			particleSystem.addParticleInitializer(new ColorParticleInitializer<Sprite>(1.0f, 0.0f, 0.0f));
			particleSystem.addParticleInitializer(new ExpireParticleInitializer<Sprite>(11.5f));

			particleSystem.addParticleModifier(new ScaleParticleModifier<Sprite>(0, 5, 0.5f, 2.0f));
			particleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(2.5f, 3.5f, 1.0f, 0.0f));
			particleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(3.5f, 4.5f, 0.0f, 1.0f));
			particleSystem.addParticleModifier(new ColorParticleModifier<Sprite>(0.0f, 11.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f));
			particleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(4.5f, 11.5f, 1.0f, 0.0f));
			this.mScene.attachChild(particleSystem);
			
		}*/
		//face.registerEntityModifier(new RotationModifier(6, 0, 360));
	}

	public void loose() {
		reload();
	}

    public void reload() {
    	
	    /* Does not work perfectly
	    Intent intent = getIntent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	    finish();
	    overridePendingTransition(0, 0);
	
	    startActivity(intent);
	    overridePendingTransition(0, 0);
	    */
        Context context=getApplicationContext();
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, mPendingIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
}
	
	/*@Override
	public void onBackPressed() {
		super.onBackPressed();
		onResumeGame();
	}*/
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
