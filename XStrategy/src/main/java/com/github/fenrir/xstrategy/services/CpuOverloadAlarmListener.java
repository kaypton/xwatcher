package com.github.fenrir.xstrategy.services;

import com.github.fenrir.xstrategy.services.strategies.antcolonysystemservice.AntColonySystemService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CpuOverloadAlarmListener {
    private final AntColonySystemService antColonySystemService;

    public CpuOverloadAlarmListener(@Autowired AntColonySystemService antColonySystemService){
        // TODO hook watcher
        this.antColonySystemService = antColonySystemService;
    }
}
