package com.aayvazyan.flappy;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.widget.Toast;
import org.andengine.util.system.SystemUtils;

class Tube extends AnimatedSprite {
	private final PhysicsHandler mPhysicsHandler;
	private int VELOCITY = 400;
	private AnimatedSprite collideWith;
	private MainActivity mainActivity;
	private boolean spawnnew;
	public Tube(final float pX, final float pY, final TiledTextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager,AnimatedSprite collideWith,MainActivity ma, boolean spawnnew) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		this.mainActivity=ma;
		this.collideWith=collideWith;
		this.mPhysicsHandler = new PhysicsHandler(this);
		this.registerUpdateHandler(this.mPhysicsHandler);
		this.mPhysicsHandler.setVelocity(VELOCITY,0);
		this.spawnnew=spawnnew;
	}

	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		//Check collision with the wall
		if(this.mX < 0) {
            this.setX(0);
			this.mPhysicsHandler.setVelocityX(VELOCITY);
			this.mainActivity.increaseScore(0.1);
            System.err.println(this.mainActivity.score);
			if(this.spawnnew)mainActivity.createObstacle(mainActivity.mTubeTextureRegion , collideWith);
		} else if(this.mX + this.getWidth() > MainActivity.CAMERA_WIDTH) {
			this.mPhysicsHandler.setVelocityX(-VELOCITY);
		}
		//Check collision with the bird
		if(this.collidesWith(collideWith)) {
			try {
				mainActivity.loose();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/*if(this.mY < 0) {
			this.mPhysicsHandler.setVelocityY(VELOCITY);
		} else if(this.mY + this.getHeight() > MainActivity.CAMERA_HEIGHT) {
			this.mPhysicsHandler.setVelocityY(-VELOCITY);
		}*/
		super.onManagedUpdate(pSecondsElapsed);
	}
}