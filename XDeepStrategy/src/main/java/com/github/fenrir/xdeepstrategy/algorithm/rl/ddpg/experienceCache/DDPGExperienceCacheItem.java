package com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.experienceCache;

import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities.DDPGAction;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities.DDPGReward;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities.DDPGState;

public class DDPGExperienceCacheItem {
    private final DDPGState state0;
    private final DDPGAction action;
    private final DDPGReward reward;
    private final DDPGState state1;

    private DDPGExperienceCacheItem(DDPGState state0,
                                    DDPGAction action,
                                    DDPGReward reward,
                                    DDPGState state1){
        this.state0 = state0;
        this.state1 = state1;
        this.reward = reward;
        this.action = action;
    }

    static public DDPGExperienceCacheItem create(DDPGState state0,
                                                 DDPGAction action,
                                                 DDPGReward reward,
                                                 DDPGState state1){
        return new DDPGExperienceCacheItem(state0, action, reward, state1);
    }

    public DDPGState getState0() {
        return state0;
    }

    public DDPGAction getAction() {
        return action;
    }

    public DDPGReward getReward() {
        return reward;
    }

    public DDPGState getState1() {
        return state1;
    }
}
