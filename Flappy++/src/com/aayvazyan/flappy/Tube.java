package com.aayvazyan.flappy;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

class Tube extends AnimatedSprite {
    private final PhysicsHandler mPhysicsHandler;
    private int VELOCITY = 400;
    private AnimatedSprite collideWith;
    private MainActivity mainActivity;
    private boolean spawnnew;

    public Tube(final float pX, final float pY, final TiledTextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager, AnimatedSprite collideWith, MainActivity ma, boolean spawnnew) {
        super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
        this.mainActivity = ma;
        this.collideWith = collideWith;
        this.mPhysicsHandler = new PhysicsHandler(this);
        this.registerUpdateHandler(this.mPhysicsHandler);
        this.mPhysicsHandler.setVelocity(VELOCITY, 0);
        this.spawnnew = spawnnew;
    }

    @Override
    protected void onManagedUpdate(final float pSecondsElapsed) {
        //Check collision with the bird
        if (this.collidesWith(collideWith)) {
            try {
                mainActivity.loose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Check collision with the wall
        if (this.mX < 0) {
            this.setX(0);
            this.mPhysicsHandler.setVelocityX(VELOCITY);
            this.mainActivity.increaseScore(0.05);
            System.err.println(this.mainActivity.score);
            if (this.spawnnew) mainActivity.createObstacle(mainActivity.mTubeTextureRegion, collideWith);
        } else if (this.mX + this.getWidth() > MainActivity.CAMERA_WIDTH) {
            this.mPhysicsHandler.setVelocityX(-VELOCITY);
        }
        super.onManagedUpdate(pSecondsElapsed);
    }
}