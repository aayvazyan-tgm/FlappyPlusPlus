package com.aayvazyan.flappy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.webkit.GeolocationPermissions;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;

/**
 * @author Ari Ayvazyan ariay@live.at
 * @version 13.07.2014
 */
public class MainActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener {
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
    public Entity tubeLevel;
    private AnimatedSprite mainChar;
    private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
    private TextureRegion mParallaxLayerMiddle;
    public Text elapsedText;
    public double score = 0;
    private EntityManager entityManager;


    @Override
    public EngineOptions onCreateEngineOptions() {
        //Toast.makeText(this, "Touch the screen to add objects. Touch an object to shoot it up into the air.", Toast.LENGTH_LONG).show();
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int cw = displayMetrics.widthPixels;
        int ch = displayMetrics.heightPixels;
        double ratio = (((double) ch) / ((double) cw));
        CAMERA_WIDTH = (int) ((double) CAMERA_HEIGHT / ratio);
        System.out.println("cw:" + cw + "ch" + ch + "r" + ratio);
        final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        //return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
        return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy((float) (1d / ratio)), camera);

    }

    @Override
    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
        this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "birdy.png", 0, 0, 1, 1);
        this.mBitmapTextureAtlas.load();

        this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
        this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "Hintergrund2.png", 0, 0);
        this.mParallaxLayerMiddle = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "hintergrund.png", 0, 0);
        this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "ScrollingBG.png", 0, CAMERA_HEIGHT);
        this.mAutoParallaxBackgroundTexture.load();

        this.m2BitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
        this.mTubeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.m2BitmapTextureAtlas, this, "Tube.png", 0, 0, 1, 1);
        this.mLongTubeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.m2BitmapTextureAtlas, this, "Longtube.png", 0, 0, 1, 1);
        this.m2BitmapTextureAtlas.load();

        final ITexture fontTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
        this.mFont = FontFactory.createFromAsset(this.getFontManager(), fontTexture, this.getAssets(),
                "fnt/SCRIPTIN.ttf", 30, true, android.graphics.Color.WHITE);
        this.mFont.load();
    }

    @Override
    public Scene onCreateScene() {
        this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH * 4), false);

        this.mScene = new Scene();
        this.entityManager=new EntityManager(mScene);
        this.mScene.setOnSceneTouchListener(this);

        final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
        //Borders
        final Rectangle ground = new Rectangle(-2, CAMERA_HEIGHT, CAMERA_WIDTH - 2, 0, vertexBufferObjectManager);
        final Rectangle roof = new Rectangle(-2, -2, CAMERA_WIDTH + 2, 0, vertexBufferObjectManager);
        final Rectangle left = new Rectangle(-2, -2, 0, CAMERA_HEIGHT - 2, vertexBufferObjectManager);
        final Rectangle right = new Rectangle(CAMERA_WIDTH - 4, -2, 0, CAMERA_HEIGHT - 2, vertexBufferObjectManager);

        final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

        this.mScene.attachChild(ground);
        this.mScene.attachChild(left);
        this.mScene.attachChild(right);

        // Create World objects
        this.createBird(100, 200);

        //Create animated background
        final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
        autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-1.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerBack.getHeight(), this.mParallaxLayerBack, vertexBufferObjectManager)));
        autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerMiddle.getHeight(), this.mParallaxLayerMiddle, vertexBufferObjectManager)));
        autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager)));
        mScene.setBackground(autoParallaxBackground);

        //start listeners
        this.mScene.registerUpdateHandler(this.mPhysicsWorld);
        //attach the tubeLevel before the Text so this entity is drawn behind it
        tubeLevel = new Entity();
        this.mScene.attachChild(tubeLevel);
        //Craete Text
        elapsedText = new Text(10, 10, this.mFont, "Score: 12345678.9,0", 17, this.getVertexBufferObjectManager());
        elapsedText.setText("0");
        elapsedText.setColor(Color.WHITE);
        mScene.attachChild(elapsedText);

        // Create the Tubes
        createObstacle(this.mLongTubeTextureRegion, this.mainChar);
        return this.mScene;
    }

    public void createObstacle(TiledTextureRegion texReg, AnimatedSprite collideWith) {
        final Tube tube = new Tube(CAMERA_WIDTH, 0, texReg, this.getVertexBufferObjectManager(), collideWith, this, true);
        final Tube tube2 = new Tube(CAMERA_WIDTH, CAMERA_HEIGHT - texReg.getHeight(), texReg, this.getVertexBufferObjectManager(), collideWith, this, true);
        tube.setFlipped(false, true);

        entityManager.addChild(tube);
        entityManager.addChild(tube2);

        tubeLevel.attachChild(tube2);
        tubeLevel.attachChild(tube);
    }

    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        if (pSceneTouchEvent.isActionDown()) {
            this.jumpFace(this.mainChar);
            return true;
        }
        return false;
    }

    private void createBird(final float pX, final float pY) {

        final AnimatedSprite face;
        final Body body;
        final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
        face = new AnimatedSprite(pX, pY, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
        body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
        this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));

        face.setUserData(body);
        this.mainChar = face;
        this.mScene.attachChild(face);
    }

    private void jumpFace(final AnimatedSprite face) {
        final Body faceBody = (Body) face.getUserData();
        final Vector2 velocity = Vector2Pool.obtain(0, -10.5f);
        faceBody.setLinearVelocity(velocity);
        faceBody.setAngularVelocity(faceBody.getAngularVelocity() + 1.5f);
        Vector2Pool.recycle(velocity);
    }

    public void loose() {
        onPauseGame();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        reload();
    }

    public void reload() {
        //resetAll();
	    /* Does not work perfectly */
        Context context = getApplicationContext();
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, mPendingIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public synchronized void increaseScore(double increaseBy) {
        this.score += increaseBy;
        this.elapsedText.setText("" + this.score);
    }

    public void resetAll(){
        this.entityManager.clearAll();
    }
}