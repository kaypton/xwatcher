package com.github.fenrir.xdeepstrategy.entities.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public abstract class BaseConfig {

    protected Map<String, Object> yamlObjects = null;
    protected boolean invalid = false;

    protected BaseConfig(String filePath){
        load(filePath);
    }

    private void load(String filePath){
        Yaml yaml = new Yaml();
        File configFile = new File(filePath);
        try {
            InputStream fileInputStream = new FileInputStream(configFile);
            this.yamlObjects = yaml.load(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isInvalid(){
        return this.invalid;
    }
}
