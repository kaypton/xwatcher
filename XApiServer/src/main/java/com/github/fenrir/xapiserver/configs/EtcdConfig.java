package com.github.fenrir.xapiserver.configs;

import com.github.fenrir.xapiserver.components.etcd.EtcdClient;
import com.github.fenrir.xapiserver.components.etcd.EtcdClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class EtcdConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdConfig.class);

    private final String[] etcdEndpoints;
    private final EtcdClientBuilder.Client client;
    private String caFile;
    private String keyFile;
    private String certFile;
    private final String authType;
    private String password;
    private String user;

    public EtcdConfig(@Value("${Components.etcd.endpoints}") String etcdEndpoints,
                      @Value("${Components.etcd.client}") String client,
                      @Value("${Components.etcd.ca}") String caFile,
                      @Value("${Components.etcd.cert}") String certFile,
                      @Value("${Components.etcd.key}") String keyFile,
                      @Value("${Components.etcd.password}") String password,
                      @Value("${Components.etcd.user}") String user,
                      @Value("${Components.etcd.authType}") String authType) throws Exception {
        this.etcdEndpoints = etcdEndpoints.split(",");
        this.client = EtcdClientBuilder.Client.fromString(client);
        if(this.client == null){
            LOGGER.error("{} client do not support or exist", client);
            throw new Exception(client + " client do not support or exist");
        }

        this.authType = authType;

        if(this.authType.equals("ssl")){
            this.caFile = caFile;
            this.certFile = certFile;
            this.keyFile = keyFile;
            LOGGER.info("ssl:");
            LOGGER.info("     ca:{}", caFile);
            LOGGER.info("   cert:{}", certFile);
            LOGGER.info("    key:{}", keyFile);
            this.password = null;
            this.user = null;
        }else if(this.authType.equals("password")){
            this.password = password;
            this.user = user;
            LOGGER.info("password:");
            LOGGER.info("   password:{}", this.password);
            LOGGER.info("       user:{}", this.user);
            this.caFile = null;
            this.certFile = null;
            this.keyFile = null;
        }
    }

    @Bean(name = "EtcdClient")
    @Scope("prototype")
    public EtcdClient getEtcdClient(){
        if(this.authType.equals("ssl"))
            return EtcdClientBuilder.builder()
                    .endpoints(this.etcdEndpoints)
                    .authType("ssl")
                    .ca(this.caFile)
                    .key(this.certFile, this.keyFile)
                    .build(this.client);
        else if(this.authType.equals("password"))
            return EtcdClientBuilder.builder()
                    .endpoints(this.etcdEndpoints)
                    .authType("password")
                    .password(this.password)
                    .user(this.user)
                    .build(this.client);
        return null;
    }
}
