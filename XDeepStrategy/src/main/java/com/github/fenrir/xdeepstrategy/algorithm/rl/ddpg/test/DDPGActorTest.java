package com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.test;

import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.actor.DDPGActor;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.actor.DDPGMLPActor;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.experienceCache.DDPGExperienceCache;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.experienceCache.DDPGExperienceCacheItem;

public class DDPGActorTest {
    public static void main(String[] args){
        NDManager manager = NDManager.newBaseManager();

        // create experience cache
        DDPGExperienceCache cache = new DDPGExperienceCache.Builder()
                .setSampling(2, true, true)
                .setMaxSize(100)
                .build();

        for(int i = 0; i < 10000; i++){
            cache.put(DDPGExperienceCacheItem.create(
                    new DefaultDDPGState(manager.randomNormal(new Shape(4))),
                    new DefaultDDPGAction(manager.randomNormal(new Shape(3))),
                    new DefaultDDPGReward(manager.randomNormal(new Shape(4))),
                    new DefaultDDPGState(manager.randomNormal(new Shape(4)))
            ));
        }

        DDPGActor actor = DDPGMLPActor.create(4, 1, cache);
        actor.easyTrain();
    }
}
