package com.github.fenrir.xdeepstrategy.entities.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * input:
 *      size: 784
 * output:
 *      size: 10
 * layers:
 *      layer_num: 3
 *      layers:
 *          - layer1:
 *              units: 10
 *              activation: relu
 *          - layer2:
 *              units: 5
 *              activation: relu
 *          - layer3:
 *              units: 10
 *              activation: relu
 * initializer:
 *      method: Normal
 *      params: Weight
 */
public class MultilayerPerceptronConfig extends BaseConfig {
    private final static Logger LOGGER = LoggerFactory.getLogger(MultilayerPerceptronConfig.class);

    public MultilayerPerceptronConfig(String filePath) {
        super(filePath);

        Map<String, Object> configObjects = this.yamlObjects;
        Set<String> keySet = configObjects.keySet();

        if(!keySet.contains("input")){
            LOGGER.error("No `input` found");
            this.invalid = true;
        }

        if(!keySet.contains("layers")){
            LOGGER.error("No `layers` found");
            this.invalid = true;
        }

        if(!keySet.contains("output")){
            LOGGER.error("No `output` found");
            this.invalid = true;
        }

        if(!keySet.contains("initializer")){
            LOGGER.error("No `initializer` found");
            this.invalid = true;
        }
    }
}
