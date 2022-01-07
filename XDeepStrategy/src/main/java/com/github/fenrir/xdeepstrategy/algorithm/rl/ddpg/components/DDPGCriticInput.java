package com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.components;

import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities.DDPGAction;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities.DDPGState;

public class DDPGCriticInput {
    private Tuple2<DDPGState, DDPGAction> input;

    public DDPGState getState(){
        return input.first;
    }

    public DDPGAction getAction(){
        return input.second;
    }
}
