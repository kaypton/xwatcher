package com.github.fenrir.xstrategy.services.strategies;

import com.github.fenrir.xcommon.clients.BaseResponse;

public interface StrategyService {
    BaseResponse trigger();
    BaseResponse result();
    BaseResponse test();
    void startup();
}
