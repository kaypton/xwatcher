package com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities;

import ai.djl.ndarray.NDArray;

public abstract class DDPGState {
    public DDPGState(){

    }
    public abstract NDArray toNDArray();
}
