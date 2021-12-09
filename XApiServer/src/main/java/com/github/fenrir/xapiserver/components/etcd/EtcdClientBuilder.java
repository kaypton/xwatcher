package com.github.fenrir.xapiserver.components.etcd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EtcdClientBuilder {
    static private final Logger LOGGER = LoggerFactory.getLogger(EtcdClientBuilder.class);

    public enum Client {
        JETCD("jetcd");

        private final String value;
        static private final Map<String, Client> MAP = new ConcurrentHashMap<>();

        static {
            for(Client client : values()){
                MAP.put(client.toString(), client);
            }
        }

        Client(final String value){
            this.value = value;
        }

        public String getStringValue(){
            return this.value;
        }

        @Override
        public String toString(){
            return this.getStringValue();
        }

        static public Client fromString(String client){
            return MAP.getOrDefault(client, null);
        }
    }

    private String[] endpoints = null;

    private String caFile = null;
    private String keyFile = null;
    private String certFile = null;
    private String password = null;
    private String user = null;
    private String authType = null;

    public EtcdClientBuilder endpoints(String[] endpoints){
        this.endpoints = endpoints;
        return this;
    }

    public EtcdClientBuilder ca(String caFile){
        this.caFile = caFile;
        return this;
    }

    public EtcdClientBuilder key(String certFile, String keyFile){
        this.certFile = certFile;
        this.keyFile = keyFile;
        return this;
    }

    public EtcdClientBuilder authType(String authType){
        this.authType = authType;
        return this;
    }

    public EtcdClientBuilder password(String password){
        this.password = password;
        return this;
    }

    public EtcdClientBuilder user(String user){
        this.user = user;
        return this;
    }

    public EtcdClient build(Client client){
        switch (client) {
            case JETCD:
                return new EtcdClientJEtcdImpl(
                        this.caFile,
                        this.keyFile,
                        this.certFile,
                        this.password,
                        this.user,
                        this.authType,
                        this.endpoints);
            default:
                LOGGER.error("No such client {}", client);
                return null;
        }
    }

    static public EtcdClientBuilder builder(){
        return new EtcdClientBuilder();
    }
}
