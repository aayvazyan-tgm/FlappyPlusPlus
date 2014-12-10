package com.aayvazyan.flappy;

import org.andengine.entity.IEntity;
import org.andengine.entity.scene.Scene;
import java.util.LinkedList;

/**
 * @author Ari Ayvazyan
 * @version 07.Nov.14
 */
public class EntityManager {
    private LinkedList<IEntity> entities=new LinkedList<IEntity>();
    private Scene scene;

    public EntityManager(Scene scene) {
        this.scene = scene;
    }

    public void addChild(IEntity myNewEntity){
        entities.add(myNewEntity);
    }

    public void clearAll(){
        for (IEntity entity : entities) {
//            scene.detachChild(entity);
            entity.dispose();
//            entity.detachSelf();
            entities.remove(entity);
        }
    }
}
