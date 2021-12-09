package com.github.fenrir.xdeepstrategy.nn;

import ai.djl.ndarray.NDArray;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.Parameter;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.training.initializer.NormalInitializer;

public class MultilayerPerceptron {

    private SequentialBlock net = null;

    public MultilayerPerceptron(){
        this.net = new SequentialBlock();

        this.net.add(Blocks.batchFlattenBlock(784));
        this.net.add(Linear.builder().setUnits(100).build());
        this.net.add(Activation::relu);
        this.net.add(Linear.builder().setUnits(10).build());
        this.net.setInitializer(new NormalInitializer(), Parameter.Type.WEIGHT);
    }
}
