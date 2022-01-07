package com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities;

import ai.djl.ndarray.NDArray;

public abstract class DDPGAction {
    public DDPGAction(){

    }
    public abstract NDArray toNDArray();
}
