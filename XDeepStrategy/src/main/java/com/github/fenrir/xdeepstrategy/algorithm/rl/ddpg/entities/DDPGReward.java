package com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities;

import ai.djl.ndarray.NDArray;

public abstract class DDPGReward {
    public DDPGReward(){

    }
    public abstract NDArray toNDArray();
}
