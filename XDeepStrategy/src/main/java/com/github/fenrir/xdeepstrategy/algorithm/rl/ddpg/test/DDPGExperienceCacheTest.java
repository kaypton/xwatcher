package com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.test;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.EasyTrain;
import ai.djl.training.dataset.*;
import ai.djl.translate.TranslateException;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities.DDPGAction;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities.DDPGReward;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities.DDPGState;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.experienceCache.DDPGExperienceCache;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.experienceCache.DDPGExperienceCacheItem;

import java.io.IOException;

class DefaultDDPGState extends DDPGState {

    private final NDArray ndArray;

    public DefaultDDPGState(NDArray ndArray){
        this.ndArray = ndArray;
    }

    @Override
    public NDArray toNDArray() {
        return this.ndArray;
    }
}

class DefaultDDPGAction extends DDPGAction {

    private final NDArray ndArray;

    public DefaultDDPGAction(NDArray ndArray){
        this.ndArray = ndArray;
    }

    @Override
    public NDArray toNDArray() {
        return this.ndArray;
    }
}

class DefaultDDPGReward extends DDPGReward {

    private final NDArray ndArray;

    public DefaultDDPGReward(NDArray ndArray){
        this.ndArray = ndArray;
    }

    @Override
    public NDArray toNDArray() {
        return this.ndArray;
    }
}

public class DDPGExperienceCacheTest {
    public static void main(String[] args) throws TranslateException, IOException {
        NDManager manager = NDManager.newBaseManager();
        DDPGExperienceCache cache = new DDPGExperienceCache.Builder()
                .setSampling(2, true, true)
                .setMaxSize(100)
                .build();
        // 1
        cache.put(DDPGExperienceCacheItem.create(
                new DefaultDDPGState(manager.ones(new Shape(4))),
                new DefaultDDPGAction(manager.ones(new Shape(3))),
                new DefaultDDPGReward(manager.ones(new Shape(4))),
                new DefaultDDPGState(manager.ones(new Shape(4)))
        ));

        // 2
        cache.put(DDPGExperienceCacheItem.create(
                new DefaultDDPGState(manager.ones(new Shape(4))),
                new DefaultDDPGAction(manager.ones(new Shape(3))),
                new DefaultDDPGReward(manager.ones(new Shape(4))),
                new DefaultDDPGState(manager.ones(new Shape(4)))
        ));

        // 3
        cache.put(DDPGExperienceCacheItem.create(
                new DefaultDDPGState(manager.ones(new Shape(4))),
                new DefaultDDPGAction(manager.ones(new Shape(3))),
                new DefaultDDPGReward(manager.ones(new Shape(4))),
                new DefaultDDPGState(manager.ones(new Shape(4)))
        ));

        // 4
        cache.put(DDPGExperienceCacheItem.create(
                new DefaultDDPGState(manager.ones(new Shape(4))),
                new DefaultDDPGAction(manager.ones(new Shape(3))),
                new DefaultDDPGReward(manager.ones(new Shape(4))),
                new DefaultDDPGState(manager.ones(new Shape(4)))
        ));

        // 5
        cache.put(DDPGExperienceCacheItem.create(
                new DefaultDDPGState(manager.ones(new Shape(4))),
                new DefaultDDPGAction(manager.ones(new Shape(3))),
                new DefaultDDPGReward(manager.ones(new Shape(4))),
                new DefaultDDPGState(manager.ones(new Shape(4)))
        ));

        Iterable<Batch> batches = cache.getData(manager);
        for(Batch batch : batches){
            System.out.println("=====================");
            System.out.println(batch.getSize());
            for(NDArray array : batch.getData()){
                System.out.println(array);
            }
            System.out.println("=====================");
        }
    }
}
